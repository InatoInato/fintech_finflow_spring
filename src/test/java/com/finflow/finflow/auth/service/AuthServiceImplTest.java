package com.finflow.finflow.auth.service;

import com.finflow.finflow.auth.dto.AuthResponse;
import com.finflow.finflow.auth.dto.InputRequest;
import com.finflow.finflow.auth.service.impl.AuthServiceImpl;
import com.finflow.finflow.config.security.JwtService;
import com.finflow.finflow.exception.InvalidCredentialsException;
import com.finflow.finflow.user.entity.User;
import com.finflow.finflow.user.service.UserService;
import com.finflow.finflow.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private WalletService walletService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void shouldRegisterUserSuccessfully() {
        InputRequest request = new InputRequest("test@mail.com", "password");

        User user = new User();
        user.setEmail(request.email());

        when(passwordEncoder.encode(request.password())).thenReturn("hashed");
        when(userService.createUser(request.email(), "hashed")).thenReturn(user);
        when(jwtService.generateToken(request.email())).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertEquals("test@mail.com", response.email());
        assertEquals("jwt-token", response.token());

        verify(walletService).createDefaultWallet(user);
    }

    @Test
    void shouldLoginSuccessfully() {
        InputRequest request = new InputRequest("test@mail.com", "password");

        User user = new User();
        user.setEmail(request.email());
        user.setPassword("hashed");

        when(userService.getUserByEmail(request.email())).thenReturn(user);
        when(passwordEncoder.matches(request.password(), "hashed")).thenReturn(true);
        when(jwtService.generateToken(user.getEmail())).thenReturn("jwt");

        AuthResponse response = authService.login(request);

        assertEquals("jwt", response.token());
    }

    @Test
    void shouldThrowIfPasswordInvalid() {
        InputRequest request = new InputRequest("test@mail.com", "wrong");

        User user = new User();
        user.setPassword("hashed");

        when(userService.getUserByEmail(request.email())).thenReturn(user);
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request));
    }
}
