package com.curapaste.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DeletePasteRequest {
    @NotBlank(message = "Delete token cannot be empty")
    private String deleteToken;
}
