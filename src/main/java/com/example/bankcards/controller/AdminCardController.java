package com.example.bankcards.controller;


import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.MessageDto;
import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.enums.CardStatus;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<Page<CardDto>> getAllCards(@RequestParam(required = false) Long userId,
                                                     @RequestParam(required = false) CardStatus status,
                                                     Pageable pageable) {
        return ResponseEntity.ok(cardService.getAllCards(userId, status, pageable));
    }

    @PostMapping("/{cardId}/block")
    public ResponseEntity<CardDto> blockCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.blockCard(cardId));
    }

    @PostMapping("/{cardId}/activate")
    public ResponseEntity<CardDto> activateCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.activateCard(cardId));
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<MessageDto> deleteCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.deleteCard(cardId));
    }

    @GetMapping("/{cardId}/transactions")
    public ResponseEntity<List<TransactionDto>> getCardTransactions(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.getCardTransactions(cardId));
    }

}
