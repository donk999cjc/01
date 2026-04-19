package com.example.agentframework.integration;

import java.util.List;
import java.util.Map;

public interface TeachingPlatformIntegration {
    String getPlatformName();
    String getPlatformCode();
    boolean testConnection();
    Map<String, Object> authenticate(String credentials);
    List<Map<String, Object>> getCourses(String token);
    List<Map<String, Object>> getStudents(String token, String courseId);
    List<Map<String, Object>> getAssignments(String token, String courseId);
    Map<String, Object> getAssignmentDetail(String token, String assignmentId);
    List<Map<String, Object>> getSubmissions(String token, String assignmentId);
    boolean submitGrade(String token, String submissionId, double score, String feedback);
    boolean pushNotification(String token, List<String> userIds, String title, String content);
    Map<String, Object> syncCourse(String token, String courseId);
    Map<String, Object> syncStudentProgress(String token, String courseId, String studentId);
}
