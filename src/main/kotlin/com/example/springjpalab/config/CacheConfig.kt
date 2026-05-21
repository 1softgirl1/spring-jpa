package com.example.springjpalab.config

import tools.jackson.databind.DeserializationFeature
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import java.time.Duration

@Configuration
class CacheConfig {

    @Bean
    fun redisCacheManagerBuilderCustomizer(): RedisCacheManagerBuilderCustomizer {
        val serializer = GenericJacksonJsonRedisSerializer.builder()
            .enableUnsafeDefaultTyping()
            .customize { mapper ->
                mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
            }
            .build()

        val serializationPair =
            RedisSerializationContext.SerializationPair.fromSerializer(serializer)

        fun config(ttl: Duration) = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(ttl)
            .serializeValuesWith(serializationPair)
            .disableCachingNullValues()

        return RedisCacheManagerBuilderCustomizer { builder ->
            builder
                .withCacheConfiguration("restaurants", config(Duration.ofHours(1)))
                .withCacheConfiguration("dishes", config(Duration.ofHours(1)))
                .cacheDefaults(config(Duration.ofMinutes(5)))
        }
    }
}
