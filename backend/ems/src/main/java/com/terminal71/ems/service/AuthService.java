package com.terminal71.ems.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.Locale;

import com.google.firebase.database.DataSnapshot;
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
    private final java.util.Optional<FirebaseFirestoreService> firestore;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // lightweight in-memory fallback for local/dev when Firebase is not configured
    private final java.util.concurrent.ConcurrentMap<String, Map<String, Object>> inMemoryUsers = new java.util.concurrent.ConcurrentHashMap<>();

    public AuthService(@org.springframework.lang.Nullable FirebaseRealtimeService rtdb,
            @org.springframework.lang.Nullable FirebaseFirestoreService firestore) {
        this.rtdb = java.util.Optional.ofNullable(rtdb);
        this.firestore = java.util.Optional.ofNullable(firestore);

        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            // For developer convenience, generate a temporary secret but warn loudly.
            byte[] b = new byte[32];
            new java.security.SecureRandom().nextBytes(b);
            secret = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(b);
            log.warn(
                    "JWT_SECRET not set — generated temporary runtime secret. Set JWT_SECRET in env for persistent tokens and production.");
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
            if (firestore.isPresent()) {
                String key = docIdForEmail(email);
                var existing = firestore.get().readDocument("auth_users", key);
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

                firestore.get().writeDocument("auth_users", key, payload);

                UserDto u = new UserDto();
                u.setId(((Long) payload.get("id")));
                u.setName((String) payload.get("name"));
                u.setRole((String) payload.get("role"));
                return u;
            } else if (rtdb.isPresent()) {
                String key = "auth/users/" + keyForEmail(email);
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
                String key = "auth/users/" + keyForEmail(email);
                if (inMemoryUsers.containsKey(key))
                    throw new IllegalStateException("User already exists");
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

    public Map<String, Object> login(String login, String password) {
        log.info("Login called for login={}", login);
        try {
            if (!StringUtils.hasText(login) || !StringUtils.hasText(password)) {
                throw new IllegalArgumentException("login and password required");
            }

            Map<?, ?> m = findUserByLogin(login);

            String hash = (String) m.get("passwordHash");
            if (hash == null || !passwordEncoder.matches(password, hash)) {
                throw new IllegalArgumentException("Invalid credentials");
            }
            String email = valueOf(m.get("email"));
            String name = valueOf(m.get("name"));
            String role = valueOf(m.get("role"));
            String idStr = valueOf(m.get("id"));

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

    private Map<?, ?> findUserByLogin(String login) throws InterruptedException, ExecutionException {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        if (firestore.isPresent()) {
            if (looksLikeEmail(login)) {
                String key = docIdForEmail(login);
                var snap = firestore.get().readDocument("auth_users", key);
                if (snap != null && snap.exists()) {
                    Object val = snap.getData();
                    if (val instanceof Map) {
                        return (Map<?, ?>) val;
                    }
                }
            }

            var byEmail = firestore.get().queryCollection("auth_users", "email", login);
            if (byEmail != null && !byEmail.isEmpty()) {
                var doc = byEmail.getDocuments().get(0);
                Object val = doc.getData();
                if (val instanceof Map) {
                    return (Map<?, ?>) val;
                }
            }

            var byName = firestore.get().queryCollection("auth_users", "name", login);
            if (byName != null && !byName.isEmpty()) {
                var doc = byName.getDocuments().get(0);
                Object val = doc.getData();
                if (val instanceof Map) {
                    return (Map<?, ?>) val;
                }
            }

            // Firestore queries are case-sensitive; fall back to a complete scan if needed.
            var allDocs = firestore.get().getAllDocuments("auth_users");
            for (var doc : allDocs) {
                Object val = doc.getData();
                if (val instanceof Map) {
                    Map<?, ?> candidate = (Map<?, ?>) val;
                    String candidateEmail = valueOf(candidate.get("email"));
                    String candidateName = valueOf(candidate.get("name"));
                    if (login.equalsIgnoreCase(candidateEmail) || login.equalsIgnoreCase(candidateName)) {
                        return candidate;
                    }
                }
            }
        } else if (rtdb.isPresent()) {
            if (looksLikeEmail(login)) {
                String key = "auth/users/" + keyForEmail(login);
                var snap = rtdb.get().readData(key).get();
                if (snap != null && snap.exists()) {
                    Object val = snap.getValue();
                    if (val instanceof Map)
                        return (Map<?, ?>) val;
                }
            }

            var allSnap = rtdb.get().readData("auth/users").get();
            if (allSnap != null && allSnap.exists()) {
                for (DataSnapshot child : allSnap.getChildren()) {
                    Object val = child.getValue();
                    if (val instanceof Map) {
                        Map<?, ?> candidate = (Map<?, ?>) val;
                        String candidateEmail = valueOf(candidate.get("email"));
                        String candidateName = valueOf(candidate.get("name"));
                        if (login.equalsIgnoreCase(candidateEmail) || login.equalsIgnoreCase(candidateName)) {
                            return candidate;
                        }
                    }
                }
            }
        } else {
            if (looksLikeEmail(login)) {
                String key = "auth/users/" + keyForEmail(login);
                if (inMemoryUsers.containsKey(key)) {
                    return inMemoryUsers.get(key);
                }
            }
            for (Map<String, Object> candidate : inMemoryUsers.values()) {
                String candidateEmail = valueOf(candidate.get("email"));
                String candidateName = valueOf(candidate.get("name"));
                if (login.equalsIgnoreCase(candidateEmail) || login.equalsIgnoreCase(candidateName)) {
                    return candidate;
                }
            }
        }

        throw new IllegalArgumentException("Invalid credentials");
    }

    private static boolean looksLikeEmail(String login) {
        return login != null && login.contains("@");
    }

    private static String docIdForEmail(String email) {
        if (email == null)
            return "";
        return java.util.Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(email.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
    }

    private static String valueOf(Object obj) {
        return obj == null ? "" : String.valueOf(obj);
    }
}
