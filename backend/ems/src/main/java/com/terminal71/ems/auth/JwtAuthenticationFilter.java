package com.terminal71.ems.auth;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                var decoded = jwtUtil.verifyToken(token);
                String subject = decoded.getSubject();
                Map<String, Object> claims = jwtUtil.extractClaims(token);
                var role = claims.getOrDefault("role", "User").toString();
                var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                var authToken = new UsernamePasswordAuthenticationToken(subject, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception e) {
                log.warn("Invalid JWT token: {}. Trying Firebase ID token fallback.", e.getMessage());
                if (!tryAuthenticateFirebaseToken(token)) {
                    log.warn("Invalid Firebase token or Firebase not configured.");
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean tryAuthenticateFirebaseToken(String token) {
        try {
            if (com.google.firebase.FirebaseApp.getApps().isEmpty()) {
                return false;
            }
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(token);
            String subject = decoded.getUid();
            var claims = decoded.getClaims();
            String role = "User";
            if (claims.containsKey("role") && claims.get("role") != null) {
                role = claims.get("role").toString();
            }
            var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
            var authToken = new UsernamePasswordAuthenticationToken(subject, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authToken);
            return true;
        } catch (Exception e) {
            log.debug("Firebase token verification failed", e);
            return false;
        }
    }
}
