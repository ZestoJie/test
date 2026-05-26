package com.terminal71.ems.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfiguration {

  @Bean
  @ConditionalOnExpression("#{systemEnvironment['REDIS_URL'] != null and systemEnvironment['REDIS_URL'].trim().length() > 0}")
  public LettuceConnectionFactory redisConnectionFactory() {

    String redisUrl = System.getenv("REDIS_URL");

    if (redisUrl == null || redisUrl.isBlank()) {
      throw new IllegalStateException("REDIS_URL is not set");
    }

    redisUrl = redisUrl.trim();

    java.net.URI uri = java.net.URI.create(redisUrl);

    RedisStandaloneConfiguration cfg =
        new RedisStandaloneConfiguration(
            uri.getHost(),
            uri.getPort() == -1 ? 6379 : uri.getPort()
        );

    if (uri.getUserInfo() != null) {
      String[] parts = uri.getUserInfo().split(":", 2);
      if (parts.length == 2) {
        cfg.setPassword(RedisPassword.of(parts[1]));
      }
    }

    LettuceClientConfiguration clientConfig =
        "rediss".equalsIgnoreCase(uri.getScheme())
            ? LettuceClientConfiguration.builder().useSsl().build()
            : LettuceClientConfiguration.builder().build();

    return new LettuceConnectionFactory(cfg, clientConfig);
  }

  @Bean
  @ConditionalOnBean(LettuceConnectionFactory.class)
  public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory factory) {
    return new StringRedisTemplate(factory);
  }
}