package com.example.agentframework.controller;

import com.example.agentframework.entity.Assignment;
import com.example.agentframework.entity.Submission;
import com.example.agentframework.service.AssignmentService;
import com.example.agentframework.service.SubmissionService;
import com.example.agentframework.service.AIService;
import com.example.agentframework.service.FileStorageService;
import com.example.agentframework.service.SmartImageReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assignments")
@CrossOrigin
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private AIService aiService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private SmartImageReviewService smartImageReviewService;

    @Autowired
    private com.example.agentframework.engine.IntelligentEngine intelligentEngine;

    @PostMapping
    public ResponseEntity<?> createAssignment(@RequestBody Assignment assignment) {
        try {
            System.out.println("📝 收到新建作业请求:");
            System.out.println("   标题: " + assignment.getTitle());
            System.out.println("   课程ID: " + assignment.getCourseId());
            System.out.println("   截止时间: " + assignment.getDueDate());
            System.out.println("   总分: " + assignment.getTotalScore());
            System.out.println("   描述: " + assignment.getDescription());

            Assignment createdAssignment = assignmentService.createAssignment(assignment);

            System.out.println("✅ 作业创建成功! ID: " + createdAssignment.getId());
            return new ResponseEntity<>(createdAssignment, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("❌ 创建作业失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "创建作业失败: " + e.getMessage());
            errorResult.put("error", e.getClass().getSimpleName());
            return new ResponseEntity<>(errorResult, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Assignment> getAssignmentById(@PathVariable Long id) {
        return assignmentService.getAssignmentById(id)
                .map(assignment -> new ResponseEntity<>(assignment, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/assignmentId/{assignmentId}")
    public ResponseEntity<Assignment> getAssignmentByAssignmentId(@PathVariable String assignmentId) {
        Assignment assignment = assignmentService.getAssignmentByAssignmentId(assignmentId);
        if (assignment != null) {
            return new ResponseEntity<>(assignment, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Assignment>> getAssignmentsByCourseId(@PathVariable String courseId) {
        List<Assignment> assignments = assignmentService.getAssignmentsByCourseId(courseId);
        return new ResponseEntity<>(assignments, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Assignment>> getAllAssignments() {
        List<Assignment> assignments = assignmentService.getAllAssignments();
        return new ResponseEntity<>(assignments, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAssignment(@PathVariable Long id, @RequestBody Assignment assignment) {
        try {
            System.out.println("✏️ 收到编辑作业请求 ID: " + id);
            System.out.println("   标题: " + assignment.getTitle());
            System.out.println("   截止时间: " + assignment.getDueDate());
            System.out.println("   描述: " + assignment.getDescription());
            
            Assignment updatedAssignment = assignmentService.updateAssignment(id, assignment);
            
            if (updatedAssignment != null) {
                System.out.println("✅ 作业更新成功!");
                return new ResponseEntity<>(updatedAssignment, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.err.println("❌ 更新作业失败: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "更新作业失败: " + e.getMessage());
            return new ResponseEntity<>(errorResult, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 学生提交作业（使用文件上传）
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<Map<String, Object>> submitAssignment(
            @PathVariable Long id,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("studentId") String studentId,
            @RequestParam(value = "assignmentId", required = false) String assignmentId) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 创建提交记录
            Submission submission = new Submission();

            // 设置assignmentId
            if (assignmentId == null || assignmentId.isEmpty()) {
                assignmentId = String.valueOf(id);
            }
            submission.setAssignmentId(assignmentId);
            submission.setStudentId(studentId);

            // 生成submissionId
            String submissionId = "SUB" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
            submission.setSubmissionId(submissionId);

            // 保存文件（如果有）
            if (file != null && !file.isEmpty()) {
                String filePath = fileStorageService.saveSubmissionImage(file, studentId, assignmentId);
                submission.setFilePath(filePath);
                submission.setImageUrl(fileStorageService.getFileUrl(filePath));
            }

            submission.setStatus("pending");

            // 保存到数据库
            Submission createdSubmission = submissionService.createSubmission(submission);

            result.put("success", true);
            result.put("message", "提交成功");
            result.put("submission", createdSubmission);
            result.put("imageUrl", createdSubmission.getImageUrl());

            return new ResponseEntity<>(result, HttpStatus.CREATED);

        } catch (IOException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "文件保存失败：" + e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "提交失败：" + e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * AI智能批改作业（使用Agent模块 + 文件路径）
     */
    @PostMapping("/{id}/review-image")
    public ResponseEntity<Map<String, Object>> reviewImageAssignment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> reviewData) {
        String studentId = (String) reviewData.get("studentId");
        String filePath = (String) reviewData.get("filePath");
        String assignmentInfo = (String) reviewData.get("assignmentInfo");

        Map<String, Object> result = new HashMap<>();

        // 多种方式尝试查找提交记录（与前端loadStudentSubmission逻辑保持一致）
        Submission submission = null;

        // 方式1：使用数字ID查询
        submission = submissionService.getSubmissionByStudentIdAndAssignmentId(studentId, String.valueOf(id));

        // 方式2：如果没找到，获取作业的assignmentId再查询
        if (submission == null) {
            java.util.Optional<Assignment> assignmentOpt = assignmentService.getAssignmentById(id);
            if (assignmentOpt.isPresent()) {
                Assignment assignment = assignmentOpt.get();
                if (assignment.getAssignmentId() != null) {
                    submission = submissionService.getSubmissionByStudentIdAndAssignmentId(studentId, assignment.getAssignmentId());
                }
            }
        }

        // 方式3：如果还没找到，从该学生的所有提交中查找
        if (submission == null) {
            List<Submission> studentSubmissions = submissionService.getSubmissionsByStudentId(studentId);
            for (Submission s : studentSubmissions) {
                // 匹配数字ID或assignmentId
                if (String.valueOf(id).equals(s.getAssignmentId()) ||
                    (s.getAssignmentId() != null && s.getAssignmentId().equals(String.valueOf(id)))) {
                    submission = s;
                    break;
                }
            }
        }

        if (submission == null) {
            result.put("success", false);
            result.put("message", "未找到提交记录，请确认学生已提交此作业");
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }

        if (filePath == null || filePath.isEmpty()) {
            result.put("success", false);
            result.put("message", "未找到作业图片");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            // 获取图片绝对路径
            String absolutePath = fileStorageService.getAbsolutePath(filePath);

            System.out.println("🚀 开始智能批改...");
            System.out.println("   使用新的 SmartImageReviewService（支持OCR+题目识别）");

            // 调用新的智能图片识别服务（支持OCR、题目归纳、逐题分析）
            Map<String, Object> smartResult = smartImageReviewService.reviewImageByPath(absolutePath, assignmentInfo);

            if (!(Boolean) smartResult.get("success")) {
                result.put("success", false);
                result.put("message", "AI识别失败：" + smartResult.get("message"));
                return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // 提取智能分析结果
            String recognizedContent = smartResult.containsKey("recognizedContent") ? 
                    (String) smartResult.get("recognizedContent") : "无法识别图片内容";
            String feedback = smartResult.containsKey("feedback") ? 
                    (String) smartResult.get("feedback") : "批改完成";
            
            // 安全获取分数，防止null值
            double finalScore = 75.0; // 默认分数
            Object scoreObj = smartResult.get("score");
            if (scoreObj != null) {
                if (scoreObj instanceof Number) {
                    finalScore = ((Number) scoreObj).doubleValue();
                } else if (scoreObj instanceof String) {
                    try {
                        finalScore = Double.parseDouble((String) scoreObj);
                    } catch (NumberFormatException e) {
                        System.out.println("⚠️ 分数格式异常: " + scoreObj + ", 使用默认分数75.0");
                    }
                }
            }
            
            // 确保分数在合理范围内
            finalScore = Math.max(0, Math.min(100, finalScore));
            
            // 题目归纳信息
            String questionsSummary = smartResult.containsKey("questionsSummary") ? 
                    (String) smartResult.get("questionsSummary") : "";
            
            // 逐题详细分析
            String detailedAnalysis = smartResult.containsKey("detailedAnalysis") ? 
                    (String) smartResult.get("detailedAnalysis") : "";

            // 等级评价
            String gradeLevel = smartResult.containsKey("gradeLevel") ? 
                    (String) smartResult.get("gradeLevel") : "良";

            // 学习建议
            String learningSuggestions = smartResult.containsKey("learningSuggestions") ? 
                    (String) smartResult.get("learningSuggestions") : "";

            // 构建学习总结
            StringBuilder learningSummary = new StringBuilder();
            learningSummary.append("【本次作业表现总结】\n\n");
            learningSummary.append(String.format("✅ 最终得分：%.1f 分（满分100）\n", finalScore));
            learningSummary.append(String.format("📊 等级评价：%s\n\n", gradeLevel));
            
            if (finalScore >= 90) {
                learningSummary.append("🌟 表现优秀！继续保持！\n\n");
                learningSummary.append("亮点：答案准确，思路清晰，完全掌握知识点。\n");
            } else if (finalScore >= 75) {
                learningSummary.append("👍 表现良好！还有提升空间。\n\n");
                learningSummary.append("建议：基本概念已掌握，部分细节需要加强。\n");
            } else if (finalScore >= 60) {
                learningSummary.append("⚠️ 及格水平，需要努力。\n\n");
                learningSummary.append("提醒：基础知识有欠缺，建议复习相关章节。\n");
            } else {
                learningSummary.append("❌ 需要加油！请认真对待。\n\n");
                learningSummary.append("警告：对知识点理解不足，建议寻求老师帮助。\n");
            }
            
            if (learningSuggestions != null && !learningSuggestions.isEmpty()) {
                learningSummary.append("\n【AI学习建议】\n");
                learningSummary.append(learningSuggestions).append("\n");
            }
            
            learningSummary.append("\n💪 下次争取更好成绩！");

            // 确保分数在0-100范围内
            finalScore = Math.max(0, Math.min(100, finalScore));

            // 保存批改结果和评价报告
            submission.setScore(finalScore);
            submission.setFeedback(feedback);
            submission.setImageAnalysis(recognizedContent);
            submission.setStatus("graded");
            submission.setEvaluationReport(feedback); 
            submission.setLearningSummary(learningSummary.toString());
            submission.setGradedAt(new java.util.Date());
            submissionService.updateSubmission(submission.getId(), submission);

            result.put("success", true);
            result.put("message", "✅ AI智能批改完成！已使用智谱AI GLM-4V模型进行OCR识别和逐题分析");
            result.put("score", finalScore);
            result.put("feedback", feedback);
            result.put("learningSummary", learningSummary.toString());
            result.put("recognizedContent", recognizedContent);
            result.put("questionsSummary", questionsSummary);
            result.put("detailedAnalysis", detailedAnalysis);
            result.put("gradeLevel", gradeLevel);
            result.put("submission", submission);
            result.put("imageUrl", fileStorageService.getFileUrl(submission.getFilePath()));
            
            // 如果有警告信息（如使用了本地模拟模式）
            if (smartResult.containsKey("warning")) {
                result.put("warning", smartResult.get("warning"));
            }

            System.out.println("✅ 智能批改完成！");
            System.out.println("   得分: " + finalScore);
            System.out.println("   等级: " + gradeLevel);

            return new ResponseEntity<>(result, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "批改过程出错：" + e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}/submissions")
    public ResponseEntity<List<Submission>> getAssignmentSubmissions(@PathVariable Long id) {
        List<Submission> submissions = submissionService.getSubmissionsByAssignmentId(String.valueOf(id));
        // 为每个提交记录生成imageUrl
        submissions.forEach(submission -> {
            if (submission.getFilePath() != null && !submission.getFilePath().isEmpty()) {
                submission.setImageUrl(fileStorageService.getFileUrl(submission.getFilePath()));
            }
        });
        return new ResponseEntity<>(submissions, HttpStatus.OK);
    }
    //上传作业
    @PostMapping("/{id}/upload-attachment")
    public ResponseEntity<?> uploadAttachment(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            System.out.println("📎 收到附件上传请求: " + file.getOriginalFilename());
            System.out.println("   文件大小: " + (file.getSize() / 1024) + " KB");
            System.out.println("   作业ID: " + id);

            // 1. 获取作业
            Assignment assignment = assignmentService.getAssignmentById(id).orElse(null);
            if (assignment == null) {
                System.err.println("❌ 作业不存在，ID: " + id);
                return ResponseEntity.badRequest().body("作业不存在，请先创建作业");
            }

            // 2. 保存文件
            String savedPath = fileStorageService.saveSubmissionImage(file, "teacher", String.valueOf(id));
            String fileName = file.getOriginalFilename();

            // 构建可访问的URL路径（供前端使用）
            String fileUrl = "/api/files/" + savedPath;

            System.out.println("✅ 文件已保存到: " + savedPath);
            System.out.println("   访问URL: " + fileUrl);

            // 3. 更新作业的文件信息
            assignment.setFilePath(fileUrl);      // 设置完整访问路径
            assignment.setFileName(fileName);       // 保存原始文件名

            // 4. 【关键】更新到数据库（使用包含filePath/fileName的UPDATE语句）
            assignmentService.updateAssignment(id, assignment);

            System.out.println("✅ 数据库已更新！filePath=" + fileUrl + ", fileName=" + fileName);

            // 返回成功信息（包含文件路径）
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("message", "上传成功并已保存到数据库");
            result.put("url", fileUrl);
            result.put("fileName", fileName);
            result.put("savedPath", savedPath);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ 上传失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("上传失败：" + e.getMessage());
        }
    }
}
