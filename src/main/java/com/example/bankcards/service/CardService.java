package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.request.UpdateCardRequest;
import com.example.bankcards.dto.MessageDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.enums.CardStatus;
import com.example.bankcards.enums.TransactionStatus;
import com.example.bankcards.enums.TransactionType;
import com.example.bankcards.enums.UserRole;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.EntityNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.InvalidDataException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final RsaService rsaService;
    private final CardValidator cardValidator;
    private final TransactionRepository transactionRepository;

    @Value("${app.card.hash.salt}")
    private String cardHashSalt;

    @Value("${app.card.hash.algorithm:SHA-256}")
    private String hashAlgorithm;


    @Transactional
    public CardDto createCard(UserDetails userDetails, CreateCardRequest request) {
        User user = getCurrentUser(userDetails);

        String decryptedCardNumber = rsaService.decrypt(request.getEncryptedCardNumber());
        String cleanCardNumber = decryptedCardNumber.replaceAll("\\s", "");

        if (!isValidCardNumber(cleanCardNumber)) {
            throw new InvalidDataException("Invalid card number");
        }

        String cardHash = generateCardHash(cleanCardNumber);
        String lastFourDigits = cleanCardNumber.substring(cleanCardNumber.length() - 4);

        if (cardRepository.existsByCardHash(cardHash)) {
            throw new InvalidDataException("Card already exists");
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

        cardRepository.save(card);
        return mapToDto(card);
    }

    @Transactional
    public MessageDto transfer(UserDetails userDetails, TransferRequest request) {
        User user = getCurrentUser(userDetails);
        Long fromCardId = request.getFromCardId();
        Long toCardId = request.getToCardId();
        Long userId = user.getId();

        boolean hasAccess = cardRepository.existsByIdAndUserId(fromCardId, userId)
                && cardRepository.existsByIdAndUserId(toCardId, userId);

        if (!hasAccess) {
            throw new InsufficientFundsException("Access denied");
        }

        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));
        Card toCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));


        BigDecimal amount = request.getAmount();
        BigDecimal remaining = fromCard.getBalance().subtract(amount);

        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException("Not enough money");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        Transaction transaction = new Transaction();
        transaction.setFromCard(fromCard);
        transaction.setToCard(toCard);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setDescription(String.format("Transfer from %s to %s, amount %s", fromCard.getCardHolder(), toCard.getCardHolder(), amount.toString()));

        fromCard.addOutgoingTransaction(transaction);
        toCard.addIncomingTransaction(transaction);

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        transactionRepository.save(transaction);

        log.info("Transfer completed: fromCardId={}, toCardId={}, amount={}",
                fromCardId, toCardId, amount);


        return new MessageDto("Transfer successfully");
    }

    public MessageDto requestBlockCard(UserDetails userDetails, Long cardId) {
        User user = getCurrentUser(userDetails);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        if (!card.getUser().equals(user)) {
            throw new AccessDeniedException("Not your card");
        }
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new InvalidDataException("Card is already blocked");
        }

        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new InvalidDataException("Card is expired");
        }


        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);

        log.info("Card blocked by user: cardId={}, userId={}", cardId, user.getId());

        return new MessageDto("Card blocked successfully");
    }



    //-------------------------------
    //Admins methods

    public Page<CardDto> getAllCards(Long userId, CardStatus status, Pageable pageable) {
        Page<Card> cards;

        if (userId != null && status != null) {
            cards = cardRepository.findByUserIdAndStatus(userId, status, pageable);
        } else if (userId != null) {
            cards = cardRepository.findByUserId(userId, pageable);
        } else if (status != null) {
            cards = cardRepository.findByStatus(status, pageable);
        } else {
            cards = cardRepository.findAll(pageable);
        }

        return cards.map(this::mapToDto);
    }

    public CardDto blockCard(@PathVariable Long cardId){
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
        return mapToDto(card);
    }

    public CardDto activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
        return mapToDto(card);
    }

    public MessageDto deleteCard(Long cardId){
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));
        cardRepository.delete(card);
        return new MessageDto("Card deleted successfully");
    }

    public List<TransactionDto> getCardTransactions(Long cardId){
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));
        return Stream.concat(
                        card.getIncomingTransactions().stream(),
                        card.getOutgoingTransactions().stream()
                )
                .distinct()
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .map(this::mapToDto)
                .toList();
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
        return cardValidator.luhnCheck(cardNumber);
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

    private TransactionDto mapToDto(Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setTransactionId(transaction.getTransactionId());
        dto.setToCard(transaction.getToCard());
        dto.setFromCard(transaction.getFromCard());
        dto.setDescription(transaction.getDescription());
        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setStatus(transaction.getStatus());
        return dto;
    }
}
