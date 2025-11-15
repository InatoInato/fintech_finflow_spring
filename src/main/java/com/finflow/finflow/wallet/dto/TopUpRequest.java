package com.finflow.finflow.wallet.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record TopUpRequest(
        @NotNull(message = "walletId is required")
        @Positive(message = "walletId must be positive")
        Long walletId,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "Minimum top-up amount is 0.01")
        BigDecimal amount
) {}
