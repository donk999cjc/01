package com.example.agentframework.controller;

import com.example.agentframework.rag.RAGService;
import com.example.agentframework.knowledge.KnowledgeMiddleware;
import com.example.agentframework.knowledge.KnowledgeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
@CrossOrigin
public class RAGController {

    @Autowired
    private RAGService ragService;

    @Autowired
    private KnowledgeMiddleware knowledgeMiddleware;

    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> queryWithRAG(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        String courseId = request.get("courseId");

        if (query == null || query.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "查询内容不能为空");
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> result = ragService.queryWithRAGDetailed(query, courseId);
        result.put("success", true);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> ragChat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String courseId = request.get("courseId");

        if (message == null || message.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "消息不能为空");
            return ResponseEntity.badRequest().body(error);
        }

        String response = ragService.queryWithRAG(message, courseId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("response", response);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/index/{courseId}")
    public ResponseEntity<Map<String, Object>> indexCourse(@PathVariable String courseId) {
        ragService.indexKnowledgeBase(courseId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "知识库索引构建完成，课程: " + courseId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/index/node/{knowledgeId}")
    public ResponseEntity<Map<String, Object>> indexNode(@PathVariable String knowledgeId) {
        KnowledgeNode node = knowledgeMiddleware.getKnowledge(knowledgeId);
        if (node == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "知识节点不存在");
            return ResponseEntity.badRequest().body(error);
        }

        ragService.indexKnowledgeNode(node);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "知识节点索引完成");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/retrieve")
    public ResponseEntity<Map<String, Object>> retrieve(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        String courseId = request.get("courseId");

        if (query == null || query.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "查询内容不能为空");
            return ResponseEntity.badRequest().body(error);
        }

        List<Map<String, Object>> results = ragService.retrieve(query, courseId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("results", results);
        result.put("count", results.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = ragService.getStatus();
        status.put("success", true);
        return ResponseEntity.ok(status);
    }
}
