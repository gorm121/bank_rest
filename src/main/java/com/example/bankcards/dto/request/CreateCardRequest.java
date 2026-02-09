package com.example.bankcards.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCardRequest {

    @NotBlank(message = "Encrypted card number is required")
    String encryptedCardNumber;

    @NotBlank(message = "Card holder is required")
    @Size(min = 2, max = 100, message = "Card holder must be between 2 and 100 characters")
    @Pattern(regexp = "^[A-Za-zА-Яа-я\\s-]+$")
    String cardHolder;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;

    @NotBlank(message = "CVV is required")
    @Size(min = 3, max = 4, message = "CVV must be 3 or 4 digits")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must contain only digits")
    String cvv;
}
