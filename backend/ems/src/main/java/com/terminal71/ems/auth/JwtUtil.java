package com.terminal71.ems.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

    private final Algorithm algorithm;

    public JwtUtil(String secret) {
        this.algorithm = Algorithm.HMAC256(secret);
    }

    public String generateToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        var builder = JWT.create()
                .withSubject(subject)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(60 * 60 * 24)));

        if (claims != null) {
            claims.forEach((k, v) -> {
                if (v instanceof String) builder.withClaim(k, (String) v);
                else if (v instanceof Integer) builder.withClaim(k, (Integer) v);
                else if (v instanceof Long) builder.withClaim(k, (Long) v);
                else if (v instanceof Boolean) builder.withClaim(k, (Boolean) v);
                else builder.withClaim(k, String.valueOf(v));
            });
        }

        return builder.sign(algorithm);
    }
}
