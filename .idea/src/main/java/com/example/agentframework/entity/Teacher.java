package com.example.agentframework.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Teacher {
    private Long id;
    private String teacherId;
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private String department;
    private Date createdAt;
    private Date updatedAt;
}
