package com.jdt16.agenin.users.configuration.redis;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith("agenin::")
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer())
                );

        // Konfigurasi khusus per cache
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // User data - cache lebih lama (30 menit)
        cacheConfigurations.put("users",
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Product list - cache 1 jam
        cacheConfigurations.put("products",
                defaultConfig.entryTtl(Duration.ofHours(1)));

        // Referral code - cache 20 menit
        cacheConfigurations.put("referralCodes",
                defaultConfig.entryTtl(Duration.ofMinutes(20)));

        // Commission values - cache 1 jam
        cacheConfigurations.put("commissions",
                defaultConfig.entryTtl(Duration.ofHours(1)));

        // User balance - cache singkat (2 menit)
        cacheConfigurations.put("userBalance",
                defaultConfig.entryTtl(Duration.ofMinutes(2)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
