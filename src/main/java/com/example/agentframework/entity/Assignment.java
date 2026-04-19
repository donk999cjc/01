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

    // 老师上传的作业文件路径（数据库字段是 file_path）
    private String filePath;

    // 老师上传的作业文件名（数据库字段是 file_name）
    private String fileName;
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private String homeworkType;       // 作业类型：WRITTEN / CODE / REPORT / QUIZ
    private String gradingStandard;   // 评分标准（AI批改用）
    private Boolean allowLate;        // 是否允许迟交
    private Integer latePenalty;      // 每日扣分比例 %
    private String attachmentUrl;     // 作业附件地址
}
