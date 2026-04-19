package com.example.agentframework.service;

import com.example.agentframework.entity.KnowledgeNode;
import com.example.agentframework.mapper.KnowledgeNodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KnowledgeNodeService {

    @Autowired
    private KnowledgeNodeMapper knowledgeNodeMapper;

    public KnowledgeNode createKnowledgeNode(KnowledgeNode node) {
        knowledgeNodeMapper.insert(node);
        return node;
    }

    public Optional<KnowledgeNode> getKnowledgeNodeById(Long id) {
        KnowledgeNode node = knowledgeNodeMapper.findById(id);
        return Optional.ofNullable(node);
    }

    public KnowledgeNode getKnowledgeNodeByNodeId(String nodeId) {
        return knowledgeNodeMapper.findByNodeId(nodeId);
    }

    public List<KnowledgeNode> getKnowledgeNodesByCourseId(String courseId) {
        return knowledgeNodeMapper.findByCourseId(courseId);
    }

    public List<KnowledgeNode> getKnowledgeNodesByParentId(String parentId) {
        return knowledgeNodeMapper.findByParentId(parentId);
    }

    public List<KnowledgeNode> getAllKnowledgeNodes() {
        return knowledgeNodeMapper.findAll();
    }

    public KnowledgeNode updateKnowledgeNode(Long id, KnowledgeNode node) {
        KnowledgeNode existingNode = knowledgeNodeMapper.findById(id);
        if (existingNode != null) {
            existingNode.setName(node.getName());
            existingNode.setCourseId(node.getCourseId());
            existingNode.setParentId(node.getParentId());
            existingNode.setDescription(node.getDescription());
            existingNode.setDifficulty(node.getDifficulty());
            knowledgeNodeMapper.update(existingNode);
            return existingNode;
        }
        return null;
    }

    public void deleteKnowledgeNode(Long id) {
        knowledgeNodeMapper.deleteById(id);
    }
}
