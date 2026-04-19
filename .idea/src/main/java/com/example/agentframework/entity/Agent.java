package com.example.agentframework.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agent {
    private Long id;
    private String name;
    private String courseId;
    private String description;
    private String config;
    private String avatar;
    private Date createdAt;
}
