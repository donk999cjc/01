package com.example.agentframework.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private String role;
    private Integer status;
    private String studentId;
    private String teacherId;
    private String department;
    private Date lastLoginTime;
    private Date createdAt;
    private Date updatedAt;
}
