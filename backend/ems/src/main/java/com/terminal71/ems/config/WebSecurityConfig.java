package com.terminal71.ems.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

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
    cfg.setAllowedOriginPatterns(java.util.List.of("*"));
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
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(authz -> authz.requestMatchers("/api/**").permitAll().anyRequest().permitAll());
    return http.build();
  }
}
