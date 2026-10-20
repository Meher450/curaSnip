package com.curapaste.config.storage;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class S3Config {

    @Bean
    public S3Client s3Client(StorageProperties storageProperties) {
        return S3Client.builder()
                .endpointOverride(URI.create(storageProperties.getEndpoint()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        storageProperties.getAccessKey(),
                                        storageProperties.getSecretKey()
                                )
                        )
                )
                .region(Region.of(storageProperties.getRegion()))
                .forcePathStyle(true)
                .build();

    }
}
