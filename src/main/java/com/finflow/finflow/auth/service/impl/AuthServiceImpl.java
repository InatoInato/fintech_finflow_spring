package com.finflow.finflow.auth.service.impl;

import com.finflow.finflow.auth.dto.AuthResponse;
import com.finflow.finflow.auth.dto.InputRequest;
import com.finflow.finflow.auth.entity.User;
import com.finflow.finflow.auth.repository.UserRepository;
import com.finflow.finflow.auth.security.JwtService;
import com.finflow.finflow.auth.service.AuthService;
import com.finflow.finflow.exception.InvalidCredentialsException;
import com.finflow.finflow.exception.UserAlreadyExistsException;
import com.finflow.finflow.wallet.entity.Wallet;
import com.finflow.finflow.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final WalletRepository walletRepository;

    @Override
    public AuthResponse register(InputRequest request) {
        if (repository.findByEmail(request.email()).isPresent()){
            throw new UserAlreadyExistsException("User already exists");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        repository.save(user);

        Wallet wallet = new Wallet();
        wallet.setCurrency("USD");
        wallet.setUser(user);
        walletRepository.save(wallet);

        String token = jwtService.generateToken(request.email());
        return new AuthResponse(user.getEmail(), token);
    }

    @Override
    public AuthResponse login(InputRequest request) {
        User user = repository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())){
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(user.getEmail(), token);
    }
}
