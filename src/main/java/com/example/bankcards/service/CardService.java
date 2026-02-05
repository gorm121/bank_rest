package com.example.bankcards.service;

import com.example.bankcards.dto.request.CardDto;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.request.UpdateCardRequest;
import com.example.bankcards.dto.response.MessageDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.enums.UserRole;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.EntityNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final RsaService rsaService;
    private final CardValidator cardValidator;

    @Value("${app.card.hash.salt}")
    private String cardHashSalt;

    @Value("${app.card.hash.algorithm:SHA-256}")
    private String hashAlgorithm;


    public CardDto createCard(UserDetails userDetails, CreateCardRequest request) {
        User user = getCurrentUser(userDetails);

        try {
            String decryptedCardNumber = rsaService.decrypt(request.getEncryptedCardNumber());

            String cleanCardNumber = decryptedCardNumber.replaceAll("\\s", "");

            if (!isValidCardNumber(cleanCardNumber)) {
                throw new BadRequestException("Invalid card number");
            }

            String cardHash = generateCardHash(cleanCardNumber);
            String lastFourDigits = cleanCardNumber.substring(cleanCardNumber.length() - 4);

            if (cardRepository.existsByCardHash(cardHash)) {
                throw new BadRequestException("Card already exists");
            }

            String normalizedHolder = request.getCardHolder()
                    .trim()
                    .replaceAll("\\s+", " ")
                    .toUpperCase();

            Card card = new Card();
            card.setCardHash(cardHash);
            card.setLastFourDigits(lastFourDigits);
            card.setCardHolder(normalizedHolder);
            card.setExpiryDate(request.getExpiryDate());
            card.setUser(user);

            Card savedCard = cardRepository.save(card);

            return mapToDto(savedCard);

        } catch (Exception e) {
            log.error("Failed to create card", e);
            throw new ServiceException("Failed to process card", e);
        }
    }

    public List<CardDto> getMyCards(UserDetails userDetails) {
        User user = getCurrentUser(userDetails);

        List<Card> cards = cardRepository.findAllByUser(user);

        return cards.stream().map(this::mapToDto).toList();

    }

    public MessageDto deleteCard(UserDetails userDetails, Long id) {
        User user = getCurrentUser(userDetails);
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        if (!hasAccess(card, user)) {
            throw new AccessDeniedException("Access denied");
        }
        cardRepository.delete(card);

        return new MessageDto("Card deleted successfully");
    }

    public CardDto getCardById(UserDetails userDetails, Long id) {
        User user = getCurrentUser(userDetails);
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        if (!hasAccess(card, user)) {
            throw new AccessDeniedException("Access denied");
        }

        return mapToDto(card);
    }

    public CardDto updateCard(UserDetails userDetails, Long id, UpdateCardRequest request) {
        User user = getCurrentUser(userDetails);
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        if (!hasAccess(card, user)) {
            throw new AccessDeniedException("Access denied");
        }

        String normalizedHolder = request.getCardHolder()
                .trim()
                .replaceAll("\\s+", " ")
                .toUpperCase();

        card.setCardHolder(normalizedHolder);
        card.setUpdatedAt(LocalDateTime.now());

        cardRepository.save(card);
        return mapToDto(card);
    }

    public MessageDto transfer(UserDetails userDetails, TransferRequest request) {
        User user = getCurrentUser(userDetails);
        Long fromCardId = request.getFromCardId();
        Long toCardId = request.getToCardId();
        Long userId = user.getId();

        boolean hasAccess = cardRepository.existsByIdAndUserId(fromCardId, userId)
                && cardRepository.existsByIdAndUserId(toCardId, userId);

        if (!hasAccess) {
//            throw new BadRequestException("Bad request");
        }

        BigDecimal amount = request.getAmount();
        return null;
    }




    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Нужно войти в систему");
        }
        String username = userDetails.getUsername();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private boolean hasAccess(Card card, User user) {
        return card.getUser().equals(user) || user.getRole() == UserRole.ADMIN;
    }

    private boolean hasAccessToTransfer(User user, Card fromCard, Card toCard) {
        Long userId = user.getId();
        return fromCard.getUser().getId().equals(userId)
                && toCard.getUser().getId().equals(userId);
    }

    private String generateCardHash(String cardNumber) {
        try {
            MessageDigest digest = MessageDigest.getInstance(hashAlgorithm);
            String data = cardNumber + cardHashSalt;
            byte[] hash = digest.digest(data.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private boolean isValidCardNumber(String cardNumber) {
        return CardValidator.luhnCheck(cardNumber);
    }

    private CardDto mapToDto(Card card) {
        CardDto dto = new CardDto();
        dto.setId(card.getId());
        dto.setMaskedNumber("**** **** **** " + card.getLastFourDigits());
        dto.setCardHolder(card.getCardHolder());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setBalance(card.getBalance());
        dto.setStatus(card.getStatus());
        dto.setCreatedAt(card.getCreatedAt());
        return dto;
    }
}
