package com.jdt16.agenin.users.components.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Health health() {
        try {
            assert redisTemplate.getConnectionFactory() != null;
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();

            return Health.up()
                    .withDetail("redis", "Available")
                    .withDetail("ping", pong)
                    .build();

        } catch (Exception e) {
            log.error("Redis health check failed", e);
            return Health.down()
                    .withDetail("redis", "Unavailable")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
