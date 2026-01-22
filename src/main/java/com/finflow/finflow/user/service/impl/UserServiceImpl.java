package com.finflow.finflow.user.service.impl;

import com.finflow.finflow.exception.NotFoundException;
import com.finflow.finflow.exception.UserAlreadyExistsException;
import com.finflow.finflow.user.entity.User;
import com.finflow.finflow.user.repository.UserRepository;
import com.finflow.finflow.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    @Override
    public User getUserByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User with this email not found"));
    }

    @Override
    public List<User> getAllUsers() {
        return repository.findAll();
    }

    @Override
    public User createUser(String email, String encodedPassword) {
        if (repository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);
        return repository.save(user);
    }
}
