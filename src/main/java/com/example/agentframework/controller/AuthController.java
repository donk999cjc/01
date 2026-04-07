package com.example.agentframework.controller;

import com.example.agentframework.entity.User;
import com.example.agentframework.entity.Student;
import com.example.agentframework.service.UserService;
import com.example.agentframework.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private StudentService studentService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginForm) {
        String username = loginForm.get("username");
        String password = loginForm.get("password");
        
        Map<String, Object> result = new HashMap<>();
        
        // 首先尝试查询student表（学生登录）
        Student student = studentService.getStudentByStudentId(username);
        if (student != null) {
            // 学生默认密码是123456或学号
            String defaultPassword = "123456";
            if (password.equals(defaultPassword) || password.equals(username)) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", student.getId());
                userData.put("username", student.getStudentId());
                userData.put("realName", student.getName());
                userData.put("role", "STUDENT");
                userData.put("studentId", student.getStudentId());
                
                result.put("success", true);
                result.put("message", "登录成功");
                result.put("user", userData);
                result.put("token", "token_" + student.getId() + "_" + System.currentTimeMillis());
                return ResponseEntity.ok(result);
            }
        }
        
        // 如果不是学生，尝试从sys_user表查询（教师或管理员登录）
        User user = userService.login(username, password);
        
        if (user != null) {
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("user", user);
            result.put("token", "token_" + user.getId() + "_" + System.currentTimeMillis());
            return ResponseEntity.ok(result);
        }
        
        // 登录失败
        result.put("success", false);
        result.put("message", "用户名或密码错误");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        try {
            User createdUser = userService.register(user);
            result.put("success", true);
            result.put("message", "注册成功");
            result.put("user", createdUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String role) {
        List<User> users = userService.getUsersByRole(role);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        if (updatedUser != null) {
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        userService.deleteUser(id);
        result.put("success", true);
        result.put("message", "删除成功");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/user/{id}/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> passwordForm) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.changePassword(
                id, 
                passwordForm.get("oldPassword"), 
                passwordForm.get("newPassword")
        );
        result.put("success", success);
        result.put("message", success ? "密码修改成功" : "原密码错误");
        return ResponseEntity.ok(result);
    }
}
