package com.terminal71.ems.config;

import java.time.Duration;
import java.util.ArrayList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.terminal71.ems.auth.JwtAuthenticationFilter;
import com.terminal71.ems.auth.JwtUtil;
import com.terminal71.ems.security.RequestLoggingFilter;
import com.terminal71.ems.security.RedisRateLimitingFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

  @Bean
  public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
    var cfg = new CorsConfiguration();

    String frontendOrigin = System.getenv().getOrDefault("FRONTEND_ORIGIN", "").trim();

    var allowedOrigins = new ArrayList<String>();
    if (frontendOrigin != null && !frontendOrigin.isBlank()) {
      allowedOrigins.add(frontendOrigin);
    }

    allowedOrigins.add("https://terminal71-ems.web.app");
    allowedOrigins.add("http://localhost:5173");
    allowedOrigins.add("http://127.0.0.1:5173");

    cfg.setAllowedOrigins(allowedOrigins);
    cfg.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    cfg.setAllowedHeaders(java.util.List.of("*"));
    cfg.setAllowCredentials(false);

    var source = new UrlBasedCorsConfigurationSource();
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

      // =========================
      // 1. REQUEST LOGGING FIRST
      // =========================
      http.addFilterBefore(
          new RequestLoggingFilter(),
          UsernamePasswordAuthenticationFilter.class
      );

      // =========================
      // 2. REDIS RATE LIMITER (ONLY ONE LIMITER)
      // =========================
      http.addFilterBefore(
          new RedisRateLimitingFilter(
              redisTemplate,
              2,
              Duration.ofSeconds(1)
          ),
          UsernamePasswordAuthenticationFilter.class
      );

      // =========================
      // 3. JWT FILTER
      // =========================
      String jwtSecret = System.getenv().getOrDefault("JWT_SECRET", "");

      if (jwtSecret == null || jwtSecret.isBlank()) {
          byte[] b = new byte[32];
          new java.security.SecureRandom().nextBytes(b);
          jwtSecret = java.util.Base64.getUrlEncoder()
                  .withoutPadding()
                  .encodeToString(b);
      }

      JwtUtil jwtUtil = new JwtUtil(jwtSecret);
      JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtil);

      http.addFilterBefore(
          jwtFilter,
          UsernamePasswordAuthenticationFilter.class
      );

      return http.build();
  }
}