package com.finflow.finflow.user.service;

import com.finflow.finflow.user.entity.User;

import java.util.List;

public interface UserService {
    public User getUserByEmail(String email);
    public List<User> getAllUsers();
    public User createUser(String email, String encodedPassword);
}
