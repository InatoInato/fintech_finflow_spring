package com.finflow.finflow.transaction.service;

import com.finflow.finflow.exception.BadRequestException;
import com.finflow.finflow.exception.ForbiddenException;
import com.finflow.finflow.transaction.entity.Transaction;
import com.finflow.finflow.transaction.entity.TransactionType;
import com.finflow.finflow.transaction.repository.TransactionRepository;
import com.finflow.finflow.transaction.service.impl.TransactionServiceImpl;
import com.finflow.finflow.user.entity.User;
import com.finflow.finflow.user.service.UserService;
import com.finflow.finflow.wallet.entity.Wallet;
import com.finflow.finflow.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User user;
    private Wallet fromWallet;
    private Wallet toWallet;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@mail.com");

        fromWallet = new Wallet();
        fromWallet.setId(10L);
        fromWallet.setUser(user);
        fromWallet.setBalance(BigDecimal.valueOf(100));
        fromWallet.setCurrency("USD");

        toWallet = new Wallet();
        toWallet.setId(20L);
        toWallet.setUser(user);
        toWallet.setBalance(BigDecimal.valueOf(50));
        toWallet.setCurrency("USD");
    }

    // ---------------- SUCCESS ----------------

    @Test
    void shouldCreateTransferTransaction() {
        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
        when(walletService.getWalletById(10L)).thenReturn(fromWallet);
        when(walletService.getWalletById(20L)).thenReturn(toWallet);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction tx = transactionService.createTransaction(
                user.getEmail(),
                10L,
                20L,
                BigDecimal.valueOf(30)
        );

        assertEquals(TransactionType.TRANSFER, tx.getType());
        assertEquals(BigDecimal.valueOf(70), fromWallet.getBalance());
        assertEquals(BigDecimal.valueOf(80), toWallet.getBalance());

        verify(walletService).save(fromWallet);
        verify(walletService).save(toWallet);
        verify(transactionRepository).save(any(Transaction.class));
    }

    // ---------------- ERRORS ----------------

    @Test
    void shouldThrowIfInsufficientBalance() {
        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
        when(walletService.getWalletById(10L)).thenReturn(fromWallet);

        assertThrows(BadRequestException.class, () ->
                transactionService.createTransaction(
                        user.getEmail(),
                        10L,
                        null,
                        BigDecimal.valueOf(1000)
                )
        );

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldThrowIfWalletNotOwned() {
        User anotherUser = new User();
        anotherUser.setId(999L);

        fromWallet.setUser(anotherUser);

        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
        when(walletService.getWalletById(10L)).thenReturn(fromWallet);

        assertThrows(ForbiddenException.class, () ->
                transactionService.createTransaction(
                        user.getEmail(),
                        10L,
                        null,
                        BigDecimal.TEN
                )
        );
    }

    @Test
    void shouldThrowIfAmountInvalid() {
        assertThrows(BadRequestException.class, () ->
                transactionService.createTransaction(
                        user.getEmail(),
                        10L,
                        null,
                        BigDecimal.ZERO
                )
        );
    }
}
