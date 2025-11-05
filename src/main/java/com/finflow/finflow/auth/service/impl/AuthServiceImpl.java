package com.finflow.finflow.auth.service.impl;

import com.finflow.finflow.auth.dto.RegisterRequest;
import com.finflow.finflow.auth.entity.User;
import com.finflow.finflow.auth.repository.UserRepository;
import com.finflow.finflow.auth.service.AuthService;
import com.finflow.finflow.exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(RegisterRequest request) {
        if (repository.findByEmail(request.email()).isPresent()){
            throw new UserAlreadyExistsException("User already exists");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        return repository.save(user);
    }
}
