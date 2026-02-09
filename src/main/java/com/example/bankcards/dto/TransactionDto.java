package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.enums.TransactionStatus;
import com.example.bankcards.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionDto {
    Long id;
    UUID transactionId = UUID.randomUUID();
    BigDecimal amount;
    String description;
    TransactionStatus status = TransactionStatus.COMPLETED;
    TransactionType type;
    LocalDateTime createdAt;
    Card fromCard;
    Card toCard;
}
