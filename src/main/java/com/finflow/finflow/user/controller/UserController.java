package com.finflow.finflow.user.controller;

import com.finflow.finflow.user.entity.User;
import com.finflow.finflow.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAll(){
        return ResponseEntity.ok(service.getAllUsers());
    }
}
