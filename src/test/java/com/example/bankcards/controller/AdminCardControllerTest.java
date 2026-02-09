package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.MessageDto;
import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.enums.CardStatus;
import com.example.bankcards.enums.TransactionType;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminCardControllerTest {

    @Mock
    private CardService cardService;

    @InjectMocks
    private AdminCardController adminCardController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(adminCardController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .build();
    }

    @Test
    void getAllCards_NoFilters_Success() throws Exception {
        CardDto card1 = CardDto.builder()
                .id(1L)
                .maskedNumber("**** **** **** 1234")
                .cardHolder("JOHN DOE")
                .status(CardStatus.ACTIVE)
                .build();

        CardDto card2 = CardDto.builder()
                .id(2L)
                .maskedNumber("**** **** **** 5678")
                .cardHolder("JANE DOE")
                .status(CardStatus.BLOCKED)
                .build();

        Map<String, Object> pageResponse = new HashMap<>();
        pageResponse.put("content", Arrays.asList(card1, card2));
        pageResponse.put("totalPages", 1);
        pageResponse.put("totalElements", 2);

        Page<CardDto> page = new PageImpl<>(Arrays.asList(card1, card2), PageRequest.of(0, 10), 2);

        when(cardService.getAllCards(any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/admin/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[1].id").value(2L));

    }

    @Test
    void getAllCards_WithUserIdFilter_Success() throws Exception {
        CardDto card = CardDto.builder()
                .id(1L)
                .maskedNumber("**** **** **** 1234")
                .cardHolder("JOHN DOE")
                .build();

        Page<CardDto> page = new PageImpl<>(
                List.of(card),
                PageRequest.of(0, 10),
                1
        );

        when(cardService.getAllCards(
                eq(1L),
                nullable(CardStatus.class),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/admin/cards")
                        .param("userId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    void getAllCards_WithStatus_Success() throws Exception {
        CardDto card1 = CardDto.builder()
                .id(1L)
                .maskedNumber("**** **** **** 1234")
                .cardHolder("JOHN DOE")
                .status(CardStatus.ACTIVE)
                .build();

        Page<CardDto> page = new PageImpl<>(
                List.of(card1),
                PageRequest.of(0, 10),
                1
        );


        when(cardService.getAllCards(
                any(),
                any(),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/admin/cards")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    void blockCard_Success() throws Exception {
        CardDto card = CardDto.builder()
                .id(1L)
                .maskedNumber("**** **** **** 1234")
                .status(CardStatus.BLOCKED)
                .build();

        when(cardService.blockCard(eq(1L))).thenReturn(card);

        mockMvc.perform(post("/api/admin/cards/1/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    void activateCard_Success() throws Exception {
        CardDto card = CardDto.builder()
                .id(1L)
                .maskedNumber("**** **** **** 1234")
                .status(CardStatus.ACTIVE)
                .build();

        when(cardService.activateCard(eq(1L))).thenReturn(card);

        mockMvc.perform(post("/api/admin/cards/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void deleteCard_Success() throws Exception {
        MessageDto response = new MessageDto("Card deleted successfully");
        when(cardService.deleteCard(eq(1L))).thenReturn(response);

        mockMvc.perform(delete("/api/admin/cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Card deleted successfully"));
    }

    @Test
    void getCardTransactions_Success() throws Exception {
        TransactionDto transaction1 = TransactionDto.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.TRANSFER)
                .build();

        TransactionDto transaction2 = TransactionDto.builder()
                .id(2L)
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.TRANSFER)
                .build();

        List<TransactionDto> transactions = Arrays.asList(transaction1, transaction2);

        when(cardService.getCardTransactions(eq(1L))).thenReturn(transactions);

        mockMvc.perform(get("/api/admin/cards/1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].amount").value(100.00))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].amount").value(50.00));
    }

    @Test
    void getCardTransactions_NoTransactions_EmptyList() throws Exception {
        when(cardService.getCardTransactions(eq(1L))).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/cards/1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}