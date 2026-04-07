package com.example.agentframework.knowledge.impl;

import com.example.agentframework.knowledge.KnowledgeMiddleware;
import com.example.agentframework.knowledge.KnowledgeNode;
import com.example.agentframework.mapper.KnowledgeNodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KnowledgeMiddlewareImpl implements KnowledgeMiddleware {

    @Autowired
    private KnowledgeNodeMapper mapper;

    private final Map<String, KnowledgeNode> knowledgeCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> invertedIndex = new ConcurrentHashMap<>();

    @Override
    public void addKnowledge(KnowledgeNode node) {
        com.example.agentframework.entity.KnowledgeNode entity = convertToEntity(node);
        mapper.insert(entity);
        node.setId(String.valueOf(entity.getId()));
        knowledgeCache.put(node.getId(), node);
        updateIndex(node);
    }

    @Override
    public void updateKnowledge(String id, KnowledgeNode node) {
        com.example.agentframework.entity.KnowledgeNode existing = mapper.findById(Long.parseLong(id));
        if (existing != null) {
            com.example.agentframework.entity.KnowledgeNode entity = convertToEntity(node);
            entity.setId(Long.parseLong(id));
            mapper.update(entity);
            knowledgeCache.put(id, node);
            updateIndex(node);
        }
    }

    @Override
    public void deleteKnowledge(String id) {
        mapper.deleteById(Long.parseLong(id));
        knowledgeCache.remove(id);
        removeFromIndex(id);
    }

    @Override
    public KnowledgeNode getKnowledge(String id) {
        if (knowledgeCache.containsKey(id)) {
            return knowledgeCache.get(id);
        }
        
        com.example.agentframework.entity.KnowledgeNode entity = mapper.findById(Long.parseLong(id));
        return entity != null ? convertToNode(entity) : null;
    }

    @Override
    public List<KnowledgeNode> searchKnowledge(String query, String courseId) {
        List<com.example.agentframework.entity.KnowledgeNode> entities = mapper.findByCourseId(courseId);
        
        return entities.stream()
                .map(this::convertToNode)
                .filter(node -> matchesQuery(node, query))
                .sorted((a, b) -> Double.compare(
                        calculateRelevance(query, b),
                        calculateRelevance(query, a)))
                .collect(Collectors.toList());
    }

    @Override
    public List<KnowledgeNode> getKnowledgeByCourse(String courseId) {
        List<com.example.agentframework.entity.KnowledgeNode> entities = mapper.findByCourseId(courseId);
        return entities.stream()
                .map(this::convertToNode)
                .collect(Collectors.toList());
    }

    @Override
    public List<KnowledgeNode> getKnowledgeByTags(List<String> tags) {
        return knowledgeCache.values().stream()
                .filter(node -> node.getTags() != null && 
                        node.getTags().stream().anyMatch(tags::contains))
                .collect(Collectors.toList());
    }

    @Override
    public List<KnowledgeNode> getRelatedKnowledge(String knowledgeId) {
        KnowledgeNode node = getKnowledge(knowledgeId);
        if (node == null || node.getRelated() == null) {
            return Collections.emptyList();
        }
        
        return node.getRelated().stream()
                .map(this::getKnowledge)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<KnowledgeNode> recommendKnowledge(String userId, String courseId) {
        List<KnowledgeNode> allKnowledge = getKnowledgeByCourse(courseId);
        
        return allKnowledge.stream()
                .sorted((a, b) -> Integer.compare(a.getDifficulty(), b.getDifficulty()))
                .limit(10)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> buildKnowledgeGraph(String courseId) {
        Map<String, Object> graph = new HashMap<>();
        List<KnowledgeNode> nodes = getKnowledgeByCourse(courseId);
        
        List<Map<String, Object>> nodeList = new ArrayList<>();
        List<Map<String, Object>> edgeList = new ArrayList<>();
        
        for (KnowledgeNode node : nodes) {
            Map<String, Object> nodeMap = new HashMap<>();
            nodeMap.put("id", node.getId());
            nodeMap.put("title", node.getTitle());
            nodeMap.put("type", node.getType());
            nodeMap.put("difficulty", node.getDifficulty());
            nodeList.add(nodeMap);
            
            if (node.getPrerequisites() != null) {
                for (String prereq : node.getPrerequisites()) {
                    Map<String, Object> edge = new HashMap<>();
                    edge.put("source", prereq);
                    edge.put("target", node.getId());
                    edge.put("type", "prerequisite");
                    edgeList.add(edge);
                }
            }
            
            if (node.getRelated() != null) {
                for (String related : node.getRelated()) {
                    Map<String, Object> edge = new HashMap<>();
                    edge.put("source", node.getId());
                    edge.put("target", related);
                    edge.put("type", "related");
                    edgeList.add(edge);
                }
            }
        }
        
        graph.put("nodes", nodeList);
        graph.put("edges", edgeList);
        
        return graph;
    }

    @Override
    public double calculateRelevance(String query, KnowledgeNode node) {
        if (query == null || node == null) return 0;
        
        String lowerQuery = query.toLowerCase();
        double score = 0;
        
        if (node.getTitle() != null && node.getTitle().toLowerCase().contains(lowerQuery)) {
            score += 0.5;
        }
        
        if (node.getContent() != null && node.getContent().toLowerCase().contains(lowerQuery)) {
            score += 0.3;
        }
        
        if (node.getTags() != null) {
            for (String tag : node.getTags()) {
                if (tag.toLowerCase().contains(lowerQuery)) {
                    score += 0.2;
                    break;
                }
            }
        }
        
        return Math.min(1.0, score);
    }

    @Override
    public void indexKnowledge() {
        knowledgeCache.clear();
        invertedIndex.clear();
        
        List<com.example.agentframework.entity.KnowledgeNode> entities = mapper.findAll();
        for (com.example.agentframework.entity.KnowledgeNode entity : entities) {
            KnowledgeNode node = convertToNode(entity);
            knowledgeCache.put(node.getId(), node);
            updateIndex(node);
        }
    }

    private void updateIndex(KnowledgeNode node) {
        String[] words = (node.getTitle() + " " + node.getContent()).toLowerCase().split("\\s+");
        for (String word : words) {
            if (word.length() > 2) {
                invertedIndex.computeIfAbsent(word, k -> new ArrayList<>())
                        .add(node.getId());
            }
        }
    }

    private void removeFromIndex(String id) {
        invertedIndex.values().forEach(list -> list.remove(id));
    }

    private boolean matchesQuery(KnowledgeNode node, String query) {
        if (query == null || query.isEmpty()) return true;
        
        String lowerQuery = query.toLowerCase();
        return (node.getTitle() != null && node.getTitle().toLowerCase().contains(lowerQuery)) ||
               (node.getContent() != null && node.getContent().toLowerCase().contains(lowerQuery)) ||
               (node.getTags() != null && node.getTags().stream()
                       .anyMatch(tag -> tag.toLowerCase().contains(lowerQuery)));
    }

    private com.example.agentframework.entity.KnowledgeNode convertToEntity(KnowledgeNode node) {
        com.example.agentframework.entity.KnowledgeNode entity = new com.example.agentframework.entity.KnowledgeNode();
        if (node.getId() != null && !node.getId().isEmpty()) {
            try {
                entity.setId(Long.parseLong(node.getId()));
            } catch (NumberFormatException e) {
            }
        }
        entity.setNodeId(node.getId());
        entity.setName(node.getTitle());
        entity.setCourseId(node.getCourseId());
        entity.setDescription(node.getContent());
        entity.setDifficulty(node.getDifficulty() > 0 ? node.getDifficulty() : 1);
        return entity;
    }

    private KnowledgeNode convertToNode(com.example.agentframework.entity.KnowledgeNode entity) {
        KnowledgeNode node = new KnowledgeNode();
        node.setId(entity.getNodeId() != null ? entity.getNodeId() : String.valueOf(entity.getId()));
        node.setTitle(entity.getName());
        node.setContent(entity.getDescription());
        node.setCourseId(entity.getCourseId());
        node.setDifficulty(entity.getDifficulty() != null ? entity.getDifficulty() : 1);
        return node;
    }
}
