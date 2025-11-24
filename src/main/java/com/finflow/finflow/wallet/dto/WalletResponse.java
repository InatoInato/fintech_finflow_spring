package com.finflow.finflow.wallet.dto;

import java.math.BigDecimal;

public record WalletResponse(
        Long walletId,
        BigDecimal balance,
        String currency,
        Long userId
) {}
