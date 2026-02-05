package com.example.bankcards.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateCardRequest {

    @NotBlank(message = "Encrypted card number is required")
    private String encryptedCardNumber;

    @NotBlank(message = "Card holder is required")
    @Size(min = 2, max = 100, message = "Card holder must be between 2 and 100 characters")
    @Pattern(regexp = "^[A-Za-zА-Яа-я\\s-]+$")
    private String cardHolder;

    @NotBlank(message = "Expiry date is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Expiry date must be in MM/YY format")
    private LocalDate expiryDate;

    @NotBlank(message = "CVV is required")
    @Size(min = 3, max = 4, message = "CVV must be 3 or 4 digits")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must contain only digits")
    private String cvv;
}
