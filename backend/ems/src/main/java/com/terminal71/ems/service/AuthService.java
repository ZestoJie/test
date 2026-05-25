package com.terminal71.ems.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.terminal71.ems.auth.JwtUtil;
import com.terminal71.ems.dto.UserDto;


@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final FirebaseRealtimeService rtdb;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(FirebaseRealtimeService rtdb) {
        this.rtdb = Objects.requireNonNull(rtdb, "FirebaseRealtimeService must be available");

        String secret = System.getenv()
                .getOrDefault("JWT_SECRET", "dev-secret-change-me");

        this.jwtUtil = new JwtUtil(secret);
    }

    private String keyForEmail(String email) {
        return URLEncoder.encode(email.toLowerCase(), StandardCharsets.UTF_8);
    }

    public UserDto register(String email, String password, String name, String role) {
        log.info("Register called for email={} name={} role={}", email, name, role);
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("email and password required");
        }
        try {
            String key = "auth/users/" + keyForEmail(email);
            var existing = rtdb.readData(key).get();
            if (existing != null && existing.exists()) {
                throw new IllegalStateException("User already exists");
            }

            String hash = passwordEncoder.encode(password);
            Map<String, Object> payload = new HashMap<>();
            payload.put("email", email);
            payload.put("passwordHash", hash);
            payload.put("name", name == null ? "" : name);
            payload.put("role", role == null ? "User" : role);
            payload.put("id", System.currentTimeMillis());

            rtdb.writeData(key, payload).get();

            UserDto u = new UserDto();
            u.setId(((Long) payload.get("id")));
            u.setName((String) payload.get("name"));
            u.setRole((String) payload.get("role"));
            return u;
        } catch (RuntimeException re) {
            throw re;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to register user", e);
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> login(String email, String password) {
        log.info("Login called for email={}", email);
        try {
            String key = "auth/users/" + keyForEmail(email);
            var snap = rtdb.readData(key).get();
            if (snap == null || !snap.exists()) {
                throw new IllegalArgumentException("Invalid credentials");
            }
            Object val = snap.getValue();
            if (!(val instanceof Map)) throw new IllegalArgumentException("Invalid credentials");
            Map<?, String> m = (Map<?, String>) val;
            String hash = (String) m.get("passwordHash");
            if (hash == null || !passwordEncoder.matches(password, hash)) {
                throw new IllegalArgumentException("Invalid credentials");
            }
            String name = (String) m.getOrDefault("name", "");
            String role = (String) m.getOrDefault("role", "User");
            Object idObj = m.get("id");
            String idStr = idObj == null ? "" : String.valueOf(idObj);

            Map<String, Object> claims = new HashMap<>();
            claims.put("email", email);
            claims.put("name", name);
            claims.put("role", role);
            claims.put("id", idStr);

            String token = jwtUtil.generateToken(email, claims);

            Map<String, Object> resp = new HashMap<>();
            resp.put("token", token);
            resp.put("user", Map.of("email", email, "name", name, "role", role, "id", idStr));
            return resp;
        } catch (RuntimeException re) {
            throw re;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Login failed", e);
            throw new RuntimeException(e);
        }
    }
}
