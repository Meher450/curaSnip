package com.curapaste.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor

public class CreatePasteRequest {
    @NotBlank(message = "Content cannot be empty")
    private String content;

    @Positive(message = "Expiration time must be greater than 0")
    private Long expiresInSeconds;

    private boolean burnAfterRead = false;

    private String password;
}
