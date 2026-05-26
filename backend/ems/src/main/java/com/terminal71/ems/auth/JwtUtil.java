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

    public com.auth0.jwt.interfaces.DecodedJWT verifyToken(String token) {
        return com.auth0.jwt.JWT.require(algorithm).build().verify(token);
    }

    public java.util.Map<String, Object> extractClaims(String token) {
        var decoded = verifyToken(token);
        var claims = new java.util.HashMap<String, Object>();
        decoded.getClaims().forEach((k, c) -> {
            try {
                Object v = null;
                if (c.asBoolean() != null) v = c.asBoolean();
                else if (c.asLong() != null) v = c.asLong();
                else if (c.asInt() != null) v = c.asInt();
                else if (c.asString() != null) v = c.asString();
                else v = c.toString();
                claims.put(k, v);
            } catch (Exception ex) {
                claims.put(k, c.toString());
            }
        });
        return claims;
    }
}
