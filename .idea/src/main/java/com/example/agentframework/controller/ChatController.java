package com.example.agentframework.controller;

import com.example.agentframework.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class ChatController {

    @Autowired
    private AIService aiService;

    /**
     * AI聊天接口 - 供学生端使用
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> chatRequest) {
        String message = chatRequest.get("message");
        String systemPrompt = chatRequest.get("systemPrompt");

        Map<String, Object> result = new HashMap<>();

        if (message == null || message.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "消息不能为空");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            if (systemPrompt == null || systemPrompt.isEmpty()) {
                systemPrompt = "你是一个专业的AI学习助手，专门帮助学生解答学习相关的问题。请用友好、鼓励的语气回答，用中文回复。";
            }

            String response = aiService.chat(systemPrompt, message);

            result.put("success", true);
            result.put("message", "OK");
            result.put("response", response);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "AI服务暂时不可用：" + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 检查AI服务状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("enabled", aiService.isAIEnabled());
        result.put("message", aiService.isAIEnabled() ? "AI服务可用" : "AI服务未配置或已禁用");
        return ResponseEntity.ok(result);
    }
}
