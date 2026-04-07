package com.example.agentframework.service.impl;

import com.example.agentframework.entity.User;
import com.example.agentframework.mapper.UserMapper;
import com.example.agentframework.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User register(User user) {
        User existingUser = userMapper.findByUsername(user.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        user.setStatus(1);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        userMapper.insert(user);
        return user;
    }

    @Override
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user != null) {
            String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
            if (user.getPassword().equals(encryptedPassword)) {
                user.setLastLoginTime(new Date());
                userMapper.updateLoginTime(user.getId());
                return user;
            }
        }
        return null;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        User user = userMapper.findById(id);
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        User user = userMapper.findByUsername(username);
        return Optional.ofNullable(user);
    }

    @Override
    public List<User> getUsersByRole(String role) {
        return userMapper.findByRole(role);
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.findAll();
    }

    @Override
    public User updateUser(Long id, User user) {
        User existingUser = userMapper.findById(id);
        if (existingUser != null) {
            if (user.getUsername() != null) {
                existingUser.setUsername(user.getUsername());
            }
            if (user.getRealName() != null) {
                existingUser.setRealName(user.getRealName());
            }
            if (user.getEmail() != null) {
                existingUser.setEmail(user.getEmail());
            }
            if (user.getPhone() != null) {
                existingUser.setPhone(user.getPhone());
            }
            if (user.getDepartment() != null) {
                existingUser.setDepartment(user.getDepartment());
            }
            if (user.getAvatar() != null) {
                // 限制avatar字段长度，避免数据库截断错误
                String avatar = user.getAvatar();
                if (avatar.length() > 250) {
                    // 如果头像数据太长，只保存前250个字符
                    avatar = avatar.substring(0, 250);
                }
                existingUser.setAvatar(avatar);
            }
            existingUser.setUpdatedAt(new Date());
            userMapper.update(existingUser);
            return existingUser;
        }
        return null;
    }

    @Override
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }

    @Override
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        User user = userMapper.findById(id);
        if (user != null) {
            String encryptedOldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
            if (user.getPassword().equals(encryptedOldPassword)) {
                userMapper.updatePassword(id, DigestUtils.md5DigestAsHex(newPassword.getBytes()));
                return true;
            }
        }
        return false;
    }

    @Override
    public User updateProfile(Long id, User user) {
        return updateUser(id, user);
    }
}
