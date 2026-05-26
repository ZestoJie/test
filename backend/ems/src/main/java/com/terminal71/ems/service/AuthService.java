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
    private final java.util.Optional<FirebaseRealtimeService> rtdb;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // lightweight in-memory fallback for local/dev when Firebase is not configured
    private final java.util.concurrent.ConcurrentMap<String, Map<String, Object>> inMemoryUsers = new java.util.concurrent.ConcurrentHashMap<>();

    public AuthService(@org.springframework.lang.Nullable FirebaseRealtimeService rtdb) {
        this.rtdb = java.util.Optional.ofNullable(rtdb);

        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            // For developer convenience, generate a temporary secret but warn loudly.
            byte[] b = new byte[32];
            new java.security.SecureRandom().nextBytes(b);
            secret = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(b);
            log.warn("JWT_SECRET not set — generated temporary runtime secret. Set JWT_SECRET in env for persistent tokens and production.");
        }
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
            if (rtdb.isPresent()) {
                var existing = rtdb.get().readData(key).get();
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

                rtdb.get().writeData(key, payload).get();

                UserDto u = new UserDto();
                u.setId(((Long) payload.get("id")));
                u.setName((String) payload.get("name"));
                u.setRole((String) payload.get("role"));
                return u;
            } else {
                // local in-memory fallback
                if (inMemoryUsers.containsKey(key)) throw new IllegalStateException("User already exists");
                String hash = passwordEncoder.encode(password);
                Map<String, Object> payload = new HashMap<>();
                payload.put("email", email);
                payload.put("passwordHash", hash);
                payload.put("name", name == null ? "" : name);
                payload.put("role", role == null ? "User" : role);
                payload.put("id", System.currentTimeMillis());
                inMemoryUsers.put(key, payload);

                UserDto u = new UserDto();
                u.setId(((Long) payload.get("id")));
                u.setName((String) payload.get("name"));
                u.setRole((String) payload.get("role"));
                return u;
            }
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
            Map<?, ?> m;
            if (rtdb.isPresent()) {
                var snap = rtdb.get().readData(key).get();
                if (snap == null || !snap.exists()) {
                    throw new IllegalArgumentException("Invalid credentials");
                }
                Object val = snap.getValue();
                if (!(val instanceof Map)) throw new IllegalArgumentException("Invalid credentials");
                m = (Map<?, ?>) val;
            } else {
                if (!inMemoryUsers.containsKey(key)) throw new IllegalArgumentException("Invalid credentials");
                m = inMemoryUsers.get(key);
            }

            String hash = (String) m.get("passwordHash");
            if (hash == null || !passwordEncoder.matches(password, hash)) {
                throw new IllegalArgumentException("Invalid credentials");
            }
            Object nameObj = m.get("name");
            String name = nameObj == null ? "" : String.valueOf(nameObj);
            Object roleObj = m.get("role");
            String role = roleObj == null ? "User" : String.valueOf(roleObj);
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
