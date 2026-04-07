package com.example.agentframework.integration;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IntegrationManager {

    private final Map<String, TeachingPlatformIntegration> integrations = new HashMap<>();

    @Autowired
    public IntegrationManager(List<TeachingPlatformIntegration> integrationList) {
        for (TeachingPlatformIntegration integration : integrationList) {
            integrations.put(integration.getPlatformCode(), integration);
        }
    }

    public List<Map<String, Object>> getAvailablePlatforms() {
        List<Map<String, Object>> platforms = new ArrayList<>();
        for (TeachingPlatformIntegration integration : integrations.values()) {
            Map<String, Object> platform = new HashMap<>();
            platform.put("code", integration.getPlatformCode());
            platform.put("name", integration.getPlatformName());
            platform.put("connected", integration.testConnection());
            platforms.add(platform);
        }
        return platforms;
    }

    public TeachingPlatformIntegration getIntegration(String platformCode) {
        return integrations.get(platformCode);
    }

    public boolean hasIntegration(String platformCode) {
        return integrations.containsKey(platformCode);
    }

    public Map<String, Object> authenticate(String platformCode, String credentials) {
        TeachingPlatformIntegration integration = integrations.get(platformCode);
        if (integration == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "不支持的平台: " + platformCode);
            return error;
        }
        return integration.authenticate(credentials);
    }

    public List<Map<String, Object>> getCourses(String platformCode, String token) {
        TeachingPlatformIntegration integration = integrations.get(platformCode);
        if (integration == null) {
            return new ArrayList<>();
        }
        return integration.getCourses(token);
    }

    public List<Map<String, Object>> getStudents(String platformCode, String token, String courseId) {
        TeachingPlatformIntegration integration = integrations.get(platformCode);
        if (integration == null) {
            return new ArrayList<>();
        }
        return integration.getStudents(token, courseId);
    }

    public List<Map<String, Object>> getAssignments(String platformCode, String token, String courseId) {
        TeachingPlatformIntegration integration = integrations.get(platformCode);
        if (integration == null) {
            return new ArrayList<>();
        }
        return integration.getAssignments(token, courseId);
    }

    public Map<String, Object> getAssignmentDetail(String platformCode, String token, String assignmentId) {
        TeachingPlatformIntegration integration = integrations.get(platformCode);
        if (integration == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "不支持的平台");
            return error;
        }
        return integration.getAssignmentDetail(token, assignmentId);
    }

    public List<Map<String, Object>> getSubmissions(String platformCode, String token, String assignmentId) {
        TeachingPlatformIntegration integration = integrations.get(platformCode);
        if (integration == null) {
            return new ArrayList<>();
        }
        return integration.getSubmissions(token, assignmentId);
    }

    public boolean submitGrade(String platformCode, String token, String submissionId, double score, String feedback) {
        TeachingPlatformIntegration integration = integrations.get(platformCode);
        if (integration == null) {
            return false;
        }
        return integration.submitGrade(token, submissionId, score, feedback);
    }

    public boolean pushNotification(String platformCode, String token, List<String> userIds, String title, String content) {
        TeachingPlatformIntegration integration = integrations.get(platformCode);
        if (integration == null) {
            return false;
        }
        return integration.pushNotification(token, userIds, title, content);
    }

    public Map<String, Object> syncCourse(String platformCode, String token, String courseId) {
        TeachingPlatformIntegration integration = integrations.get(platformCode);
        if (integration == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "不支持的平台");
            return error;
        }
        return integration.syncCourse(token, courseId);
    }

    public Map<String, Object> syncStudentProgress(String platformCode, String token, String courseId, String studentId) {
        TeachingPlatformIntegration integration = integrations.get(platformCode);
        if (integration == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "不支持的平台");
            return error;
        }
        return integration.syncStudentProgress(token, courseId, studentId);
    }
}
