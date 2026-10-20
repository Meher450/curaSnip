package com.curapaste.services.storage;


import com.curapaste.config.storage.StorageProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Service
public class S3ContentStorageService implements ContentStorageService{
    private final S3Client s3Client;
    private final StorageProperties storageProperties;

    public S3ContentStorageService(
            S3Client s3Client,
            StorageProperties storageProperties
    ) {
        this.s3Client = s3Client;
        this.storageProperties = storageProperties;
    }

    @Override
    @CircuitBreaker(
            name = "objectStorage",
            fallbackMethod = "storeFallback"
    )
    public String store(String shortId,String content) {
        String key = buildKey(shortId);

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(storageProperties.getBucket())
                        .key(key)
                        .contentType("text/plain")
                        .build(),
                RequestBody.fromString(content, StandardCharsets.UTF_8)
        );

        return key;
    }

    private String storeFallback(
            String shortId,
            String content,
            Throwable t) {

        System.out.println(
                "Object storage unavailable while storing paste: "
                        + t.getMessage()
        );

        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Content storage is temporarily unavailable. Please try again later."
        );
    }

    @Override
    public String fetch(String location) {
       return s3Client.getObjectAsBytes(
               GetObjectRequest.builder()
                       .bucket(storageProperties.getBucket())
                       .key(location)
                       .build()
       ).asUtf8String();
    }

    @Override
    @CircuitBreaker(
            name = "objectStorage",
            fallbackMethod = "fetchFallback"
    )
    @Retry(name = "objectStorage")
    public void delete(String location) {

        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(storageProperties.getBucket())
                        .key(location)
                        .build()
        );
    }

    private String fetchFallback(
            String location,
            Throwable t) {

        System.out.println(
                "Object storage unavailable: "
                        + t.getMessage()
        );

        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Storage temporarily unavailable"
        );
    }

    private String buildKey(String shortId) {

        LocalDate today = LocalDate.now();

        return "%d/%02d/%02d/%s.txt".formatted(
                today.getYear(),
                today.getMonthValue(),
                today.getDayOfMonth(),
                shortId
        );
    }

}
