package com.example.bankcards.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TransferRequest {
    @NotNull(message = "From card ID is required")
    private Long fromCardId;

    @NotNull(message = "To card ID is required")
    private Long toCardId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @DecimalMin(value = "0.01", message = "Minimum amount is 0.01")
    private BigDecimal amount;
}
