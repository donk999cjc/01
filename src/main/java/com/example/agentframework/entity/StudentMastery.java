package com.example.agentframework.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentMastery {
    private Long id;
    private String studentId;
    private String knowledgeId;
    private String courseId;
    private Double pL;
    private Double pL0;
    private Double pT;
    private Double pG;
    private Double pS;
    private Integer totalAttempts;
    private Integer correctAttempts;
    private Date lastAttemptAt;
    private Date createdAt;
    private Date updatedAt;
}
