package com.example.agentframework.integration.impl;

import com.example.agentframework.integration.TeachingPlatformIntegration;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DingTalkIntegration implements TeachingPlatformIntegration {

    private static final String PLATFORM_NAME = "钉钉教育";
    private static final String PLATFORM_CODE = "dingtalk";
    private static final String API_BASE_URL = "https://api.dingtalk.com";

    @Override
    public String getPlatformName() {
        return PLATFORM_NAME;
    }

    @Override
    public String getPlatformCode() {
        return PLATFORM_CODE;
    }

    @Override
    public boolean testConnection() {
        return true;
    }

    @Override
    public Map<String, Object> authenticate(String credentials) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("accessToken", "dingtalk_access_token_" + System.currentTimeMillis());
        result.put("expiresIn", 7200);
        result.put("corpId", "dingtalk_corp_001");
        result.put("userId", "dingtalk_user_001");
        result.put("userName", "钉钉用户");
        return result;
    }

    @Override
    public List<Map<String, Object>> getCourses(String token) {
        List<Map<String, Object>> courses = new ArrayList<>();
        
        Map<String, Object> course1 = new HashMap<>();
        course1.put("courseId", "dingtalk_course_001");
        course1.put("courseName", "Python程序设计");
        course1.put("teacherName", "王老师");
        course1.put("studentCount", 85);
        course1.put("classId", "dingtalk_class_001");
        course1.put("className", "计算机2101班");
        courses.add(course1);
        
        Map<String, Object> course2 = new HashMap<>();
        course2.put("courseId", "dingtalk_course_002");
        course2.put("courseName", "数据结构与算法");
        course2.put("teacherName", "赵老师");
        course2.put("studentCount", 92);
        course2.put("classId", "dingtalk_class_002");
        course2.put("className", "计算机2102班");
        courses.add(course2);
        
        return courses;
    }

    @Override
    public List<Map<String, Object>> getStudents(String token, String courseId) {
        List<Map<String, Object>> students = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> student = new HashMap<>();
            student.put("studentId", "dingtalk_student_" + String.format("%03d", i));
            student.put("studentName", "钉钉学生" + i);
            student.put("studentNo", "202100" + String.format("%03d", i));
            student.put("className", "计算机2101");
            student.put("dingTalkId", "dingtalk_user_" + String.format("%03d", i));
            students.add(student);
        }
        
        return students;
    }

    @Override
    public List<Map<String, Object>> getAssignments(String token, String courseId) {
        List<Map<String, Object>> assignments = new ArrayList<>();
        
        Map<String, Object> assignment1 = new HashMap<>();
        assignment1.put("assignmentId", "dingtalk_assign_001");
        assignment1.put("title", "Python基础练习");
        assignment1.put("type", "homework");
        assignment1.put("deadline", "2026-04-10 23:59:59");
        assignment1.put("totalSubmissions", 78);
        assignment1.put("gradedSubmissions", 50);
        assignments.add(assignment1);
        
        Map<String, Object> assignment2 = new HashMap<>();
        assignment2.put("assignmentId", "dingtalk_assign_002");
        assignment2.put("title", "算法设计作业");
        assignment2.put("type", "homework");
        assignment2.put("deadline", "2026-04-18 23:59:59");
        assignment2.put("totalSubmissions", 65);
        assignment2.put("gradedSubmissions", 30);
        assignments.add(assignment2);
        
        return assignments;
    }

    @Override
    public Map<String, Object> getAssignmentDetail(String token, String assignmentId) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("assignmentId", assignmentId);
        detail.put("title", "Python基础练习");
        detail.put("description", "完成Python基础语法练习，包括变量、数据类型、控制结构等");
        detail.put("totalScore", 100);
        detail.put("deadline", "2026-04-10 23:59:59");
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("name", "练习题.pdf");
        attachment.put("url", "https://example.com/dingtalk/file1.pdf");
        detail.put("attachments", Arrays.asList(attachment));
        return detail;
    }

    @Override
    public List<Map<String, Object>> getSubmissions(String token, String assignmentId) {
        List<Map<String, Object>> submissions = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> submission = new HashMap<>();
            submission.put("submissionId", "dingtalk_submit_" + String.format("%03d", i));
            submission.put("studentId", "dingtalk_student_" + String.format("%03d", i));
            submission.put("studentName", "钉钉学生" + i);
            submission.put("submitTime", "2026-03-2" + i + " 14:00:00");
            submission.put("status", i <= 2 ? "graded" : "pending");
            submission.put("score", i <= 2 ? 90 + i : null);
            submissions.add(submission);
        }
        
        return submissions;
    }

    @Override
    public boolean submitGrade(String token, String submissionId, double score, String feedback) {
        return true;
    }

    @Override
    public boolean pushNotification(String token, List<String> userIds, String title, String content) {
        return true;
    }

    @Override
    public Map<String, Object> syncCourse(String token, String courseId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("courseId", courseId);
        result.put("syncedStudents", 85);
        result.put("syncedAssignments", 3);
        result.put("syncTime", new Date());
        return result;
    }

    @Override
    public Map<String, Object> syncStudentProgress(String token, String courseId, String studentId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("studentId", studentId);
        result.put("courseId", courseId);
        result.put("progress", 68.0);
        result.put("completedAssignments", 6);
        result.put("totalAssignments", 8);
        result.put("averageScore", 78.5);
        result.put("lastActiveTime", "2026-03-29 16:45:00");
        return result;
    }
}
