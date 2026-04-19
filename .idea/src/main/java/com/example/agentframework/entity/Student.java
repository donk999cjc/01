package com.example.agentframework.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    private Long id;
    private String studentId;
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private String classes;
    private Date createdAt;
    private Date updatedAt;
}
