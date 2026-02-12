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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        fromWallet.setBalance(BigDecimal.valueOf(1000));
        fromWallet.setCurrency("USD");

        toWallet = new Wallet();
        toWallet.setId(20L);
        toWallet.setUser(user);
        toWallet.setBalance(BigDecimal.valueOf(500));
        toWallet.setCurrency("USD");
    }

    @Nested
    @DisplayName("Transfer Transactions")
    class TransferTests {

        @Test
        @DisplayName("Should create transfer transaction successfully")
        void shouldCreateTransferTransaction() {
            // Arrange
            BigDecimal transferAmount = BigDecimal.valueOf(300);

            when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
            when(walletService.getWalletByIdWithLock(10L)).thenReturn(fromWallet);
            when(walletService.getWalletEntityById(20L)).thenReturn(toWallet);
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Transaction result = transactionService.createTransaction(
                    user.getEmail(),
                    10L,
                    20L,
                    transferAmount
            );

            // Assert
            assertNotNull(result);
            assertEquals(TransactionType.TRANSFER, result.getType());
            assertEquals(transferAmount, result.getAmount());
            assertEquals("USD", result.getCurrency());
            assertEquals(fromWallet, result.getFromWallet());
            assertEquals(toWallet, result.getToWallet());

            // Verify balance changes
            assertEquals(BigDecimal.valueOf(700), fromWallet.getBalance());
            assertEquals(BigDecimal.valueOf(800), toWallet.getBalance());

            // Verify saves
            verify(walletService).save(fromWallet);
            verify(walletService).save(toWallet);
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Should throw exception when insufficient balance")
        void shouldThrowExceptionWhenInsufficientBalance() {
            // Arrange
            BigDecimal excessiveAmount = BigDecimal.valueOf(2000);

            when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
            when(walletService.getWalletByIdWithLock(10L)).thenReturn(fromWallet);

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class, () ->
                    transactionService.createTransaction(
                            user.getEmail(),
                            10L,
                            20L,
                            excessiveAmount
                    )
            );

            assertEquals("Insufficient balance", exception.getMessage());
            verify(transactionRepository, never()).save(any());
            verify(walletService, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when user doesn't own wallet")
        void shouldThrowExceptionWhenUserDoesntOwnWallet() {
            // Arrange
            User anotherUser = new User();
            anotherUser.setId(999L);
            anotherUser.setEmail("another@mail.com");

            fromWallet.setUser(anotherUser);

            when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
            when(walletService.getWalletByIdWithLock(10L)).thenReturn(fromWallet);

            // Act & Assert
            ForbiddenException exception = assertThrows(ForbiddenException.class, () ->
                    transactionService.createTransaction(
                            user.getEmail(),
                            10L,
                            20L,
                            BigDecimal.valueOf(100)
                    )
            );

            assertEquals("You do not own this wallet", exception.getMessage());
            verify(transactionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Deposit Transactions")
    class DepositTests {

        @Test
        @DisplayName("Should create deposit transaction successfully")
        void shouldCreateDepositTransaction() {
            // Arrange
            BigDecimal depositAmount = BigDecimal.valueOf(200);

            when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
            when(walletService.getWalletEntityById(20L)).thenReturn(toWallet);
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Transaction result = transactionService.createTransaction(
                    user.getEmail(),
                    null,  // No fromWallet for deposit
                    20L,
                    depositAmount
            );

            // Assert
            assertNotNull(result);
            assertEquals(TransactionType.DEPOSIT, result.getType());
            assertEquals(depositAmount, result.getAmount());
            assertEquals("USD", result.getCurrency());
            assertNull(result.getFromWallet());
            assertEquals(toWallet, result.getToWallet());

            // Verify balance increase
            assertEquals(BigDecimal.valueOf(700), toWallet.getBalance());

            verify(walletService).save(toWallet);
            verify(walletService, never()).save(fromWallet);
            verify(transactionRepository).save(any(Transaction.class));
        }
    }

    @Nested
    @DisplayName("Withdrawal Transactions")
    class WithdrawalTests {

        @Test
        @DisplayName("Should create withdrawal transaction successfully")
        void shouldCreateWithdrawalTransaction() {
            // Arrange
            BigDecimal withdrawalAmount = BigDecimal.valueOf(300);

            when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
            when(walletService.getWalletByIdWithLock(10L)).thenReturn(fromWallet);
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Transaction result = transactionService.createTransaction(
                    user.getEmail(),
                    10L,
                    null,  // No toWallet for withdrawal
                    withdrawalAmount
            );

            // Assert
            assertNotNull(result);
            assertEquals(TransactionType.WITHDRAW, result.getType());
            assertEquals(withdrawalAmount, result.getAmount());
            assertEquals("USD", result.getCurrency());
            assertEquals(fromWallet, result.getFromWallet());
            assertNull(result.getToWallet());

            // Verify balance decrease
            assertEquals(BigDecimal.valueOf(700), fromWallet.getBalance());

            verify(walletService).save(fromWallet);
            verify(walletService, never()).save(toWallet);
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Should throw exception when withdrawing more than balance")
        void shouldThrowExceptionWhenWithdrawingTooMuch() {
            // Arrange
            BigDecimal excessiveAmount = BigDecimal.valueOf(1500);

            when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
            when(walletService.getWalletByIdWithLock(10L)).thenReturn(fromWallet);

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class, () ->
                    transactionService.createTransaction(
                            user.getEmail(),
                            10L,
                            null,
                            excessiveAmount
                    )
            );

            assertEquals("Insufficient balance", exception.getMessage());
            verify(transactionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when both wallets are null")
        void shouldThrowExceptionWhenBothWalletsNull() {
            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class, () ->
                    transactionService.createTransaction(
                            user.getEmail(),
                            null,
                            null,
                            BigDecimal.valueOf(100)
                    )
            );

            assertEquals("Both 'fromWalletId' and 'toWalletId' cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when amount is null")
        void shouldThrowExceptionWhenAmountIsNull() {
            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class, () ->
                    transactionService.createTransaction(
                            user.getEmail(),
                            10L,
                            20L,
                            null
                    )
            );

            assertEquals("Amount must be greater than zero", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when amount is zero")
        void shouldThrowExceptionWhenAmountIsZero() {
            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class, () ->
                    transactionService.createTransaction(
                            user.getEmail(),
                            10L,
                            20L,
                            BigDecimal.ZERO
                    )
            );

            assertEquals("Amount must be greater than zero", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when amount is negative")
        void shouldThrowExceptionWhenAmountIsNegative() {
            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class, () ->
                    transactionService.createTransaction(
                            user.getEmail(),
                            10L,
                            20L,
                            BigDecimal.valueOf(-100)
                    )
            );

            assertEquals("Amount must be greater than zero", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Currency Resolution Tests")
    class CurrencyTests {

        @Test
        @DisplayName("Should use fromWallet currency for transfer")
        void shouldUseFromWalletCurrencyForTransfer() {
            // Arrange
            fromWallet.setCurrency("EUR");
            toWallet.setCurrency("USD");

            when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
            when(walletService.getWalletByIdWithLock(10L)).thenReturn(fromWallet);
            when(walletService.getWalletEntityById(20L)).thenReturn(toWallet);
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Transaction result = transactionService.createTransaction(
                    user.getEmail(),
                    10L,
                    20L,
                    BigDecimal.valueOf(100)
            );

            // Assert
            assertEquals("EUR", result.getCurrency());
        }

        @Test
        @DisplayName("Should use toWallet currency for deposit")
        void shouldUseToWalletCurrencyForDeposit() {
            // Arrange
            toWallet.setCurrency("GBP");

            when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
            when(walletService.getWalletEntityById(20L)).thenReturn(toWallet);
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Transaction result = transactionService.createTransaction(
                    user.getEmail(),
                    null,
                    20L,
                    BigDecimal.valueOf(100)
            );

            // Assert
            assertEquals("GBP", result.getCurrency());
        }
    }

    @Nested
    @DisplayName("Transaction Persistence Tests")
    class PersistenceTests {

        @Test
        @DisplayName("Should save transaction with correct timestamp")
        void shouldSaveTransactionWithTimestamp() {
            // Arrange
            when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
            when(walletService.getWalletByIdWithLock(10L)).thenReturn(fromWallet);
            when(walletService.getWalletEntityById(20L)).thenReturn(toWallet);

            ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
            when(transactionRepository.save(transactionCaptor.capture()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            transactionService.createTransaction(
                    user.getEmail(),
                    10L,
                    20L,
                    BigDecimal.valueOf(100)
            );

            // Assert
            Transaction savedTransaction = transactionCaptor.getValue();
            assertNotNull(savedTransaction.getCreatedAt());
        }
    }
}