package com.finflow.finflow.integration;

import com.finflow.finflow.user.entity.User;
import com.finflow.finflow.user.service.UserService;
import com.finflow.finflow.wallet.dto.WalletResponse;
import com.finflow.finflow.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class WalletIntegrationTest extends BaseIntegrationTest{
    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @Test
    void testTopUpAndGetWallet(){
        User user = userService.createUser("testuser@email.com", "Test123");

        walletService.createDefaultWallet(user);

        WalletResponse wallet = walletService.getWalletByUser(user);
        assertThat(wallet.balance()).isEqualByComparingTo(BigDecimal.ZERO);

        walletService.adjustBalance(walletService.getWalletEntityById(wallet.walletId()), BigDecimal.valueOf(500));

        WalletResponse updatedWallet = walletService.getWalletByUser(user);
        assertThat(updatedWallet.balance()).isEqualByComparingTo("500");
    }
}
