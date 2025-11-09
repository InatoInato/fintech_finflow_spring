package com.finflow.finflow.transaction.service.impl;

import com.finflow.finflow.transaction.entity.Transaction;
import com.finflow.finflow.transaction.entity.TransactionType;
import com.finflow.finflow.transaction.repository.TransactionRepository;
import com.finflow.finflow.transaction.service.TransactionService;
import com.finflow.finflow.wallet.entity.Wallet;
import com.finflow.finflow.wallet.repository.WalletRepository;
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
    private final WalletRepository walletRepository;

    @Transactional
    @Override
    public Transaction createTransaction(Long fromWalletId, Long toWalletId, BigDecimal amount) {
        if(fromWalletId == null && toWalletId == null){
            throw new IllegalArgumentException("Both wallets doesn't exist");
        }
        Wallet fromWallet = null;
        Wallet toWallet = null;

        if (fromWalletId != null) {
            fromWallet = walletRepository.findById(fromWalletId)
                    .orElseThrow(() -> new RuntimeException("From wallet not found"));
        }

        if (toWalletId != null) {
            toWallet = walletRepository.findById(toWalletId)
                    .orElseThrow(() -> new RuntimeException("To wallet not found"));
        }

        if (fromWallet != null && fromWallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        if(fromWallet != null){
            fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
            walletRepository.save(fromWallet);
        }

        if (toWallet != null) {
            toWallet.setBalance(toWallet.getBalance().add(amount));
            walletRepository.save(toWallet);
        }

        Transaction transaction = new Transaction();
        transaction.setFromWallet(fromWallet);
        transaction.setToWallet(toWallet);
        transaction.setAmount(amount);
        transaction.setCurrency(fromWallet != null ? fromWallet.getCurrency() :
                Objects.requireNonNull(toWallet).getCurrency());
        transaction.setType(determineType(fromWallet, toWallet));
        transaction.setCreatedAt(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    private TransactionType determineType(Wallet fromWallet, Wallet toWallet){
        if(fromWallet != null && toWallet != null) return TransactionType.TRANSFER;
        if(fromWallet == null) return TransactionType.DEPOSIT;
        return TransactionType.WITHDRAW;
    }
}
