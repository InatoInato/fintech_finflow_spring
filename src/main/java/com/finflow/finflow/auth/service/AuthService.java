package com.finflow.finflow.auth.service;

import com.finflow.finflow.auth.dto.AuthResponse;
import com.finflow.finflow.auth.dto.InputRequest;
import com.finflow.finflow.auth.entity.User;

public interface AuthService {
    public AuthResponse register(InputRequest request);
    public AuthResponse login(InputRequest request);
}
