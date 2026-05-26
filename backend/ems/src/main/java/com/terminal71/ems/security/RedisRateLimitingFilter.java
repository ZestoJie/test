package com.terminal71.ems.security;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Simple Redis-backed rate limiter using per-IP counters with expiry.
 * Requires `StringRedisTemplate` bean to be configured (Spring Boot Redis starter handles this).
 */
public class RedisRateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimitingFilter.class);

    private final StringRedisTemplate redis;
    private final int maxRequests;
    private final Duration window;

    public RedisRateLimitingFilter(StringRedisTemplate redis, int maxRequestsPerWindow, Duration window) {
        this.redis = redis;
        this.maxRequests = maxRequestsPerWindow;
        this.window = window;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        String key = "rl:" + ip + ":" + (System.currentTimeMillis() / window.toMillis());

        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, window);
        }

        if (count != null && count > maxRequests) {
            log.warn("Redis rate limit exceeded for {} -> {} req per {}ms", ip, maxRequests, window.toMillis());
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(window.toSeconds()));
            response.getWriter().write("Too many requests");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
