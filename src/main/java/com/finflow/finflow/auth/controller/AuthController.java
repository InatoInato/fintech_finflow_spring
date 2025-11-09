package com.finflow.finflow.auth.controller;

import com.finflow.finflow.auth.dto.AuthResponse;
import com.finflow.finflow.auth.dto.InputRequest;
import com.finflow.finflow.auth.entity.User;
import com.finflow.finflow.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/hello")
    public ResponseEntity<String> hello(){
        return ResponseEntity.ok("Hello from Java");
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody InputRequest request){
        return ResponseEntity.ok(authService.register(request));
    }

    @GetMapping("/transactions")
    public ResponseEntity<String> getTransactions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return ResponseEntity.ok("Transactions for " + email);
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody InputRequest request){
        return ResponseEntity.ok(authService.login(request));
    }
}
