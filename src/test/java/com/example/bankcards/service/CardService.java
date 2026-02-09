package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.MessageDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.enums.CardStatus;
import com.example.bankcards.enums.UserRole;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.EntityNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.InvalidDataException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private RsaService rsaService;

    @Mock
    private CardValidator cardValidator;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private CardService cardService;

    private User user;
    private Card card1;
    private Card card2;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(cardService, "hashAlgorithm", "SHA-256");
        ReflectionTestUtils.setField(cardService, "cardHashSalt", "7oaOj1g1AfLbcRJlRhXQbAtYF3Slsqjz");

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .updatedAt(LocalDate.now())
                .createdAt(LocalDate.now())
                .role(UserRole.USER)
                .enabled(true)
                .build();

        card1 = Card.builder()
                .id(1L)
                .cardHash("hash1")
                .lastFourDigits("1234")
                .cardHolder("JOHN DOE")
                .expiryDate(LocalDate.now().plusYears(2))
                .balance(new BigDecimal("1000.00"))
                .status(CardStatus.ACTIVE)
                .user(user)
                .updatedAt(LocalDate.now())
                .createdAt(LocalDate.now())
                .build();

        card2 = Card.builder()
                .id(2L)
                .cardHash("hash2")
                .lastFourDigits("5678")
                .cardHolder("JANE DOE")
                .expiryDate(LocalDate.now().plusYears(1))
                .balance(new BigDecimal("500.00"))
                .status(CardStatus.ACTIVE)
                .user(user)
                .createdAt(LocalDate.now())
                .build();
    }

    @Test
    void createCard_Success() {
        CreateCardRequest request = CreateCardRequest.builder()
                .encryptedCardNumber("encryptedCardNumber")
                .cardHolder("John Doe")
                .expiryDate(LocalDate.now().plusYears(2))
                .build();

        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(rsaService.decrypt(anyString())).thenReturn("4111111111111111");
        when(cardValidator.luhnCheck(anyString())).thenReturn(true);
        when(cardRepository.existsByCardHash(anyString())).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenReturn(card1);

        CardDto result = cardService.createCard(userDetails, request);

        assertNotNull(result);
        assertEquals("**** **** **** 1234", result.getMaskedNumber());
        assertEquals("JOHN DOE", result.getCardHolder());

        verify(rsaService, times(1)).decrypt(anyString());
        verify(cardValidator, times(1)).luhnCheck(anyString());
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void createCard_InvalidCardNumber_ThrowsException() {
        CreateCardRequest request = CreateCardRequest.builder()
                .encryptedCardNumber("encryptedCardNumber")
                .build();

        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(rsaService.decrypt(anyString())).thenReturn("invalidCardNumber");
        when(cardValidator.luhnCheck(anyString())).thenReturn(false);

        assertThrows(InvalidDataException.class, () -> {
            cardService.createCard(userDetails, request);
        });

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void getMyCards_Success() {
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(cardRepository.findAllByUser(any(User.class))).thenReturn(Arrays.asList(card1, card2));

        List<CardDto> result = cardService.getMyCards(userDetails);


        assertNotNull(result);
        assertEquals(2, result.size());
        verify(cardRepository, times(1)).findAllByUser(any(User.class));
    }

    @Test
    void deleteCard_Success() {
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(cardRepository.findById(anyLong())).thenReturn(Optional.of(card1));

        MessageDto result = cardService.deleteCard(userDetails, 1L);

        assertNotNull(result);
        assertEquals("Card deleted successfully", result.getMessage());
        verify(cardRepository, times(1)).delete(any(Card.class));
    }

    @Test
    void deleteCard_CardNotFound_ThrowsException() {
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(cardRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            cardService.deleteCard(userDetails, 1L);
        });

        verify(cardRepository, never()).delete(any(Card.class));
    }

    @Test
    void deleteCard_AccessDenied_ThrowsException() {
        User anotherUser = User.builder()
                .id(2L)
                .username("anotheruser")
                .build();
        card1.setUser(anotherUser);

        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(cardRepository.findById(anyLong())).thenReturn(Optional.of(card1));

        assertThrows(AccessDeniedException.class, () -> {
            cardService.deleteCard(userDetails, 1L);
        });

        verify(cardRepository, never()).delete(any(Card.class));
    }

    @Test
    void transfer_Success() {
        TransferRequest request = TransferRequest.builder()
                .fromCardId(1L)
                .toCardId(2L)
                .amount(new BigDecimal("100.00"))
                .build();

        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(cardRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);
        when(cardRepository.existsByIdAndUserId(2L, 1L)).thenReturn(true);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(card2));

        MessageDto result = cardService.transfer(userDetails, request);

        assertNotNull(result);
        assertEquals("Transfer successfully", result.getMessage());

        assertEquals(new BigDecimal("900.00"), card1.getBalance());
        assertEquals(new BigDecimal("600.00"), card2.getBalance());

        verify(transactionRepository, times(1)).save(any());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transfer_InsufficientFunds_ThrowsException() {
        TransferRequest request = TransferRequest.builder()
                .fromCardId(1L)
                .toCardId(2L)
                .amount(new BigDecimal("1500.00"))
                .build();

        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(cardRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);
        when(cardRepository.existsByIdAndUserId(2L, 1L)).thenReturn(true);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(card2));

        assertThrows(InsufficientFundsException.class, () -> {
            cardService.transfer(userDetails, request);
        });

        verify(transactionRepository, never()).save(any());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void requestBlockCard_Success() {
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(cardRepository.findById(anyLong())).thenReturn(Optional.of(card1));

        MessageDto result = cardService.requestBlockCard(userDetails, 1L);

        assertNotNull(result);
        assertEquals("Card blocked successfully", result.getMessage());
        assertEquals(CardStatus.BLOCKED, card1.getStatus());
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void getAllCards_WithFilters() {
        Page<Card> cardPage = new PageImpl<>(Arrays.asList(card1, card2));
        Pageable pageable = mock(Pageable.class);

        when(cardRepository.findAll(pageable)).thenReturn(cardPage);

        Page<CardDto> result = cardService.getAllCards(null, null, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(cardRepository, times(1)).findAll(pageable);
    }
}