package com.example.agentframework.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class AIService {

    // 使用智谱AI GLM-4 API (推荐，国内访问稳定)
    private static final String ZHIPU_API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    
    // 或者使用OpenAI API (需要翻墙)
    // private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${ai.api.key:}")
    private String apiKey;

    @Value("${ai.api.model:glm-4-flash}")
    private String model;

    @Value("${ai.api.enabled:false}")
    private boolean aiEnabled;

    private final OkHttpClient client;
    private final Gson gson;

    public AIService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    /**
     * 调用AI大模型进行对话
     * @param systemPrompt 系统提示词（定义AI角色）
     * @param userMessage 用户消息
     * @return AI回复内容
     */
    public String chat(String systemPrompt, String userMessage) {
        // 如果没有配置API Key，返回模拟回复
        if (!aiEnabled || apiKey == null || apiKey.isEmpty()) {
            return generateMockResponse(userMessage);
        }

        try {
            // 构建请求体
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);
            
            // 构建消息数组
            JsonArray messages = new JsonArray();
            
            // 系统消息（定义AI角色）
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                JsonObject systemMessage = new JsonObject();
                systemMessage.addProperty("role", "system");
                systemMessage.addProperty("content", systemPrompt);
                messages.add(systemMessage);
            }
            
            // 用户消息
            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            userMsg.addProperty("content", userMessage);
            messages.add(userMsg);
            
            requestBody.add("messages", messages);
            requestBody.addProperty("temperature", 0.7);
            requestBody.addProperty("max_tokens", 2048);
            
            // 构建HTTP请求
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), 
                    requestBody.toString()
            );
            
            Request request = new Request.Builder()
                    .url(ZHIPU_API_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            // 发送请求
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    System.err.println("AI API调用失败: " + response.code() + " - " + errorBody);
                    return generateMockResponse(userMessage);
                }
                
                String responseBody = response.body().string();
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                
                // 解析AI回复
                JsonArray choices = jsonResponse.getAsJsonArray("choices");
                if (choices != null && choices.size() > 0) {
                    JsonObject firstChoice = choices.get(0).getAsJsonObject();
                    JsonObject message = firstChoice.getAsJsonObject("message");
                    if (message != null) {
                        return message.get("content").getAsString();
                    }
                }
                
                return "抱歉，AI服务暂时无法回复，请稍后再试。";
            }
            
        } catch (IOException e) {
            System.err.println("AI API调用异常: " + e.getMessage());
            return generateMockResponse(userMessage);
        }
    }

    /**
     * 生成模拟回复（当AI API不可用时使用）
     */
    private String generateMockResponse(String message) {
        String lowerMessage = message.toLowerCase().trim();
        
        if (lowerMessage.contains("你好") || lowerMessage.contains("hello") || lowerMessage.contains("hi")) {
            return "你好！我是你的AI学习助手，很高兴为你服务。有什么我可以帮助你的吗？";
        } else if (lowerMessage.contains("课程") || lowerMessage.contains("学习")) {
            return "关于课程学习，我可以帮助你：\n1. 解答专业问题\n2. 提供学习建议\n3. 推荐学习资源\n4. 制定学习计划\n\n你想了解哪方面的内容？";
        } else if (lowerMessage.contains("作业") || lowerMessage.contains("题目") || lowerMessage.contains("题")) {
            return "我很乐意帮助你完成作业！请告诉我：\n1. 作业的具体内容\n2. 你遇到的困难\n3. 需要什么样的帮助\n\n我会尽力为你提供详细的解答和指导。";
        } else if (lowerMessage.contains("帮助") || lowerMessage.contains("help")) {
            return "我可以为你提供以下帮助：\n\n📚 **学习辅导**\n- 解答课程相关问题\n- 解释复杂概念\n- 提供学习建议\n\n📝 **作业辅助**\n- 分析题目要求\n- 提供解题思路\n- 检查作业答案\n\n🔍 **知识拓展**\n- 推荐学习资源\n- 解释前沿技术\n- 提供案例分析\n\n请告诉我你需要什么帮助！";
        } else if (lowerMessage.contains("谢谢") || lowerMessage.contains("感谢")) {
            return "不客气！很高兴能帮到你。如果还有其他问题，随时可以问我。祝你学习进步！📚";
        } else if (lowerMessage.contains("再见") || lowerMessage.contains("拜拜")) {
            return "再见！祝你学习愉快，期待下次为你服务！👋";
        } else {
            return "我理解你的问题是关于：" + message + "\n\n这是一个很好的问题！作为你的AI学习助手，我建议你：\n\n1. **明确问题核心** - 试着将问题分解为更小的部分\n2. **提供上下文** - 如果有相关背景信息，请告诉我\n3. **具体说明需求** - 你希望得到什么样的帮助？\n\n如果你能提供更多细节，我可以给你更准确的回答。😊";
        }
    }

    /**
     * 检查AI服务是否可用
     */
    public boolean isAIEnabled() {
        return aiEnabled && apiKey != null && !apiKey.isEmpty();
    }
}
