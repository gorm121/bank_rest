package com.example.bankcards.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateCardRequest {

    @NotBlank(message = "Card holder is required")
    @Size(min = 2, max = 100, message = "Card holder must be between 2 and 100 characters")
    @Pattern(regexp = "^[A-Za-zА-Яа-я\\s-]+$")
    private String cardHolder;

}
