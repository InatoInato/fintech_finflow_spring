package com.finflow.finflow.wallet.service;

import com.finflow.finflow.user.entity.User;
import com.finflow.finflow.wallet.dto.WalletResponse;
import com.finflow.finflow.wallet.entity.Wallet;

import java.math.BigDecimal;

public interface WalletService {
    public WalletResponse getWalletByUser(User user);
    public Wallet topUp(Long walletId, BigDecimal amount);
    public void createDefaultWallet(User user);
    public void getWalletForUser(Long walletId, User user);
    public void adjustBalance(Wallet wallet, BigDecimal amount);
    public Wallet getWalletEntityById(Long id);
    public void save(Wallet wallet);
    public Wallet getWalletByIdWithLock(Long id);
}
