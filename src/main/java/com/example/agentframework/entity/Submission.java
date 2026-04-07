package com.example.agentframework.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Submission {
    private Long id;
    private String submissionId;
    private String studentId;
    private String assignmentId;
    private String content;
    private Double score;
    private String feedback;
    private String status;
    private Date submittedAt;
    private Date gradedAt;
}
