package com.finflow.finflow.wallet.controller;

import com.finflow.finflow.auth.entity.User;
import com.finflow.finflow.auth.repository.UserRepository;
import com.finflow.finflow.wallet.dto.TopUpRequest;
import com.finflow.finflow.wallet.entity.Wallet;
import com.finflow.finflow.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Wallet> getMyWallet(Authentication authentication){
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = walletService.getWalletByUser(user);
        return ResponseEntity.ok(wallet);
    }

    @PostMapping("/topup")
    public ResponseEntity<Wallet> topUp(@Valid @RequestBody TopUpRequest request) {
        Wallet wallet = walletService.topUp(request.walletId(), request.amount());
        return ResponseEntity.ok(wallet);
    }

}
