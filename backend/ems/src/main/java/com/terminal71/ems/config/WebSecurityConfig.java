package com.terminal71.ems.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.terminal71.ems.auth.JwtUtil;
import com.terminal71.ems.auth.JwtAuthenticationFilter;
import com.terminal71.ems.security.RequestLoggingFilter;
import com.terminal71.ems.security.RateLimitingFilter;
import com.terminal71.ems.security.RedisRateLimitingFilter;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

  @Bean
  public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
    var cfg = new org.springframework.web.cors.CorsConfiguration();
    String frontendOrigin = System.getenv().getOrDefault("FRONTEND_ORIGIN", "").trim();
    var allowedOrigins = new java.util.ArrayList<String>();
    if (frontendOrigin != null && !frontendOrigin.isBlank()) {
      allowedOrigins.add(frontendOrigin);
    }
    allowedOrigins.add("https://terminal71-ems.web.app");
    allowedOrigins.add("http://localhost:5173");
    allowedOrigins.add("http://127.0.0.1:5173");
    cfg.setAllowedOrigins(allowedOrigins);
    // Do not allow a wildcard origin pattern. Rely on explicit allowed origins above.
    cfg.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    cfg.setAllowedHeaders(java.util.List.of("*"));
    cfg.setAllowCredentials(false);
    var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
  }
  
  @Bean
  public SecurityFilterChain filterChain(
          HttpSecurity http,
          StringRedisTemplate redisTemplate) throws Exception {

      http.cors(Customizer.withDefaults())
          .csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(authz -> authz
              .requestMatchers("/api/auth/**").permitAll()
              .requestMatchers("/api/**").permitAll()
              .anyRequest().permitAll()
          );

      // 🔥 ORDER FIX: logging FIRST (safe, no mutation)
      http.addFilterBefore(new RequestLoggingFilter(), UsernamePasswordAuthenticationFilter.class);

      // 🔥 Security headers second
      http.addFilterBefore(
          new com.terminal71.ems.security.SecurityHeadersFilter(),
          UsernamePasswordAuthenticationFilter.class
      );
      // ✅ REDIS RATE LIMITER (NOW ACTIVE)
      http.addFilterBefore(
        new RedisRateLimitingFilter(redisTemplate, 20, java.time.Duration.ofSeconds(1)),
        UsernamePasswordAuthenticationFilter.class
      );

      // 🔥 JWT LAST (DO NOT affect login/register)
      String jwtSecret = System.getenv().getOrDefault("JWT_SECRET", "");
      if (jwtSecret == null || jwtSecret.isBlank()) {
          byte[] b = new byte[32];
          new java.security.SecureRandom().nextBytes(b);
          jwtSecret = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(b);
      }

      JwtUtil jwtUtil = new JwtUtil(jwtSecret);
      JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtil);

      http.addFilterAfter(jwtFilter, UsernamePasswordAuthenticationFilter.class);

      return http.build();
  }
}
