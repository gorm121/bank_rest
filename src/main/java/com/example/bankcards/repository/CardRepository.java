package com.example.bankcards.repository;


import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card,Long> {

    boolean existsByCardHash(String attr0);

    List<Card> findAllByUser(User user);

    boolean existsByIdAndUserId(Long id, Long id1);
}
