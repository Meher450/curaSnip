package com.curapaste.services;


import com.curapaste.dto.CachedPaste;
import com.curapaste.dto.PasteResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Service
public class CacheService {

    private final RedisTemplate<String,String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String DIRTY_ANALYTICS_KEY =
            "paste:analytics:dirty";

    public CacheService(RedisTemplate<String,String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }


    public void set(CachedPaste paste){
       try{
           redisTemplate.opsForValue().set(
                   cacheKey(paste.getShortId()),
                   toJson(paste),
                   computeTtl(paste.getExpiresAt())
           );
       }
       catch (Exception e){
           System.out.println("CACHE SET ERROR"+e);
       }
    }

    @CircuitBreaker(
            name = "redisCache",
            fallbackMethod = "getFallback"
    )
    public Optional<CachedPaste> get(String shortId){
            String json = redisTemplate.opsForValue().get(cacheKey(shortId));
            return json == null
                    ? Optional.empty()
                    : Optional.of(fromJson(json));

    }

    private Optional<CachedPaste> getFallback(
            String shortId,
            Throwable t) {

        System.out.println(
                "Redis circuit open, bypassing cache: "
                        + t.getMessage()
        );

        return Optional.empty();
    }

    public void evict(String shortId){
        try {
            redisTemplate.delete(cacheKey(shortId));
            redisTemplate.delete(viewCountKey(shortId));
            redisTemplate.delete(lastViewedAtKey(shortId));
            redisTemplate.opsForSet()
                    .remove(DIRTY_ANALYTICS_KEY, shortId);
        } catch (Exception e) {
            System.out.println("CACHE EVICT ERROR: " + e);
        }
    }

    public void recordView(
            String shortId,
            Instant expiresAt
    ) {
        try {
            redisTemplate.opsForValue()
                    .increment(viewCountKey(shortId));

            redisTemplate.opsForValue()
                    .set(
                            lastViewedAtKey(shortId),
                            Instant.now().toString()
                    );



            redisTemplate.opsForSet()
                    .add(
                            DIRTY_ANALYTICS_KEY,
                            shortId
                    );

            // Only expiring pastes need analytics TTL.
            if (expiresAt != null) {

                Duration untilExpiry =
                        Duration.between(
                                Instant.now(),
                                expiresAt
                        );

                if (!untilExpiry.isNegative()
                        && !untilExpiry.isZero()) {

                    redisTemplate.expire(
                            viewCountKey(shortId),
                            untilExpiry
                    );

                    redisTemplate.expire(
                            lastViewedAtKey(shortId),
                            untilExpiry
                    );
                }
            }

        } catch (Exception e) {
            System.out.println(
                    "ANALYTICS CACHE UPDATE ERROR: " + e
            );
        }
    }


    public long getViewCount(String shortId) {
        try {
            String value = redisTemplate.opsForValue()
                    .get(viewCountKey(shortId));

            return value == null
                    ? 0L
                    : Long.parseLong(value);

        } catch (Exception e) {
            System.out.println(
                    "VIEW COUNT CACHE READ ERROR: " + e
            );

            return 0L;
        }
    }

    public Instant getLastViewedAt(String shortId) {
        try {
            String value = redisTemplate.opsForValue()
                    .get(lastViewedAtKey(shortId));

            return value == null
                    ? null
                    : Instant.parse(value);

        } catch (Exception e) {
            System.out.println(
                    "LAST VIEWED CACHE READ ERROR: " + e
            );

            return null;
        }
    }

    public Set<String> getDirtyPasteIds() {
        try {
            Set<String> ids =
                    redisTemplate.opsForSet()
                            .members(DIRTY_ANALYTICS_KEY);

            return ids != null
                    ? ids
                    : Collections.emptySet();

        } catch (Exception e) {
            System.out.println(
                    "DIRTY ANALYTICS READ ERROR: " + e
            );

            return Collections.emptySet();
        }
    }

    public void markAnalyticsSynced(String shortId) {
        try {
            redisTemplate.opsForSet()
                    .remove(
                            DIRTY_ANALYTICS_KEY,
                            shortId
                    );

        } catch (Exception e) {
            System.out.println(
                    "DIRTY ANALYTICS REMOVE ERROR: " + e
            );
        }
    }

    private String cacheKey(String shortId) {
        return "paste:" + shortId;
    }

    private String viewCountKey(String shortId) {
        return "paste:" + shortId + ":views";
    }

    private String lastViewedAtKey(String shortId) {
        return "paste:" + shortId + ":lastViewedAt";
    }
    private String toJson(CachedPaste paste) {
        try {
            return objectMapper.writeValueAsString(paste);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize paste", e);
        }
    }



    private CachedPaste fromJson(String json) {
        try {
            return objectMapper.readValue(json, CachedPaste.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize paste", e);
        }
    }

    private Duration computeTtl(Instant expiresAt) {

        // Paste doesn't expire.
        // Still don't keep it in Redis forever.
        if (expiresAt == null) {
            return Duration.ofHours(1);
        }

        Duration untilExpiry =
                Duration.between(Instant.now(), expiresAt);

        // Already expired.
        if (untilExpiry.isNegative()
                || untilExpiry.isZero()) {

            return Duration.ofSeconds(1);
        }

        // Never cache longer than 1 hour.
        return untilExpiry.compareTo(Duration.ofHours(1)) > 0
                ? Duration.ofHours(1)
                : untilExpiry;
    }
}
