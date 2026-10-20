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
public class CachedPaste {
    private String shortId;
    private String content;
    private Instant createdAt;
    private Integer sizeBytes;
    private Instant expiresAt;
    private boolean burnAfterRead;
    private String passwordHash;
    private String contentLocation;

}
