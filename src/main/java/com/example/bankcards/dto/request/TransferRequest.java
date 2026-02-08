package com.example.bankcards.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferRequest {
    @NotNull(message = "From card ID is required")
    Long fromCardId;

    @NotNull(message = "To card ID is required")
    Long toCardId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @DecimalMin(value = "0.01", message = "Minimum amount is 0.01")
    BigDecimal amount;
}
