package com.terminal71.ems.security;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Simple in-memory rate limiter per remote IP. Not suitable for multi-instance production
 * without a shared store (Redis). Limits requests per window (minutes).
 */
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final Map<String, Deque<Long>> requests = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final long windowMillis;

    public RateLimitingFilter(int maxRequestsPerMinute) {
        this.maxRequests = maxRequestsPerMinute;
        this.windowMillis = 60_000L; // 1 minute
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        long now = Instant.now().toEpochMilli();

        Deque<Long> deque = requests.computeIfAbsent(ip, k -> new ArrayDeque<>());
        synchronized (deque) {
            while (!deque.isEmpty() && deque.peekFirst() < now - windowMillis) {
                deque.pollFirst();
            }
            if (deque.size() >= maxRequests) {
                log.warn("Rate limit exceeded for {} ({} req/min)", ip, maxRequests);
                response.setStatus(429);
                response.setHeader("Retry-After", "60");
                response.getWriter().write("Too many requests");
                return;
            }
            deque.addLast(now);
        }

        filterChain.doFilter(request, response);
    }
}
