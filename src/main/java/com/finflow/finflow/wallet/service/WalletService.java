package com.finflow.finflow.wallet.service;

import com.finflow.finflow.user.entity.User;
import com.finflow.finflow.wallet.entity.Wallet;

import java.math.BigDecimal;

public interface WalletService {
    public Wallet getWalletByUser(User user);
    public Wallet topUp(Long walletId, BigDecimal amount);
    public void createDefaultWallet(User user);
    public Wallet getWalletForUser(Long walletId, User user);
    public void adjustBalance(Wallet wallet, BigDecimal amount);
    public Wallet getWalletById(Long id);
    public Wallet save(Wallet wallet);
}
