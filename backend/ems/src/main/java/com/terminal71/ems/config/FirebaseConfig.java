package com.terminal71.ems.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @PostConstruct
    public void initialize() {
        try {
            // Prefer service account provided via env var so credentials are not stored in the repo.
            // Support two options:
            // 1) FIREBASE_SERVICE_ACCOUNT — either raw JSON text or a filesystem path
            // 2) FIREBASE_SERVICE_ACCOUNT_B64 — a base64-encoded JSON string (single-line, safe for CLI)
            String svcPath = System.getenv("FIREBASE_SERVICE_ACCOUNT");
            String svcB64 = System.getenv("FIREBASE_SERVICE_ACCOUNT_B64");
            InputStream serviceAccount = null;

            // If a base64 secret is provided, decode it first (useful for CLI single-line secrets)
            if (svcB64 != null && !svcB64.isBlank()) {
                try {
                    byte[] decoded = java.util.Base64.getDecoder().decode(svcB64.trim());
                    serviceAccount = new java.io.ByteArrayInputStream(decoded);
                    log.info("Loading Firebase service account from FIREBASE_SERVICE_ACCOUNT_B64 env (decoded)");
                } catch (Exception e) {
                    log.warn("Failed to decode FIREBASE_SERVICE_ACCOUNT_B64", e);
                }
            }

            if (serviceAccount == null && svcPath != null && !svcPath.isBlank()) {
                try {
                    // If the env var contains JSON (secret manager provided value), use it directly
                    if (svcPath.trim().startsWith("{")) {
                        serviceAccount = new java.io.ByteArrayInputStream(svcPath.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        log.info("Loading Firebase service account from FIREBASE_SERVICE_ACCOUNT env JSON value");
                    } else {
                        serviceAccount = new java.io.FileInputStream(svcPath);
                        log.info("Loading Firebase service account from FIREBASE_SERVICE_ACCOUNT env path");
                    }
                } catch (FileNotFoundException e) {
                    log.warn("Failed to open service account from FIREBASE_SERVICE_ACCOUNT env var, will try classpath", e);
                }
            }

            if (serviceAccount == null) {
                // Try known filename(s) on classpath. Accept whichever is present.
                String[] candidateFiles = new String[] {
                    "terminal71-ems-firebase-adminsdk-fbsvc-284681596a.json",
                    "terminal71-ems-firebase-adminsdk-fbsvc-87187eda10.json"
                };
                ClassPathResource res = null;
                for (String f : candidateFiles) {
                    ClassPathResource r = new ClassPathResource(f);
                    if (r.exists()) {
                        res = r;
                        break;
                    }
                }
                if (res == null) {
                    log.warn("Firebase service account not found on classpath; skipping Firebase initialization");
                    return;
                }
                serviceAccount = res.getInputStream();
            }

            try (InputStream in = serviceAccount) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(in))
                        .setDatabaseUrl("https://terminal71-ems-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    log.info("Firebase initialized with Realtime Database");
                }
            }

        } catch (IOException e) {
            log.warn("Failed to initialize Firebase SDK, continuing without it", e);
        }
    }

    @Bean
    @ConditionalOnClass(FirebaseApp.class)
    public com.google.firebase.FirebaseApp firebaseAppBean() {
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("No FirebaseApp instances present; firebaseApp bean will not be created");
            return null;
        }
        return FirebaseApp.getInstance();
    }

    @Bean
    public FirebaseDatabase firebaseDatabase() {
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("No FirebaseApp initialized; not creating FirebaseDatabase bean");
            return null;
        }
        return FirebaseDatabase.getInstance();
    }
}