package com.finflow.finflow.wallet.service;

import com.finflow.finflow.exception.BadRequestException;
import com.finflow.finflow.exception.ForbiddenException;
import com.finflow.finflow.user.entity.User;
import com.finflow.finflow.wallet.entity.Wallet;
import com.finflow.finflow.wallet.repository.WalletRepository;
import com.finflow.finflow.wallet.service.impl.WalletServiceImpl;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);

        wallet = new Wallet();
        wallet.setId(10L);
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.valueOf(100));
    }

    @Test
    void shouldCreateDefaultWallet() {
        walletService.createDefaultWallet(user);

        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void shouldTopUpWallet() {
        when(walletRepository.findById(10L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(wallet)).thenReturn(wallet);

        Wallet updated = walletService.topUp(10L, BigDecimal.valueOf(50));

        assertEquals(BigDecimal.valueOf(150), updated.getBalance());
    }

    @Test
    void shouldThrowIfTopUpAmountInvalid() {
        assertThrows(BadRequestException.class,
                () -> walletService.topUp(10L, BigDecimal.ZERO));
    }

    @Test
    void shouldThrowIfAccessingForeignWallet() {
        User anotherUser = new User();
        anotherUser.setId(999L);

        when(walletRepository.findById(10L)).thenReturn(Optional.of(wallet));

        assertThrows(ForbiddenException.class,
                () -> walletService.getWalletForUser(10L, anotherUser));
    }

    @Test
    void shouldThrowIfAdjustBalanceGoesNegative() {
        assertThrows(BadRequestException.class,
                () -> walletService.adjustBalance(wallet, BigDecimal.valueOf(-200)));
    }

    @Test
    void optimisticLocking_shouldThrowException() {
        Wallet w1 = new Wallet();
        w1.setId(1L);
        w1.setBalance(BigDecimal.valueOf(100));
        w1.setVersion(0L);

        Wallet w2 = new Wallet();
        w2.setId(1L);
        w2.setBalance(BigDecimal.valueOf(100));
        w2.setVersion(0L);

        when(walletRepository.save(w1)).thenAnswer(invocation -> {
            Wallet saved = invocation.getArgument(0);
            saved.setVersion(1L);
            return saved;
        });

        when(walletRepository.save(w2)).thenThrow(new OptimisticLockException("Version mismatch"));

        // Act
        w1.setBalance(w1.getBalance().add(BigDecimal.valueOf(10)));
        walletRepository.save(w1);

        w2.setBalance(w2.getBalance().add(BigDecimal.valueOf(5)));

        // Assert
        assertThrows(OptimisticLockException.class, () -> walletRepository.save(w2));
    }
}
