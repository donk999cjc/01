package com.example.agentframework.controller;

import com.example.agentframework.entity.Student;
import com.example.agentframework.entity.Teacher;
import com.example.agentframework.service.StudentService;
import com.example.agentframework.service.TeacherService;
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
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginForm) {
        String username = loginForm.get("username");
        String password = loginForm.get("password");
        
        Map<String, Object> result = new HashMap<>();
        
        // 首先尝试查询 student 表（学生登录）
        Student student = studentService.getStudentByStudentId(username);
        if (student != null) {
            // 学生默认密码是 123456 或学号
            String defaultPassword = "123456";
            if (password.equals(defaultPassword) || password.equals(username)) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", student.getId());
                userData.put("username", student.getUsername());
                userData.put("realName", student.getRealName());
                userData.put("role", "STUDENT");
                userData.put("studentId", student.getStudentId());
                
                result.put("success", true);
                result.put("message", "登录成功");
                result.put("user", userData);
                result.put("token", "token_" + student.getId() + "_" + System.currentTimeMillis());
                return ResponseEntity.ok(result);
            }
        }
        
        // 尝试查询 teacher 表（教师登录）
        Teacher teacher = teacherService.findByTeacherId(username);
        if (teacher != null) {
            // 教师默认密码是 123456
            String defaultPassword = "123456";
            if (password.equals(defaultPassword) || password.equals(username)) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", teacher.getId());
                userData.put("username", teacher.getUsername());
                userData.put("realName", teacher.getRealName());
                userData.put("role", "TEACHER");
                userData.put("teacherId", teacher.getTeacherId());
                
                result.put("success", true);
                result.put("message", "登录成功");
                result.put("user", userData);
                result.put("token", "token_" + teacher.getId() + "_" + System.currentTimeMillis());
                return ResponseEntity.ok(result);
            }
        }
        
        // 登录失败
        result.put("success", false);
        result.put("message", "用户名或密码错误");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> registerForm) {
        String username = registerForm.get("username");
        String password = registerForm.get("password");
        String realName = registerForm.get("realName");
        String role = registerForm.get("role"); // STUDENT or TEACHER
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (username == null || username.trim().isEmpty() ||
                password == null || password.length() < 6 ||
                realName == null || realName.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "请填写完整信息，密码至少6位");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 检查用户名是否已存在
            Student existingStudent = studentService.getStudentByStudentId(username);
            Teacher existingTeacher = teacherService.findByTeacherId(username);
            
            if (existingStudent != null || existingTeacher != null) {
                result.put("success", false);
                result.put("message", "用户名已存在，请选择其他用户名");
                return ResponseEntity.badRequest().body(result);
            }

            if ("STUDENT".equals(role)) {
                // 注册学生
                String studentId = registerForm.get("studentId");
                
                if (studentId == null || studentId.trim().isEmpty()) {
                    result.put("success", false);
                    result.put("message", "请输入学号");
                    return ResponseEntity.badRequest().body(result);
                }
                
                // 检查学号是否已存在
                Student existingById = studentService.getStudentByStudentId(studentId);
                if (existingById != null) {
                    result.put("success", false);
                    result.put("message", "该学号已被注册");
                    return ResponseEntity.badRequest().body(result);
                }
                
                Student newStudent = new Student();
                newStudent.setStudentId(studentId);
                newStudent.setUsername(username);
                newStudent.setRealName(realName);
                newStudent.setPassword(password); // 实际项目中应该加密存储
                
                Student created = studentService.createStudent(newStudent);
                
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", created.getId());
                userData.put("username", username);
                userData.put("realName", realName);
                userData.put("role", "STUDENT");
                userData.put("studentId", studentId);
                
                result.put("success", true);
                result.put("message", "学生账号注册成功");
                result.put("user", userData);
                
            } else if ("TEACHER".equals(role)) {
                // 注册教师
                String teacherId = registerForm.get("teacherId");
                
                if (teacherId == null || teacherId.trim().isEmpty()) {
                    result.put("success", false);
                    result.put("message", "请输入工号");
                    return ResponseEntity.badRequest().body(result);
                }
                
                // 检查工号是否已存在
                Teacher existingById = teacherService.findByTeacherId(teacherId);
                if (existingById != null) {
                    result.put("success", false);
                    result.put("message", "该工号已被注册");
                    return ResponseEntity.badRequest().body(result);
                }
                
                Teacher newTeacher = new Teacher();
                newTeacher.setTeacherId(teacherId);
                newTeacher.setUsername(username);
                newTeacher.setRealName(realName);
                newTeacher.setPassword(password);
                
                Teacher created = teacherService.createTeacher(newTeacher);
                
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", created.getId());
                userData.put("username", username);
                userData.put("realName", realName);
                userData.put("role", "TEACHER");
                userData.put("teacherId", teacherId);
                
                result.put("success", true);
                result.put("message", "教师账号注册成功");
                result.put("user", userData);
                
            } else {
                result.put("success", false);
                result.put("message", "无效的角色类型");
                return ResponseEntity.badRequest().body(result);
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "注册失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}
