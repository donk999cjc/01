package com.example.agentframework.controller;

import com.example.agentframework.integration.IntegrationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/integration")
@CrossOrigin
public class IntegrationController {

    @Autowired
    private IntegrationManager integrationManager;

    @GetMapping("/platforms")
    public ResponseEntity<List<Map<String, Object>>> getAvailablePlatforms() {
        List<Map<String, Object>> platforms = integrationManager.getAvailablePlatforms();
        return ResponseEntity.ok(platforms);
    }

    @PostMapping("/{platform}/auth")
    public ResponseEntity<Map<String, Object>> authenticate(
            @PathVariable String platform,
            @RequestBody Map<String, String> credentials) {
        String cred = credentials.get("credentials");
        Map<String, Object> result = integrationManager.authenticate(platform, cred);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{platform}/courses")
    public ResponseEntity<List<Map<String, Object>>> getCourses(
            @PathVariable String platform,
            @RequestHeader("Authorization") String token) {
        List<Map<String, Object>> courses = integrationManager.getCourses(platform, token);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{platform}/courses/{courseId}/students")
    public ResponseEntity<List<Map<String, Object>>> getStudents(
            @PathVariable String platform,
            @PathVariable String courseId,
            @RequestHeader("Authorization") String token) {
        List<Map<String, Object>> students = integrationManager.getStudents(platform, token, courseId);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{platform}/courses/{courseId}/assignments")
    public ResponseEntity<List<Map<String, Object>>> getAssignments(
            @PathVariable String platform,
            @PathVariable String courseId,
            @RequestHeader("Authorization") String token) {
        List<Map<String, Object>> assignments = integrationManager.getAssignments(platform, token, courseId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/{platform}/assignments/{assignmentId}")
    public ResponseEntity<Map<String, Object>> getAssignmentDetail(
            @PathVariable String platform,
            @PathVariable String assignmentId,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> detail = integrationManager.getAssignmentDetail(platform, token, assignmentId);
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/{platform}/assignments/{assignmentId}/submissions")
    public ResponseEntity<List<Map<String, Object>>> getSubmissions(
            @PathVariable String platform,
            @PathVariable String assignmentId,
            @RequestHeader("Authorization") String token) {
        List<Map<String, Object>> submissions = integrationManager.getSubmissions(platform, token, assignmentId);
        return ResponseEntity.ok(submissions);
    }

    @PostMapping("/{platform}/submissions/{submissionId}/grade")
    public ResponseEntity<Map<String, Object>> submitGrade(
            @PathVariable String platform,
            @PathVariable String submissionId,
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> gradeData) {
        double score = Double.parseDouble(gradeData.get("score").toString());
        String feedback = (String) gradeData.get("feedback");
        
        boolean success = integrationManager.submitGrade(platform, token, submissionId, score, feedback);
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", success);
        result.put("submissionId", submissionId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{platform}/notify")
    public ResponseEntity<Map<String, Object>> pushNotification(
            @PathVariable String platform,
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> notification) {
        
        @SuppressWarnings("unchecked")
        List<String> userIds = (List<String>) notification.get("userIds");
        String title = (String) notification.get("title");
        String content = (String) notification.get("content");
        
        boolean success = integrationManager.pushNotification(platform, token, userIds, title, content);
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", success);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{platform}/courses/{courseId}/sync")
    public ResponseEntity<Map<String, Object>> syncCourse(
            @PathVariable String platform,
            @PathVariable String courseId,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> result = integrationManager.syncCourse(platform, token, courseId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{platform}/courses/{courseId}/students/{studentId}/progress")
    public ResponseEntity<Map<String, Object>> syncStudentProgress(
            @PathVariable String platform,
            @PathVariable String courseId,
            @PathVariable String studentId,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> result = integrationManager.syncStudentProgress(platform, token, courseId, studentId);
        return ResponseEntity.ok(result);
    }
}
