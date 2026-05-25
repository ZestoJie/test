package com.terminal71.ems.controller;

import com.terminal71.ems.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        String name = body.get("name");
        String role = body.get("role");
        log.info("Register endpoint hit for email={}", email);
        try {
            var user = authService.register(email, password, name, role);
            return ResponseEntity.ok(Map.of("user", user));
        } catch (Exception e) {
            log.error("Registration failed for email={}", email, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        log.info("Login endpoint hit for email={}", email);
        try {
            var resp = authService.login(email, password);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Login failed for email={}", email, e);
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}
