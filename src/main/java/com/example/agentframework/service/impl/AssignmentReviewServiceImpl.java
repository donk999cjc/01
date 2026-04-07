package com.example.agentframework.service.impl;

import com.example.agentframework.entity.Submission;
import com.example.agentframework.engine.IntelligentEngine;
import com.example.agentframework.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AssignmentReviewServiceImpl {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private IntelligentEngine intelligentEngine;

    /**
     * 精细化作业批注
     */
    public Map<String, Object> reviewAssignment(Long submissionId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取提交记录
        Submission submission = submissionService.getSubmissionById(submissionId).orElse(null);
        if (submission == null) {
            result.put("success", false);
            result.put("message", "提交记录不存在");
            return result;
        }
        
        String content = submission.getContent();
        if (content == null || content.isEmpty()) {
            result.put("success", false);
            result.put("message", "作业内容为空");
            return result;
        }
        
        // 分析作业内容
        Map<String, Object> analysis = intelligentEngine.analyze(content, detectContentType(content));
        result.put("analysis", analysis);
        
        // 生成批注
        List<Map<String, Object>> comments = generateComments(content, analysis);
        result.put("comments", comments);
        
        // 计算评分
        double score = calculateScore(analysis, comments);
        result.put("score", score);
        
        // 生成总评
        Map<String, Object> feedbackParams = new HashMap<>();
        feedbackParams.put("score", score);
        String feedback = intelligentEngine.generateFeedback(feedbackParams);
        result.put("feedback", feedback);
        
        // 更新提交记录
        submission.setScore(score);
        submission.setFeedback(feedback);
        submission.setStatus("graded");
        submissionService.updateSubmission(submissionId, submission);
        
        result.put("success", true);
        result.put("submission", submission);
        
        return result;
    }

    /**
     * 检测内容类型
     */
    private String detectContentType(String content) {
        // 检测代码
        if (content.contains("public class") || content.contains("def ") || 
            content.contains("function ") || content.contains("import ") ||
            content.contains("console.log") || content.contains("print(")) {
            return "code";
        }
        return "text";
    }

    /**
     * 生成精细化批注
     */
    private List<Map<String, Object>> generateComments(String content, Map<String, Object> analysis) {
        List<Map<String, Object>> comments = new ArrayList<>();
        
        // 检测内容类型
        String contentType = detectContentType(content);
        
        if ("code".equals(contentType)) {
            comments.addAll(generateCodeComments(content, analysis));
        } else {
            comments.addAll(generateTextComments(content, analysis));
        }
        
        // 生成通用批注
        comments.addAll(generateGeneralComments(analysis));
        
        return comments;
    }

    /**
     * 生成代码批注
     */
    private List<Map<String, Object>> generateCodeComments(String content, Map<String, Object> analysis) {
        List<Map<String, Object>> comments = new ArrayList<>();
        
        // 检测代码问题
        List<String> codeIssues = intelligentEngine.suggestCorrections(content, "code");
        for (String issue : codeIssues) {
            Map<String, Object> comment = new HashMap<>();
            comment.put("type", "code_issue");
            comment.put("content", issue);
            comment.put("severity", "medium");
            comment.put("location", findCodeIssueLocation(content, issue));
            comments.add(comment);
        }
        
        // 检测代码复杂度
        Map<String, Object> codeMetrics = (Map<String, Object>) analysis.get("codeMetrics");
        if (codeMetrics != null) {
            int linesOfCode = (int) codeMetrics.get("linesOfCode");
            int commentLines = (int) codeMetrics.get("commentLines");
            
            if (commentLines == 0 && linesOfCode > 10) {
                Map<String, Object> comment = new HashMap<>();
                comment.put("type", "code_style");
                comment.put("content", "建议添加代码注释，提高代码可读性");
                comment.put("severity", "low");
                Map<String, Object> location = new HashMap<>();
                location.put("line", 1);
                location.put("column", 1);
                comment.put("location", location);
                comments.add(comment);
            }
        }
        
        return comments;
    }

    /**
     * 生成文本批注
     */
    private List<Map<String, Object>> generateTextComments(String content, Map<String, Object> analysis) {
        List<Map<String, Object>> comments = new ArrayList<>();
        
        // 检测语法问题
        List<String> corrections = intelligentEngine.suggestCorrections(content, "text");
        for (String correction : corrections) {
            Map<String, Object> comment = new HashMap<>();
            comment.put("type", "grammar");
            comment.put("content", correction);
            comment.put("severity", "low");
            comment.put("location", findTextIssueLocation(content, correction));
            comments.add(comment);
        }
        
        // 分析关键词匹配
        List<Map<String, Object>> keywords = (List<Map<String, Object>>) analysis.get("keywords");
        if (keywords != null && !keywords.isEmpty()) {
            // 检测是否缺少关键概念
            List<String> importantKeywords = keywords.stream()
                    .filter(k -> (double) k.get("weight") > 0.1)
                    .map(k -> (String) k.get("word"))
                    .collect(java.util.stream.Collectors.toList());
            
            if (importantKeywords.size() < 3) {
                Map<String, Object> comment = new HashMap<>();
                comment.put("type", "content");
                comment.put("content", "建议增加对核心概念的阐述，使回答更加完整");
                comment.put("severity", "medium");
                Map<String, Object> location = new HashMap<>();
                location.put("line", 1);
                location.put("column", 1);
                comment.put("location", location);
                comments.add(comment);
            }
        }
        
        return comments;
    }

    /**
     * 生成通用批注
     */
    private List<Map<String, Object>> generateGeneralComments(Map<String, Object> analysis) {
        List<Map<String, Object>> comments = new ArrayList<>();
        
        // 分析可读性
        Double readabilityScore = (Double) analysis.get("readabilityScore");
        if (readabilityScore != null && readabilityScore < 60) {
            Map<String, Object> comment = new HashMap<>();
            comment.put("type", "readability");
            comment.put("content", "建议优化语言表达，提高回答的可读性");
            comment.put("severity", "low");
            Map<String, Object> location = new HashMap<>();
            location.put("line", 1);
            location.put("column", 1);
            comment.put("location", location);
            comments.add(comment);
        }
        
        // 分析长度
        Integer length = (Integer) analysis.get("length");
        if (length != null) {
            if (length < 100) {
                Map<String, Object> comment = new HashMap<>();
                comment.put("type", "content");
                comment.put("content", "回答过于简短，建议增加详细说明");
                comment.put("severity", "medium");
                Map<String, Object> location = new HashMap<>();
                location.put("line", 1);
                location.put("column", 1);
                comment.put("location", location);
                comments.add(comment);
            } else if (length > 1000) {
                Map<String, Object> comment = new HashMap<>();
                comment.put("type", "content");
                comment.put("content", "回答过于冗长，建议精简表达");
                comment.put("severity", "low");
                Map<String, Object> location = new HashMap<>();
                location.put("line", 1);
                location.put("column", 1);
                comment.put("location", location);
                comments.add(comment);
            }
        }
        
        return comments;
    }

    /**
     * 计算评分
     */
    private double calculateScore(Map<String, Object> analysis, List<Map<String, Object>> comments) {
        double baseScore = 80.0;
        
        // 基于分析结果调整分数
        Double readabilityScore = (Double) analysis.get("readabilityScore");
        if (readabilityScore != null) {
            baseScore += (readabilityScore - 70) * 0.1;
        }
        
        Integer wordCount = (Integer) analysis.get("wordCount");
        if (wordCount != null) {
            if (wordCount < 50) {
                baseScore -= 10;
            } else if (wordCount > 500) {
                baseScore += 5;
            }
        }
        
        // 基于批注调整分数
        long highSeverityComments = comments.stream()
                .filter(c -> "high".equals(c.get("severity")))
                .count();
        long mediumSeverityComments = comments.stream()
                .filter(c -> "medium".equals(c.get("severity")))
                .count();
        
        baseScore -= highSeverityComments * 5;
        baseScore -= mediumSeverityComments * 2;
        
        // 确保分数在0-100之间
        return Math.max(0, Math.min(100, baseScore));
    }

    /**
     * 查找代码问题位置
     */
    private Map<String, Object> findCodeIssueLocation(String content, String issue) {
        // 简单实现，实际项目中可以使用更精确的定位
        Map<String, Object> location = new HashMap<>();
        location.put("line", 1);
        location.put("column", 1);
        return location;
    }

    /**
     * 查找文本问题位置
     */
    private Map<String, Object> findTextIssueLocation(String content, String issue) {
        Map<String, Object> location = new HashMap<>();
        location.put("line", 1);
        location.put("column", 1);
        return location;
    }

    /**
     * 批量批改作业
     */
    public List<Map<String, Object>> batchReviewAssignments(List<Long> submissionIds) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (Long submissionId : submissionIds) {
            results.add(reviewAssignment(submissionId));
        }
        
        return results;
    }

    /**
     * 生成作业统计报告
     */
    public Map<String, Object> generateReviewReport(String assignmentId) {
        Map<String, Object> report = new HashMap<>();
        
        // 获取该作业的所有提交
        List<Submission> submissions = submissionService.getSubmissionsByAssignmentId(assignmentId);
        
        report.put("totalSubmissions", submissions.size());
        
        // 计算统计数据
        List<Submission> gradedSubmissions = submissions.stream()
                .filter(s -> s.getScore() != null)
                .collect(java.util.stream.Collectors.toList());
        
        report.put("gradedSubmissions", gradedSubmissions.size());
        
        if (!gradedSubmissions.isEmpty()) {
            double averageScore = gradedSubmissions.stream()
                    .mapToDouble(Submission::getScore)
                    .average()
                    .orElse(0);
            report.put("averageScore", Math.round(averageScore * 100) / 100.0);
            
            double highestScore = gradedSubmissions.stream()
                    .mapToDouble(Submission::getScore)
                    .max()
                    .orElse(0);
            report.put("highestScore", highestScore);
            
            double lowestScore = gradedSubmissions.stream()
                    .mapToDouble(Submission::getScore)
                    .min()
                    .orElse(0);
            report.put("lowestScore", lowestScore);
            
            // 分析常见问题
            List<String> commonIssues = analyzeCommonIssues(gradedSubmissions);
            report.put("commonIssues", commonIssues);
        }
        
        return report;
    }

    /**
     * 分析常见问题
     */
    private List<String> analyzeCommonIssues(List<Submission> submissions) {
        List<String> commonIssues = new ArrayList<>();
        
        // 简单实现，实际项目中可以基于批注内容进行分析
        commonIssues.add("代码缺少注释");
        commonIssues.add("概念理解不深入");
        commonIssues.add("表达不够清晰");
        
        return commonIssues;
    }
}
