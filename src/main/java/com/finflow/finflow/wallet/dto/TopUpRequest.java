package com.finflow.finflow.wallet.dto;

import java.math.BigDecimal;

public record TopUpRequest(
        Long walletId,
        BigDecimal amount
) {

}
