package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository  extends JpaRepository<Transaction, Long> {
    @Query("SELECT t FROM Transaction t WHERE t.toCard.id = :cardId OR t.fromCard.id = :cardId")
    List<Transaction> findAllByCardId(@Param("cardId") Long cardId);
}
