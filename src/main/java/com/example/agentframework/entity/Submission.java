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

    // 文件存储路径
    private String filePath;       // 相对路径（如：submissions/S001_A001_abc12345.jpg）
    private String imageUrl;       // 访问URL（如：/files/submissions/S001_A001_abc12345.jpg）

    // AI分析结果
    private String imageAnalysis;  // 图片内容识别结果

    // 评分和反馈
    private Double score;
    private String feedback;

    // 评价报告和学习总结
    private String evaluationReport;   // 完整的评价报告（JSON格式）
    private String learningSummary;    // 学习建议总结（学生可见）

    // 状态和时间戳
    private String status;
    private Date submittedAt;
    private Date gradedAt;
}
