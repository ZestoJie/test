package com.terminal71.ems.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfiguration {

  @Bean
  public LettuceConnectionFactory redisConnectionFactory() {
    String redisUrl = System.getenv().getOrDefault("REDIS_URL", "").trim();
    if (redisUrl.isBlank()) {
      return null;
    }
    try {
      // Expect formats like redis://[:password@]host:port or redis://host:port
      java.net.URI uri = new java.net.URI(redisUrl);
      String host = uri.getHost();
      int port = uri.getPort() == -1 ? 6379 : uri.getPort();
      String userInfo = uri.getUserInfo();
      String password = null;
      if (userInfo != null && userInfo.contains(":")) {
        password = userInfo.split(":", 2)[1];
      } else if (userInfo != null) {
        password = userInfo;
      }
      RedisStandaloneConfiguration cfg = new RedisStandaloneConfiguration(host, port);
      if (password != null && !password.isBlank()) {
        cfg.setPassword(RedisPassword.of(password));
      }
      return new LettuceConnectionFactory(cfg);
    } catch (Exception e) {
      org.slf4j.LoggerFactory.getLogger(RedisConfiguration.class).warn("Failed to parse REDIS_URL, skipping Redis autoconfig", e);
      return null;
    }
  }

  @Bean
  public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory factory) {
    if (factory == null) return null;
    var t = new StringRedisTemplate();
    t.setConnectionFactory(factory);
    return t;
  }
}
