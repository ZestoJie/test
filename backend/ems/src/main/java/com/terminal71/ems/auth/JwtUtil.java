package com.terminal71.ems.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
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
            claims.forEach((k, v) -> extracted(builder, k, v));
        }

        return builder.sign(algorithm);
    }

    private void extracted(Builder builder, String k, Object v) {
        if (v instanceof String string) builder.withClaim(k, string);
        else if (v instanceof Integer integer) builder.withClaim(k, integer);
        else if (v instanceof Long aLong) builder.withClaim(k, aLong);
        else if (v instanceof Boolean aBoolean) builder.withClaim(k, aBoolean);
        else builder.withClaim(k, String.valueOf(v));
    }
}
