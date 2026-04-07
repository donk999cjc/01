package com.example.agentframework.service;

import com.example.agentframework.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User register(User user);
    User login(String username, String password);
    Optional<User> getUserById(Long id);
    Optional<User> getUserByUsername(String username);
    List<User> getUsersByRole(String role);
    List<User> getAllUsers();
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    boolean changePassword(Long id, String oldPassword, String newPassword);
    User updateProfile(Long id, User user);
}
