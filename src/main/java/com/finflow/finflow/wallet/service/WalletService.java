package com.finflow.finflow.wallet.service;

import com.finflow.finflow.user.entity.User;
import com.finflow.finflow.wallet.entity.Wallet;

import java.math.BigDecimal;

public interface WalletService {
    public Wallet getWalletByUser(User user);
    public Wallet topUp(Long walletId, BigDecimal amount);
}
