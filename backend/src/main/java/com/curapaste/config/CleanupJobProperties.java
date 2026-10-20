package com.curapaste.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "cleanup.expiry")
public class CleanupJobProperties {
    private long intervalMs = 300000;
    private int batchSize = 500;
}
