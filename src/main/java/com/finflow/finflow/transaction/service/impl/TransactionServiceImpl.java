package com.finflow.finflow.transaction.service.impl;

import com.finflow.finflow.user.entity.User;
import com.finflow.finflow.user.repository.UserRepository;
import com.finflow.finflow.exception.BadRequestException;
import com.finflow.finflow.exception.ForbiddenException;
import com.finflow.finflow.exception.NotFoundException;
import com.finflow.finflow.transaction.entity.Transaction;
import com.finflow.finflow.transaction.entity.TransactionType;
import com.finflow.finflow.transaction.repository.TransactionRepository;
import com.finflow.finflow.transaction.service.TransactionService;
import com.finflow.finflow.wallet.entity.Wallet;
import com.finflow.finflow.wallet.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public Transaction createTransaction(Authentication auth,
                                         Long fromWalletId,
                                         Long toWalletId,
                                         BigDecimal amount) {

        validateRequest(fromWalletId, toWalletId, amount);

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Wallet fromWallet = (fromWalletId != null)
                ? getWalletOrThrow(fromWalletId)
                : null;

        Wallet toWallet = (toWalletId != null)
                ? getWalletOrThrow(toWalletId)
                : null;

        if (fromWallet != null) {
            validateOwnership(user, fromWallet);
            validateBalance(fromWallet, amount);
            fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        }

        if (toWallet != null) {
            toWallet.setBalance(toWallet.getBalance().add(amount));
        }

        // Save wallets only when needed
        saveWallets(fromWallet, toWallet);

        Transaction transaction = new Transaction();
        transaction.setFromWallet(fromWallet);
        transaction.setToWallet(toWallet);
        transaction.setAmount(amount);
        transaction.setCurrency(resolveCurrency(fromWallet, toWallet));
        transaction.setType(determineType(fromWallet, toWallet));
        transaction.setCreatedAt(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }


    // ------------------------------------------------------
    // VALIDATION
    // ------------------------------------------------------

    private void validateRequest(Long fromWallet, Long toWallet, BigDecimal amount) {
        if (fromWallet == null && toWallet == null) {
            throw new BadRequestException("Both 'fromWalletId' and 'toWalletId' cannot be null");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }
    }

    private Wallet getWalletOrThrow(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Wallet not found: " + id));
    }

    private void validateOwnership(User user, Wallet wallet) {
        if (!Objects.equals(wallet.getUser().getId(), user.getId())) {
            throw new ForbiddenException("You do not own this wallet");
        }
    }

    private void validateBalance(Wallet wallet, BigDecimal amount) {
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient balance");
        }
    }

    private void saveWallets(Wallet fromWallet, Wallet toWallet) {
        if (fromWallet != null) walletRepository.save(fromWallet);
        if (toWallet != null) walletRepository.save(toWallet);
    }


    // ------------------------------------------------------
    // BUSINESS LOGIC
    // ------------------------------------------------------

    private String resolveCurrency(Wallet fromWallet, Wallet toWallet) {
        if (fromWallet != null) return fromWallet.getCurrency();
        return toWallet.getCurrency();
    }

    private TransactionType determineType(Wallet fromWallet, Wallet toWallet) {
        if (fromWallet != null && toWallet != null) return TransactionType.TRANSFER;
        if (fromWallet == null) return TransactionType.DEPOSIT;
        return TransactionType.WITHDRAW;
    }
}
