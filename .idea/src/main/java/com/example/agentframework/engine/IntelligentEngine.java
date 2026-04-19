package com.example.agentframework.engine;

import java.util.List;
import java.util.Map;

public interface IntelligentEngine {
    Map<String, Object> analyze(String content, String type);
    List<Map<String, Object>> extractKeywords(String content);
    double calculateSimilarity(String text1, String text2);
    String generateSummary(String content, int maxLength);
    List<String> suggestCorrections(String content, String type);
    Map<String, Object> evaluateAnswer(String studentAnswer, String standardAnswer);
    List<Map<String, Object>> generateQuestions(String content, int count);
    String generateFeedback(Map<String, Object> analysisResult);
    double predictScore(Map<String, Object> features);
    List<String> recommendResources(String userId, String courseId);
}
