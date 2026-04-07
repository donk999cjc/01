package com.example.agentframework.knowledge;

import java.util.List;
import java.util.Map;

public interface KnowledgeMiddleware {
    void addKnowledge(KnowledgeNode node);
    void updateKnowledge(String id, KnowledgeNode node);
    void deleteKnowledge(String id);
    KnowledgeNode getKnowledge(String id);
    List<KnowledgeNode> searchKnowledge(String query, String courseId);
    List<KnowledgeNode> getKnowledgeByCourse(String courseId);
    List<KnowledgeNode> getKnowledgeByTags(List<String> tags);
    List<KnowledgeNode> getRelatedKnowledge(String knowledgeId);
    List<KnowledgeNode> recommendKnowledge(String userId, String courseId);
    Map<String, Object> buildKnowledgeGraph(String courseId);
    double calculateRelevance(String query, KnowledgeNode node);
    void indexKnowledge();
}
