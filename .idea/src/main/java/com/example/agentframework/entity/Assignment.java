package com.example.agentframework.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String description;        // 作业要求描述
    private Integer totalScore;        // 总分
    private String status;             // 状态：draft/published/closed
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dueDate;              // 截止时间（与前端保持一致）
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date deadline;             // 兼容旧字段
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
}
