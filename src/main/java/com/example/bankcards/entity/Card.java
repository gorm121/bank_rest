package com.example.bankcards.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "cards")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number_encrypted", nullable = false, unique = true)
    private String cardNumberEncrypted;

    @Column(name = "card_number_masked", nullable = false, length = 19)
    private String cardNumberMasked; // Формат: **** **** **** 1234

    @Column(name = "card_holder_name", nullable = false, length = 100)
    private String cardHolderName;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "cvv_encrypted", nullable = false)
    private String cvvEncrypted;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CardStatus status = CardStatus.ACTIVE;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "fromCard", fetch = FetchType.LAZY)
    private Set<Transaction> outgoingTransactions;

    @OneToMany(mappedBy = "toCard", fetch = FetchType.LAZY)
    private Set<Transaction> incomingTransactions;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        if (expirationDate.isBefore(LocalDate.now())) {
            status = CardStatus.EXPIRED;
        }
    }

    public enum CardStatus {
        ACTIVE, BLOCKED, EXPIRED
    }
}