package com.example.agentframework.knowledge;

import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Data
public class KnowledgeNode {
    private String id;
    private String title;
    private String content;
    private String type;
    private String courseId;
    private List<String> tags;
    private List<String> prerequisites;
    private List<String> related;
    private int difficulty;
    private Map<String, Object> metadata;
    private double relevanceScore;

    public KnowledgeNode() {
        this.metadata = new HashMap<>();
        this.relevanceScore = 1.0;
    }

    public void addTag(String tag) {
        if (tags != null && !tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void addPrerequisite(String prerequisiteId) {
        if (prerequisites != null && !prerequisites.contains(prerequisiteId)) {
            prerequisites.add(prerequisiteId);
        }
    }

    public void addRelated(String relatedId) {
        if (related != null && !related.contains(relatedId)) {
            related.add(relatedId);
        }
    }
}
