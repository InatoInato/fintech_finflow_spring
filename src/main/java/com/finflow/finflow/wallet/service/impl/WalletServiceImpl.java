package com.finflow.finflow.wallet.service.impl;

import com.finflow.finflow.exception.BadRequestException;
import com.finflow.finflow.exception.ForbiddenException;
import com.finflow.finflow.exception.NotFoundException;
import com.finflow.finflow.user.entity.User;
import com.finflow.finflow.wallet.dto.WalletResponse;
import com.finflow.finflow.wallet.entity.Wallet;
import com.finflow.finflow.wallet.repository.WalletRepository;
import com.finflow.finflow.wallet.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;

    @Override
    public WalletResponse getWalletByUser(User user) {
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Wallet not found"));

        return mapToResponse(wallet);
    }

    @Override
    @Transactional
    public Wallet topUp(Long walletId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new BadRequestException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(amount));
        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public void createDefaultWallet(User user) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setCurrency("USD");
        wallet.setBalance(BigDecimal.ZERO);
        walletRepository.save(wallet);
    }

    @Override
    public void getWalletForUser(Long walletId, User user) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new NotFoundException("Wallet not found"));

        if (!Objects.equals(wallet.getUser().getId(), user.getId())) {
            throw new ForbiddenException("You don't own this wallet");
        }

        mapToResponse(wallet);
    }

    @Transactional
    @Override
    public void adjustBalance(Wallet wallet, BigDecimal amount) {
        if (amount == null) throw new BadRequestException("Amound is null");
        BigDecimal newBalance = wallet.getBalance().add(amount);
        if(newBalance.compareTo(BigDecimal.ZERO) < 0){
            throw new BadRequestException("Insufficient balance");
        }
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
    }

    @Override
    public Wallet getWalletEntityById(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Wallet not found with id: " + id));
    }

    @Override
    public void save(Wallet wallet) {
        walletRepository.save(wallet);
    }

    @Override
    public Wallet getWalletByIdWithLock(Long id) {
        return walletRepository.findByIdWithLock(id)
                .orElseThrow(() -> new NotFoundException("Wallet not found"));
    }


    private WalletResponse mapToResponse(Wallet wallet){
        return new WalletResponse(
                wallet.getId(),
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.getUser().getId()
        );
    }
}
