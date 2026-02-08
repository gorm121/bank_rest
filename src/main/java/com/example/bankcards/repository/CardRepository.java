package com.example.bankcards.repository;


import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card,Long> {

    boolean existsByCardHash(String attr0);

    List<Card> findAllByUser(User user);

    boolean existsByIdAndUserId(Long id, Long id1);

    Page<Card> findByUserIdAndStatus(Long userId, CardStatus status, Pageable attr0);

    Page<Card> findByUserId(Long userId, Pageable attr0);

    Page<Card> findByStatus(CardStatus status, Pageable attr0);
}
