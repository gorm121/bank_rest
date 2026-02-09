package com.example.bankcards.entity;


import com.example.bankcards.enums.CardStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "cards")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_hash", nullable = false, unique = true)
    private String cardHash;

    @Column(name = "last_four_digits", nullable = false, length = 19)
    private String lastFourDigits; // Формат: **** **** **** 1234

    @Column(name = "card_holder", nullable = false, length = 100)
    private String cardHolder;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Builder.Default
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CardStatus status = CardStatus.ACTIVE;

    @Builder.Default
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @CreatedDate
    @Column(name = "created_at", columnDefinition = "DATE")
    private LocalDate createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "DATE")
    private LocalDate updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "fromCard", fetch = FetchType.LAZY)
    private List<Transaction> outgoingTransactions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "toCard", fetch = FetchType.LAZY)
    private List<Transaction> incomingTransactions = new ArrayList<>();

    public void addOutgoingTransaction(Transaction transaction) {
        outgoingTransactions.add(transaction);
    }

    public void addIncomingTransaction(Transaction transaction) {
        incomingTransactions.add(transaction);
    }
}