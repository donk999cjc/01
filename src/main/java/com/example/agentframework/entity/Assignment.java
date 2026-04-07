package com.example.agentframework.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {
    private Long id;
    private String assignmentId;
    private String courseId;
    private String title;
    private String content;
    private Date deadline;
    private Date createdAt;
}
