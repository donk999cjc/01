package com.example.agentframework.service.impl;

import com.example.agentframework.entity.Student;
import com.example.agentframework.entity.Submission;
import com.example.agentframework.service.StudentService;
import com.example.agentframework.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl {

    @Autowired
    private StudentService studentService;

    @Autowired
    private SubmissionService submissionService;

    /**
     * 分析学生学情
     */
    public Map<String, Object> analyzeStudentPerformance(String studentId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取学生信息
        Student student = studentService.getStudentByStudentId(studentId);
        if (student == null) {
            result.put("success", false);
            result.put("message", "学生不存在");
            return result;
        }
        
        result.put("student", student);
        
        // 获取学生所有作业提交
        List<Submission> submissions = submissionService.getSubmissionsByStudentId(studentId);
        result.put("totalSubmissions", submissions.size());
        
        // 计算成绩统计
        List<Submission> gradedSubmissions = submissions.stream()
                .filter(s -> s.getScore() != null)
                .collect(Collectors.toList());
        
        result.put("gradedSubmissions", gradedSubmissions.size());
        
        if (!gradedSubmissions.isEmpty()) {
            double averageScore = gradedSubmissions.stream()
                    .mapToDouble(Submission::getScore)
                    .average()
                    .orElse(0);
            result.put("averageScore", Math.round(averageScore * 100) / 100.0);
            
            double highestScore = gradedSubmissions.stream()
                    .mapToDouble(Submission::getScore)
                    .max()
                    .orElse(0);
            result.put("highestScore", highestScore);
            
            double lowestScore = gradedSubmissions.stream()
                    .mapToDouble(Submission::getScore)
                    .min()
                    .orElse(0);
            result.put("lowestScore", lowestScore);
            
            // 计算进步趋势
            List<Map<String, Object>> trend = calculateTrend(gradedSubmissions);
            result.put("trend", trend);
            
            // 分析薄弱环节
            List<Map<String, Object>> weaknesses = analyzeWeaknesses(gradedSubmissions);
            result.put("weaknesses", weaknesses);
        }
        
        // 生成学情预警
        List<Map<String, Object>> alerts = generateAlerts(submissions, studentId);
        result.put("alerts", alerts);
        
        result.put("success", true);
        return result;
    }

    /**
     * 计算成绩趋势
     */
    private List<Map<String, Object>> calculateTrend(List<Submission> submissions) {
        List<Map<String, Object>> trend = new ArrayList<>();
        
        // 按提交时间排序
        submissions.sort(Comparator.comparing(Submission::getSubmittedAt));
        
        for (Submission submission : submissions) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("assignmentId", submission.getAssignmentId());
            entry.put("score", submission.getScore());
            entry.put("submitTime", submission.getSubmittedAt());
            trend.add(entry);
        }
        
        return trend;
    }

    /**
     * 分析薄弱环节
     */
    private List<Map<String, Object>> analyzeWeaknesses(List<Submission> submissions) {
        List<Map<String, Object>> weaknesses = new ArrayList<>();
        
        // 按成绩分组
        Map<String, List<Submission>> lowScoreSubmissions = submissions.stream()
                .filter(s -> s.getScore() < 60)
                .collect(Collectors.groupingBy(Submission::getAssignmentId));
        
        for (Map.Entry<String, List<Submission>> entry : lowScoreSubmissions.entrySet()) {
            Map<String, Object> weakness = new HashMap<>();
            weakness.put("assignmentId", entry.getKey());
            weakness.put("count", entry.getValue().size());
            weakness.put("averageScore", entry.getValue().stream()
                    .mapToDouble(Submission::getScore)
                    .average()
                    .orElse(0));
            weaknesses.add(weakness);
        }
        
        return weaknesses;
    }

    /**
     * 生成学情预警
     */
    private List<Map<String, Object>> generateAlerts(List<Submission> submissions, String studentId) {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        // 检查未提交的作业
        long pendingSubmissions = submissions.stream()
                .filter(s -> "pending".equals(s.getStatus()))
                .count();
        
        if (pendingSubmissions > 2) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "warning");
            alert.put("message", "您有" + pendingSubmissions + "个作业尚未提交，请及时完成");
            alert.put("severity", "high");
            alert.put("timestamp", new Date());
            alerts.add(alert);
        }
        
        // 检查成绩下滑
        List<Submission> recentSubmissions = submissions.stream()
                .filter(s -> s.getScore() != null)
                .sorted(Comparator.comparing(Submission::getSubmittedAt).reversed())
                .limit(3)
                .collect(Collectors.toList());
        
        if (recentSubmissions.size() >= 2) {
            double recentAvg = recentSubmissions.subList(0, 2).stream()
                    .mapToDouble(Submission::getScore)
                    .average()
                    .orElse(0);
            
            double previousAvg = recentSubmissions.size() > 2 ?
                    recentSubmissions.get(2).getScore() : 0;
            
            if (previousAvg > 0 && recentAvg < previousAvg - 10) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("type", "performance_drop");
                alert.put("message", "您的最近作业成绩有明显下滑，请加强学习");
                alert.put("severity", "medium");
                alert.put("timestamp", new Date());
                alerts.add(alert);
            }
        }
        
        // 检查连续低分
        long lowScoreCount = submissions.stream()
                .filter(s -> s.getScore() != null && s.getScore() < 60)
                .count();
        
        if (lowScoreCount >= 2) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "low_score");
            alert.put("message", "您有" + lowScoreCount + "个作业成绩低于60分，建议寻求帮助");
            alert.put("severity", "high");
            alert.put("timestamp", new Date());
            alerts.add(alert);
        }
        
        return alerts;
    }

    /**
     * 生成增量练习
     */
    public List<Map<String, Object>> generatePractice(String studentId, String courseId, int count) {
        List<Map<String, Object>> practices = new ArrayList<>();
        
        // 分析学生薄弱环节
        Map<String, Object> analysis = analyzeStudentPerformance(studentId);
        List<Map<String, Object>> weaknesses = (List<Map<String, Object>>) analysis.get("weaknesses");
        
        // 基于薄弱环节生成练习
        if (weaknesses != null && !weaknesses.isEmpty()) {
            for (int i = 0; i < Math.min(count, weaknesses.size()); i++) {
                Map<String, Object> practice = new HashMap<>();
                Map<String, Object> weakness = weaknesses.get(i);
                
                practice.put("id", "practice_" + System.currentTimeMillis() + "_" + i);
                practice.put("type", "remedial");
                practice.put("topic", "针对作业 " + weakness.get("assignmentId") + " 的补充练习");
                practice.put("difficulty", 3);
                practice.put("targetScore", 60);
                practice.put("recommendedTime", 30); // 分钟
                
                practices.add(practice);
            }
        }
        
        // 生成基础巩固练习
        for (int i = practices.size(); i < count; i++) {
            Map<String, Object> practice = new HashMap<>();
            
            practice.put("id", "practice_" + System.currentTimeMillis() + "_" + i);
            practice.put("type", "review");
            practice.put("topic", "基础知识巩固练习 " + (i - practices.size() + 1));
            practice.put("difficulty", 2);
            practice.put("targetScore", 80);
            practice.put("recommendedTime", 20); // 分钟
            
            practices.add(practice);
        }
        
        return practices;
    }

    /**
     * 分析班级学情
     */
    public Map<String, Object> analyzeClassPerformance(String courseId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取所有学生
        List<Student> students = studentService.getStudentsByCourseId(courseId);
        result.put("totalStudents", students.size());
        
        // 分析每个学生的成绩
        List<Map<String, Object>> studentPerformances = new ArrayList<>();
        double classAverage = 0;
        int gradedCount = 0;
        
        for (Student student : students) {
            Map<String, Object> studentAnalysis = analyzeStudentPerformance(student.getStudentId());
            if ((Boolean) studentAnalysis.get("success")) {
                Double averageScore = (Double) studentAnalysis.get("averageScore");
                if (averageScore != null) {
                    classAverage += averageScore;
                    gradedCount++;
                }
                studentPerformances.add(studentAnalysis);
            }
        }
        
        if (gradedCount > 0) {
            result.put("classAverageScore", Math.round((classAverage / gradedCount) * 100) / 100.0);
        }
        
        result.put("studentPerformances", studentPerformances);
        
        // 生成班级预警
        List<Map<String, Object>> classAlerts = generateClassAlerts(studentPerformances);
        result.put("classAlerts", classAlerts);
        
        return result;
    }

    /**
     * 生成班级预警
     */
    private List<Map<String, Object>> generateClassAlerts(List<Map<String, Object>> studentPerformances) {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        // 统计低分学生数量
        long lowScoreStudents = studentPerformances.stream()
                .filter(s -> {
                    Double avgScore = (Double) s.get("averageScore");
                    return avgScore != null && avgScore < 60;
                })
                .count();
        
        if (lowScoreStudents > 3) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "class_low_performance");
            alert.put("message", "班级中有" + lowScoreStudents + "名学生成绩低于60分，需要关注");
            alert.put("severity", "high");
            alert.put("timestamp", new Date());
            alerts.add(alert);
        }
        
        // 统计未提交作业的学生
        long pendingStudents = studentPerformances.stream()
                .filter(s -> {
                    List<Map<String, Object>> studentAlerts = (List<Map<String, Object>>) s.get("alerts");
                    return studentAlerts != null && !studentAlerts.isEmpty();
                })
                .count();
        
        if (pendingStudents > 5) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "class_pending_submissions");
            alert.put("message", "班级中有" + pendingStudents + "名学生有未提交的作业");
            alert.put("severity", "medium");
            alert.put("timestamp", new Date());
            alerts.add(alert);
        }
        
        return alerts;
    }
}