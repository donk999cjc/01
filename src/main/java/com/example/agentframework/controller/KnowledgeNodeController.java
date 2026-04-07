package com.example.agentframework.controller;

import com.example.agentframework.entity.KnowledgeNode;
import com.example.agentframework.service.KnowledgeNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@CrossOrigin
public class KnowledgeNodeController {

    @Autowired
    private KnowledgeNodeService knowledgeNodeService;

    @PostMapping
    public ResponseEntity<KnowledgeNode> createKnowledgeNode(@RequestBody KnowledgeNode node) {
        KnowledgeNode createdNode = knowledgeNodeService.createKnowledgeNode(node);
        return new ResponseEntity<>(createdNode, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KnowledgeNode> getKnowledgeNodeById(@PathVariable Long id) {
        return knowledgeNodeService.getKnowledgeNodeById(id)
                .map(node -> new ResponseEntity<>(node, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/nodeId/{nodeId}")
    public ResponseEntity<KnowledgeNode> getKnowledgeNodeByNodeId(@PathVariable String nodeId) {
        KnowledgeNode node = knowledgeNodeService.getKnowledgeNodeByNodeId(nodeId);
        if (node != null) {
            return new ResponseEntity<>(node, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<KnowledgeNode>> getKnowledgeNodesByCourseId(@PathVariable String courseId) {
        List<KnowledgeNode> nodes = knowledgeNodeService.getKnowledgeNodesByCourseId(courseId);
        return new ResponseEntity<>(nodes, HttpStatus.OK);
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<KnowledgeNode>> getKnowledgeNodesByParentId(@PathVariable String parentId) {
        List<KnowledgeNode> nodes = knowledgeNodeService.getKnowledgeNodesByParentId(parentId);
        return new ResponseEntity<>(nodes, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<KnowledgeNode>> getAllKnowledgeNodes() {
        List<KnowledgeNode> nodes = knowledgeNodeService.getAllKnowledgeNodes();
        return new ResponseEntity<>(nodes, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<KnowledgeNode> updateKnowledgeNode(@PathVariable Long id, @RequestBody KnowledgeNode node) {
        KnowledgeNode updatedNode = knowledgeNodeService.updateKnowledgeNode(id, node);
        if (updatedNode != null) {
            return new ResponseEntity<>(updatedNode, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKnowledgeNode(@PathVariable Long id) {
        knowledgeNodeService.deleteKnowledgeNode(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
