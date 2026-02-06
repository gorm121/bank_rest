package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CardDto {
    private Long id;
    private String maskedNumber;
    private String cardHolder;
    private LocalDate expiryDate;
    private BigDecimal balance;
    private Card.CardStatus status;
    private LocalDateTime createdAt;

}
