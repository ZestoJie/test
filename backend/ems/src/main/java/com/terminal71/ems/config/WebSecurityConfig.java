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
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // Read allowed frontend origin from env var; fallback to allow all in dev
    String frontendOrigin = System.getenv().getOrDefault("FRONTEND_ORIGIN", "");

    if (frontendOrigin != null && !frontendOrigin.isBlank()) {
      http.cors(cors -> cors.configurationSource(request -> {
        var cfg = new org.springframework.web.cors.CorsConfiguration();
        cfg.setAllowedOrigins(java.util.List.of(frontendOrigin));
        cfg.setAllowedMethods(java.util.List.of("GET","POST","PUT","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(java.util.List.of("*"));
        cfg.setAllowCredentials(true);
        return cfg;
      }));
    } else {
      http.cors(Customizer.withDefaults());
    }

    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(authz -> authz.requestMatchers("/api/**").permitAll().anyRequest().permitAll());
    return http.build();
  }
}
