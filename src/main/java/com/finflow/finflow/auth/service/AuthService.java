package com.finflow.finflow.auth.service;

import com.finflow.finflow.auth.dto.RegisterRequest;
import com.finflow.finflow.auth.entity.User;

public interface AuthService {
    public User register(RegisterRequest request);
}
