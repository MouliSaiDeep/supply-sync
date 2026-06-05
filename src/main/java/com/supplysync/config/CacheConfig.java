package com.supplysync.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    private GenericJackson2JsonRedisSerializer jacksonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)) // default TTL fallback
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jacksonSerializer()));
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> {
            Map<String, RedisCacheConfiguration> configurationMap = new HashMap<>();
            GenericJackson2JsonRedisSerializer serializer = jacksonSerializer();

            configurationMap.put("products", RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(10))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer)));

            configurationMap.put("categories", RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(30))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer)));

            configurationMap.put("warehouses", RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(15))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer)));

            configurationMap.put("inventory:low-stock", RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(5))
                    .disableCachingNullValues()
                    .computePrefixWith(cacheName -> cacheName)
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer)));

            configurationMap.put("suppliers", RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(20))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer)));

            configurationMap.put("reports:dashboard", RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(10))
                    .computePrefixWith(cacheName -> cacheName)
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer)));

            builder.withInitialCacheConfigurations(configurationMap);
        };
    }
}
