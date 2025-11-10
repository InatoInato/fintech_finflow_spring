package com.finflow.finflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.finflow.transaction.controller.TransactionController;
import com.finflow.finflow.transaction.dto.TransactionRequestDto;
import com.finflow.finflow.transaction.entity.Transaction;
import com.finflow.finflow.transaction.entity.TransactionType;
import com.finflow.finflow.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void testCreateTransaction() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setCurrency("USD");
        transaction.setType(TransactionType.TRANSFER);
        transaction.setCreatedAt(LocalDateTime.now());

        Mockito.when(transactionService.createTransaction(
                        any(Authentication.class),
                        eq(1L),
                        eq(2L),
                        eq(BigDecimal.valueOf(100))
                ))
                .thenReturn(transaction);

        TransactionRequestDto requestDto = new TransactionRequestDto(1L, 2L, BigDecimal.valueOf(100));

        mockMvc.perform(post("/api/v1/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.type").value("TRANSFER"));
    }
}
