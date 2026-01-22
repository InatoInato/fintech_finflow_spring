package com.finflow.finflow.auth.service.impl;

import com.finflow.finflow.auth.dto.AuthResponse;
import com.finflow.finflow.auth.dto.InputRequest;
import com.finflow.finflow.user.entity.User;
import com.finflow.finflow.user.repository.UserRepository;
import com.finflow.finflow.config.security.JwtService;
import com.finflow.finflow.auth.service.AuthService;
import com.finflow.finflow.exception.InvalidCredentialsException;
import com.finflow.finflow.exception.UserAlreadyExistsException;
import com.finflow.finflow.user.service.UserService;
import com.finflow.finflow.wallet.entity.Wallet;
import com.finflow.finflow.wallet.repository.WalletRepository;
import com.finflow.finflow.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final WalletService walletService;
    private final UserService userService;

    @Override
    public AuthResponse register(InputRequest request) {
        User user = userService.createUser(
                request.email(),
                passwordEncoder.encode(request.password())
        );

        walletService.createDefaultWallet(user);

        String token = jwtService.generateToken(request.email());
        return new AuthResponse(user.getEmail(), token);
    }

    @Override
    public AuthResponse login(InputRequest request) {
        User user = userService.getUserByEmail(request.email());

        if (!passwordEncoder.matches(request.password(), user.getPassword())){
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(user.getEmail(), token);
    }
}
