package com.terminal71.ems.config;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @PostConstruct
    public void initialize() {
        try {
            // Prefer service account path provided via env var so credentials are not stored in the repo.
            String svcPath = System.getenv("FIREBASE_SERVICE_ACCOUNT");
            InputStream serviceAccount = null;

            if (svcPath != null && !svcPath.isBlank()) {
                try {
                    serviceAccount = new java.io.FileInputStream(svcPath);
                    log.info("Loading Firebase service account from env path");
                } catch (Exception e) {
                    log.warn("Failed to open service account from env path, will try classpath", e);
                }
            }

            if (serviceAccount == null) {
                ClassPathResource res = new ClassPathResource("terminal71-ems-firebase-adminsdk-fbsvc-284681596a.json");
                if (!res.exists()) {
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

        } catch (Exception e) {
            log.warn("Failed to initialize Firebase SDK, continuing without it", e);
        }
    }

    @Bean
    @ConditionalOnBean(FirebaseApp.class)
    public FirebaseDatabase firebaseDatabase() {
        return FirebaseDatabase.getInstance();
    }
}

