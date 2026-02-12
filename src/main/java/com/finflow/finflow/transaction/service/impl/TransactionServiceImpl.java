package com.finflow.finflow.transaction.service.impl;

import com.finflow.finflow.user.entity.User;
import com.finflow.finflow.exception.BadRequestException;
import com.finflow.finflow.exception.ForbiddenException;
import com.finflow.finflow.transaction.entity.Transaction;
import com.finflow.finflow.transaction.entity.TransactionType;
import com.finflow.finflow.transaction.repository.TransactionRepository;
import com.finflow.finflow.transaction.service.TransactionService;
import com.finflow.finflow.user.service.UserService;
import com.finflow.finflow.wallet.entity.Wallet;
import com.finflow.finflow.wallet.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletService walletService;
    private final UserService userService;

    @Transactional
    @Override
    public Transaction createTransaction(String email,
                                         Long fromWalletId,
                                         Long toWalletId,
                                         BigDecimal amount) {

        validateRequest(fromWalletId, toWalletId, amount);

        User user = userService.getUserByEmail(email);

        Wallet fromWallet = (fromWalletId != null)
                ? walletService.getWalletByIdWithLock(fromWalletId)
                : null;

        Wallet toWallet = (toWalletId != null)
                ? findWalletEntityOrThrow(toWalletId)
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
        return walletService.getWalletEntityById(id);
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
        if (fromWallet != null) walletService.save(fromWallet);
        if (toWallet != null) walletService.save(toWallet);
    }


    // ------------------------------------------------------
    // BUSINESS LOGIC
    // ------------------------------------------------------

    private String resolveCurrency(Wallet fromWallet, Wallet toWallet) {
        if (fromWallet != null) return fromWallet.getCurrency();
        if (toWallet != null) return toWallet.getCurrency();
        return "USD"; // Default fallback or throw a specific exception
    }

    private TransactionType determineType(Wallet fromWallet, Wallet toWallet) {
        if (fromWallet != null && toWallet != null) return TransactionType.TRANSFER;
        if (fromWallet == null && toWallet != null) return TransactionType.DEPOSIT;
        if (fromWallet != null && toWallet == null) return TransactionType.WITHDRAW;
        throw new BadRequestException("Invalid transaction direction");
    }

    private Wallet findWalletEntityOrThrow(Long id) {

        return walletService.getWalletEntityById(id);
    }
}
