package com.curapaste.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PasteMetadataResponse {
    String shortId;
    Instant createdAt;
    Instant expiresAt;
    boolean burnAfterRead;
    Integer sizeBytes;
    long viewCount;
    Instant lastViewedAt;
}
