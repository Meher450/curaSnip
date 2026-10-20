package com.curapaste.config;


import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    public RedisClient rateLimitRedisClient() {
        RedisURI redisURI = RedisURI.builder()
                .withHost(redisHost)
                .withPort(redisPort)
                .build();

        return RedisClient.create(redisURI);
    }

    @Bean
    public StatefulRedisConnection<String,byte[]> rateLimitRedisConnection(
            RedisClient rateLimitRedisClient
    ) {
        RedisCodec<String,byte[]> codec = RedisCodec.of(
                StringCodec.UTF8,
                ByteArrayCodec.INSTANCE
        );

        return rateLimitRedisClient.connect(codec);

    }

    @Bean
    public ProxyManager<String> bucketProxyManager(
            StatefulRedisConnection<String,byte[]> connection
    ) {
            return Bucket4jLettuce
                    .casBasedBuilder(connection)
                    .expirationAfterWrite(
                            ExpirationAfterWriteStrategy
                                    .basedOnTimeForRefillingBucketUpToMax(
                                            Duration.ofMinutes(1)
                                    )
                    )
                    .build();

    }

    @Bean
    public BucketConfiguration pasteRateLimitConfiguration(){
        return BucketConfiguration
                .builder()
                .addLimit(limit ->
                        limit
                                .capacity(10)
                                .refillIntervally(
                                        10,Duration.ofMinutes(1)
                                ))
                .build();
    }


}
