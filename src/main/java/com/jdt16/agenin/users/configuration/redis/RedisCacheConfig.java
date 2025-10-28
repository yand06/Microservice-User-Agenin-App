package com.jdt16.agenin.users.configuration.redis;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jdt16.agenin.users.dto.response.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableCaching
public class RedisCacheConfig {

    // ObjectMapper TANPA default typing
    @Bean
    @Primary
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // TIDAK ada activateDefaultTyping() - sehingga TIDAK ada @class

        log.info("Redis ObjectMapper configured WITHOUT default typing");
        return mapper;
    }

    // RedisTemplate untuk operasi manual
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(redisObjectMapper, Object.class);

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();

        log.info("RedisTemplate configured successfully");
        return template;
    }

    // CacheManager dengan serializer per cache
    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper) {

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Cache untuk Users - UserResponse
        cacheConfigurations.put("users",
                createRestApiResponseCacheConfig(UserResponse.class, redisObjectMapper, Duration.ofMinutes(30))
        );

        // Cache untuk Referral Codes - ReferralCodeResponse
        cacheConfigurations.put("referralCodes",
                createRestApiResponseCacheConfig(UserReferralCodeResponse.class, redisObjectMapper, Duration.ofMinutes(20))
        );

        // Default configuration (fallback)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith("agenin::")
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues();

        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();

        log.info("RedisCacheManager configured with {} specific caches", cacheConfigurations.size());

        return cacheManager;
    }

    private <T> RedisCacheConfiguration createRestApiResponseCacheConfig(
            Class<T> dataType,
            ObjectMapper objectMapper,
            Duration ttl) {

        JavaType wrapperType = objectMapper.getTypeFactory()
                .constructParametricType(RestApiResponse.class, dataType);

        Jackson2JsonRedisSerializer<RestApiResponse<T>> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, wrapperType);

        return RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith("agenin::")
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(serializer)
                );
    }
}
