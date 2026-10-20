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
public class CreatePasteResponse {
    private String shortId;
    private String content;
    private Instant createdAt;
    private String deleteToken;
}
