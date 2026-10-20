package com.curapaste.config.storage;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@Setter
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String region;

    private int inlineThresholdBytes = 50 * 1024;
}
