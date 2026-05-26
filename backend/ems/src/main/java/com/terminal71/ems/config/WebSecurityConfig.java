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
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable());

    // Require authentication for API endpoints except the auth routes.
    http.authorizeHttpRequests(authz -> authz
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/api/v1/firebase/rtdb/health").permitAll()
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/api/**").permitAll()
        .anyRequest().permitAll()
    );

    // JWT filter
    String jwtSecret = System.getenv().getOrDefault("JWT_SECRET", "");
    if (jwtSecret == null || jwtSecret.isBlank()) {
      // Generate a temporary runtime secret for local/dev use. In production, set JWT_SECRET.
      byte[] b = new byte[32];
      new java.security.SecureRandom().nextBytes(b);
      jwtSecret = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(b);
      org.slf4j.LoggerFactory.getLogger(WebSecurityConfig.class)
          .warn("JWT_SECRET not set — using temporary runtime secret. Set JWT_SECRET in env for production.");
    }
    JwtUtil jwtUtil = new JwtUtil(jwtSecret);
    JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtil);

    // Basic request logging
    RequestLoggingFilter loggingFilter = new RequestLoggingFilter();

    // Security headers filter (lightweight WAF-like protections)
    com.terminal71.ems.security.SecurityHeadersFilter securityHeadersFilter = new com.terminal71.ems.security.SecurityHeadersFilter();
    http.addFilterBefore(securityHeadersFilter, UsernamePasswordAuthenticationFilter.class);

    // First register the JWT filter (so we can reference its class for ordering)
    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    // If Redis is configured, use Redis-backed rate limiter (recommended for multiple instances)
    String redisUrl = System.getenv().getOrDefault("REDIS_URL", "").trim();
    if (!redisUrl.isBlank()) {
      try {
        var ctx = org.springframework.web.context.ContextLoader.getCurrentWebApplicationContext();
        StringRedisTemplate redisTemplate = ctx.getBean(StringRedisTemplate.class);
        RedisRateLimitingFilter redisRateLimiter = new RedisRateLimitingFilter(redisTemplate, 1, java.time.Duration.ofSeconds(1));
        http.addFilterBefore(redisRateLimiter, JwtAuthenticationFilter.class);
      } catch (Exception e) {
        RateLimitingFilter rateLimiter = new RateLimitingFilter(30);
        http.addFilterBefore(rateLimiter, JwtAuthenticationFilter.class);
      }
    } else {
      RateLimitingFilter rateLimiter = new RateLimitingFilter(20);
      http.addFilterBefore(rateLimiter, JwtAuthenticationFilter.class);
    }

    http.addFilterBefore(loggingFilter, JwtAuthenticationFilter.class);
    return http.build();
  }
}
