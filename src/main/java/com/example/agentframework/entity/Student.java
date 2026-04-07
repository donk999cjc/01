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
    private String name;
    private String courses;
    private Date createdAt;
}
