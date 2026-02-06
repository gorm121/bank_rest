package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.request.UpdateCardRequest;
import com.example.bankcards.dto.MessageDto;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping
    public ResponseEntity<CardDto> createCard(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody CreateCardRequest request) {
        return ResponseEntity.status(201).body(cardService.createCard(userDetails,request));
    }

    @GetMapping
    public ResponseEntity<List<CardDto>> getAllCards(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cardService.getMyCards(userDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageDto> deleteCard(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        return ResponseEntity.status(204).body(cardService.deleteCard(userDetails, id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCard(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCardById(userDetails, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardDto> updateCard(@AuthenticationPrincipal UserDetails  userDetails, @PathVariable Long id, @Valid @RequestBody UpdateCardRequest request) {
        return ResponseEntity.ok(cardService.updateCard(userDetails, id, request));
    }

    @PostMapping("/transaction")
    public ResponseEntity<MessageDto> transfer(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.status(201).body(cardService.transfer(userDetails, request));
    }
}
