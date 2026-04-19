package com.example.agentframework.integration.impl;

import com.example.agentframework.integration.TeachingPlatformIntegration;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ChaoxingIntegration implements TeachingPlatformIntegration {

    private static final String PLATFORM_NAME = "超星学习通";
    private static final String PLATFORM_CODE = "chaoxing";
    private static final String API_BASE_URL = "https://api.chaoxing.com";

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
        result.put("token", "chaoxing_token_" + System.currentTimeMillis());
        result.put("expiresIn", 7200);
        result.put("userId", "chaoxing_user_001");
        result.put("userName", "测试用户");
        return result;
    }

    @Override
    public List<Map<String, Object>> getCourses(String token) {
        List<Map<String, Object>> courses = new ArrayList<>();
        
        Map<String, Object> course1 = new HashMap<>();
        course1.put("courseId", "chaoxing_course_001");
        course1.put("courseName", "高等数学");
        course1.put("teacherName", "张老师");
        course1.put("studentCount", 120);
        course1.put("semester", "2025-2026-2");
        courses.add(course1);
        
        Map<String, Object> course2 = new HashMap<>();
        course2.put("courseId", "chaoxing_course_002");
        course2.put("courseName", "大学物理");
        course2.put("teacherName", "李老师");
        course2.put("studentCount", 98);
        course2.put("semester", "2025-2026-2");
        courses.add(course2);
        
        return courses;
    }

    @Override
    public List<Map<String, Object>> getStudents(String token, String courseId) {
        List<Map<String, Object>> students = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> student = new HashMap<>();
            student.put("studentId", "chaoxing_student_" + String.format("%03d", i));
            student.put("studentName", "学生" + i);
            student.put("studentNo", "202100" + String.format("%03d", i));
            student.put("className", "计算机2101");
            students.add(student);
        }
        
        return students;
    }

    @Override
    public List<Map<String, Object>> getAssignments(String token, String courseId) {
        List<Map<String, Object>> assignments = new ArrayList<>();
        
        Map<String, Object> assignment1 = new HashMap<>();
        assignment1.put("assignmentId", "chaoxing_assign_001");
        assignment1.put("title", "第一章习题");
        assignment1.put("type", "homework");
        assignment1.put("deadline", "2026-04-15 23:59:59");
        assignment1.put("totalSubmissions", 85);
        assignment1.put("gradedSubmissions", 60);
        assignments.add(assignment1);
        
        Map<String, Object> assignment2 = new HashMap<>();
        assignment2.put("assignmentId", "chaoxing_assign_002");
        assignment2.put("title", "期中测验");
        assignment2.put("type", "exam");
        assignment2.put("deadline", "2026-04-20 23:59:59");
        assignment2.put("totalSubmissions", 120);
        assignment2.put("gradedSubmissions", 120);
        assignments.add(assignment2);
        
        return assignments;
    }

    @Override
    public Map<String, Object> getAssignmentDetail(String token, String assignmentId) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("assignmentId", assignmentId);
        detail.put("title", "第一章习题");
        detail.put("description", "请完成教材第一章课后习题1-10题");
        detail.put("totalScore", 100);
        detail.put("deadline", "2026-04-15 23:59:59");
        Map<String, Object> attachment1 = new HashMap<>();
        attachment1.put("name", "习题说明.pdf");
        attachment1.put("url", "https://example.com/file1.pdf");
        Map<String, Object> attachment2 = new HashMap<>();
        attachment2.put("name", "参考答案.docx");
        attachment2.put("url", "https://example.com/file2.docx");
        detail.put("attachments", Arrays.asList(attachment1, attachment2));
        return detail;
    }

    @Override
    public List<Map<String, Object>> getSubmissions(String token, String assignmentId) {
        List<Map<String, Object>> submissions = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> submission = new HashMap<>();
            submission.put("submissionId", "chaoxing_submit_" + String.format("%03d", i));
            submission.put("studentId", "chaoxing_student_" + String.format("%03d", i));
            submission.put("studentName", "学生" + i);
            submission.put("submitTime", "2026-03-2" + i + " 10:30:00");
            submission.put("status", i <= 3 ? "graded" : "pending");
            submission.put("score", i <= 3 ? 85 + i * 2 : null);
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
        result.put("syncedStudents", 120);
        result.put("syncedAssignments", 5);
        result.put("syncTime", new Date());
        return result;
    }

    @Override
    public Map<String, Object> syncStudentProgress(String token, String courseId, String studentId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("studentId", studentId);
        result.put("courseId", courseId);
        result.put("progress", 75.5);
        result.put("completedAssignments", 8);
        result.put("totalAssignments", 10);
        result.put("averageScore", 82.5);
        result.put("lastActiveTime", "2026-03-29 15:30:00");
        return result;
    }
}
