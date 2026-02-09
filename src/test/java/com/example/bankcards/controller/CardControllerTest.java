package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.request.UpdateCardRequest;
import com.example.bankcards.dto.MessageDto;
import com.example.bankcards.enums.CardStatus;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardController cardController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = User.withUsername("test@example.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();


        HandlerMethodArgumentResolver mockUserResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().isAssignableFrom(UserDetails.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return userDetails;
            }
        };


        mockMvc = MockMvcBuilders.standaloneSetup(cardController)
                .setCustomArgumentResolvers(mockUserResolver)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void createCard_Success() throws Exception {
        CreateCardRequest request = CreateCardRequest.builder()
                .encryptedCardNumber("encryptedData")
                .cardHolder("John Doe")
                .expiryDate(LocalDate.now().plusYears(2))
                .cvv("111")
                .build();

        CardDto response = CardDto.builder()
                .id(1L)
                .maskedNumber("**** **** **** 1234")
                .cardHolder("JOHN DOE")
                .expiryDate(LocalDate.now().plusYears(2))
                .balance(BigDecimal.ZERO)
                .status(CardStatus.ACTIVE)
                .createdAt(LocalDate.now())
                .build();

        when(cardService.createCard(any(UserDetails.class), any(CreateCardRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/cards")
                        .principal(() -> "test@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.maskedNumber").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.cardHolder").value("JOHN DOE"));
    }

    @Test
    void getAllCards_Success() throws Exception {
        CardDto card1 = CardDto.builder()
                .id(1L)
                .maskedNumber("**** **** **** 1234")
                .cardHolder("JOHN DOE")
                .build();

        CardDto card2 = CardDto.builder()
                .id(2L)
                .maskedNumber("**** **** **** 5678")
                .cardHolder("JANE DOE")
                .build();

        List<CardDto> cards = Arrays.asList(card1, card2);

        when(cardService.getMyCards(any(UserDetails.class))).thenReturn(cards);

        mockMvc.perform(get("/api/cards")
                        .principal(() -> "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void getAllCards_NoCards_EmptyList() throws Exception {
        when(cardService.getMyCards(any(UserDetails.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/cards")
                        .principal(() -> "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deleteCard_Success() throws Exception {
        MessageDto response = new MessageDto("Card deleted successfully");

        when(cardService.deleteCard(any(UserDetails.class), eq(1L))).thenReturn(response);

        mockMvc.perform(delete("/api/cards/1")
                        .principal(() -> "test@example.com"))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.message").value("Card deleted successfully"));
    }

    @Test
    void getCard_Success() throws Exception {
        CardDto card = CardDto.builder()
                .id(1L)
                .maskedNumber("**** **** **** 1234")
                .cardHolder("JOHN DOE")
                .build();

        when(cardService.getCardById(any(UserDetails.class), eq(1L))).thenReturn(card);


        mockMvc.perform(get("/api/cards/1")
                        .principal(() -> "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.maskedNumber").value("**** **** **** 1234"));
    }

    @Test
    void updateCard_Success() throws Exception {
        UpdateCardRequest request = UpdateCardRequest.builder()
                .cardHolder("John Smith")
                .build();

        CardDto response = CardDto.builder()
                .id(1L)
                .maskedNumber("**** **** **** 1234")
                .cardHolder("JOHN SMITH")
                .build();

        when(cardService.updateCard(any(UserDetails.class), eq(1L), any(UpdateCardRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/cards/1")
                        .principal(() -> "test@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.cardHolder").value("JOHN SMITH"));
    }

    @Test
    void requestBlockCard_Success() throws Exception {
        MessageDto response = new MessageDto("Card blocked successfully");

        when(cardService.requestBlockCard(any(UserDetails.class), eq(1L))).thenReturn(response);

        mockMvc.perform(post("/api/cards/1/block-request")
                        .principal(() -> "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Card blocked successfully"));
    }

    @Test
    void transfer_Success() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .fromCardId(1L)
                .toCardId(2L)
                .amount(new BigDecimal("100.00"))
                .build();

        MessageDto response = new MessageDto("Transfer successfully");

        when(cardService.transfer(any(UserDetails.class), any(TransferRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/cards/transaction")
                        .principal(() -> "test@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Transfer successfully"));
    }

    @Test
    void transfer_InvalidRequest_BadRequest() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .fromCardId(1L)
                .toCardId(2L)
                .amount(new BigDecimal("-100.00"))
                .build();

        mockMvc.perform(post("/api/cards/transaction")
                        .principal(() -> "test@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}