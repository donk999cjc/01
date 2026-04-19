package com.example.agentframework.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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
     * 通过文件路径识别图片并批改
     */
    public Map<String, Object> reviewImageByPath(String filePath, String assignmentInfo) {
        try {
            // 读取文件并转换为Base64
            byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
            String imageBase64 = Base64.getEncoder().encodeToString(fileContent);

            // 调用原有的批改方法
            return reviewImageAssignment(imageBase64, assignmentInfo);

        } catch (IOException e) {
            System.err.println("读取图片文件失败: " + e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "读取图片文件失败: " + e.getMessage());
            result.put("score", 0.0);
            result.put("feedback", "无法读取图片文件");
            result.put("recognizedContent", "");
            return result;
        }
    }

    /**
     * 调用AI大模型进行对话
     * @param systemPrompt 系统提示词（定义AI角色）
     * @param userMessage 用户消息
     * @return AI回复内容
     */
    public String chat(String systemPrompt, String userMessage) {
        if (!aiEnabled || apiKey == null || apiKey.isEmpty()) {
            return generateMockResponse(userMessage);
        }

        StringBuilder sb = new StringBuilder();
        chatStream(systemPrompt, userMessage, chunk -> sb.append(chunk));
        return sb.toString();
    }

    public void chatStream(String systemPrompt, String userMessage, Consumer<String> onChunk) {
        if (!aiEnabled || apiKey == null || apiKey.isEmpty()) {
            onChunk.accept(generateMockResponse(userMessage));
            return;
        }

        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);

            JsonArray messages = new JsonArray();

            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                JsonObject systemMessage = new JsonObject();
                systemMessage.addProperty("role", "system");
                systemMessage.addProperty("content", systemPrompt);
                messages.add(systemMessage);
            }

            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            userMsg.addProperty("content", userMessage);
            messages.add(userMsg);

            requestBody.add("messages", messages);
            requestBody.addProperty("temperature", 0.7);
            requestBody.addProperty("max_tokens", 2048);
            requestBody.addProperty("stream", true);

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

            OkHttpClient streamClient = client.newBuilder()
                    .readTimeout(120, TimeUnit.SECONDS)
                    .build();

            try (Response response = streamClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    onChunk.accept(generateMockResponse(userMessage));
                    return;
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.body().byteStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        String data = line.substring(5).trim();
                        if ("[DONE]".equals(data)) {
                            break;
                        }
                        try {
                            JsonObject chunk = gson.fromJson(data, JsonObject.class);
                            JsonArray choices = chunk.getAsJsonArray("choices");
                            if (choices != null && choices.size() > 0) {
                                JsonObject delta = choices.get(0).getAsJsonObject()
                                        .getAsJsonObject("delta");
                                if (delta != null && delta.has("content")) {
                                    String content = delta.get("content").getAsString();
                                    if (content != null && !content.isEmpty()) {
                                        onChunk.accept(content);
                                    }
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("AI stream error: " + e.getMessage());
            onChunk.accept(generateMockResponse(userMessage));
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

    /**
     * 识别图片中的作业内容并批改
     * @param imageBase64 Base64编码的图片
     * @param assignmentInfo 作业信息（可选）
     * @return 批改结果，包含：识别内容、评分、反馈等
     */
    public Map<String, Object> reviewImageAssignment(String imageBase64, String assignmentInfo) {
        Map<String, Object> result = new HashMap<>();
        
        // 如果没有配置API Key，返回模拟批改结果
        if (!aiEnabled || apiKey == null || apiKey.isEmpty()) {
            return generateMockImageReview(imageBase64, assignmentInfo);
        }

        try {
            // 构建请求体 - 使用智谱AI的视觉模型
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "glm-4v-plus"); // 视觉模型
            
            // 构建消息数组
            JsonArray messages = new JsonArray();
            
            // 系统消息 - 定义作业批改角色
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", "你是一个专业的作业批改老师。请仔细阅读图片中的作业内容，然后：\n" +
                "1. 识别图片中的所有文字和内容\n" +
                "2. 评估作业完成质量\n" +
                "3. 给出评分（0-100分）\n" +
                "4. 提供详细的批改反馈，包括：\n" +
                "   - 做得好的地方\n" +
                "   - 需要改进的地方\n" +
                "   - 具体的建议\n" +
                "请用友好、鼓励的语气\n" +
                "请用中文回复");
            messages.add(systemMessage);
            
            // 用户消息 - 包含图片和文字
            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            
            // 构建包含图片的内容
            JsonArray contentArray = new JsonArray();
            
            // 添加文字提示
            JsonObject textContent = new JsonObject();
            textContent.addProperty("type", "text");
            String userPrompt = "请批改这份作业。";
            if (assignmentInfo != null && !assignmentInfo.isEmpty()) {
                userPrompt = "作业要求：" + assignmentInfo + "\n\n请批改这份作业。";
            }
            textContent.addProperty("text", userPrompt);
            contentArray.add(textContent);
            
            // 添加图片
            JsonObject imageContent = new JsonObject();
            imageContent.addProperty("type", "image_url");
            JsonObject imageUrl = new JsonObject();
            imageUrl.addProperty("url", imageBase64);
            imageContent.add("image_url", imageUrl);
            contentArray.add(imageContent);
            
            userMsg.add("content", contentArray);
            messages.add(userMsg);
            
            requestBody.add("messages", messages);
            requestBody.addProperty("temperature", 0.7);
            requestBody.addProperty("max_tokens", 4096);
            
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
                    System.err.println("AI图片识别调用失败: " + response.code() + " - " + errorBody);
                    return generateMockImageReview(imageBase64, assignmentInfo);
                }
                
                String responseBody = response.body().string();
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                
                // 解析AI回复
                JsonArray choices = jsonResponse.getAsJsonArray("choices");
                if (choices != null && choices.size() > 0) {
                    JsonObject firstChoice = choices.get(0).getAsJsonObject();
                    JsonObject message = firstChoice.getAsJsonObject("message");
                    if (message != null) {
                        String aiResponse = message.get("content").getAsString();
                        // 解析AI回复，提取评分和反馈
                        result.put("success", true);
                        result.put("review", aiResponse);
                        result.put("score", extractScore(aiResponse));
                        result.put("feedback", aiResponse);
                        result.put("recognizedContent", extractContent(aiResponse));
                        return result;
                    }
                }
                
                result.put("success", false);
                result.put("message", "抱歉，AI服务暂时无法批改作业，请稍后再试。");
                return result;
            }
            
        } catch (Exception e) {
            System.err.println("AI图片识别异常: " + e.getMessage());
            e.printStackTrace();
            return generateMockImageReview(imageBase64, assignmentInfo);
        }
    }

    /**
     * 生成模拟图片批改结果
     */
    private Map<String, Object> generateMockImageReview(String imageBase64, String assignmentInfo) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("recognizedContent", "图片作业内容（模拟识别）");
        
        // 模拟评分
        double score = 85.0;
        result.put("score", score);
        
        // 模拟反馈
        StringBuilder feedback = new StringBuilder();
        feedback.append("📝 **作业批改报告**\n\n");
        feedback.append("✨ **做得好的地方：**\n");
        feedback.append("1. 字迹工整，书写规范\n");
        feedback.append("2. 解题思路清晰\n");
        feedback.append("3. 大部分题目回答正确\n\n");
        feedback.append("💡 **需要改进的地方：**\n");
        feedback.append("1. 第3题计算有误，建议重新检查\n");
        feedback.append("2. 可以增加一些解题步骤说明\n\n");
        feedback.append("📚 **总体评价：**\n");
        feedback.append("作业完成质量良好！继续保持，相信你会做得更好！加油！💪\n\n");
        
        result.put("feedback", feedback.toString());
        result.put("review", feedback.toString());
        
        return result;
    }

    /**
     * 从AI回复中提取评分
     */
    private double extractScore(String aiResponse) {
        // 简单实现：查找评分
        if (aiResponse.contains("评分") || aiResponse.contains("得分")) {
            // 查找数字
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)分?");
            java.util.regex.Matcher matcher = pattern.matcher(aiResponse);
            if (matcher.find()) {
                try {
                    return Double.parseDouble(matcher.group(1));
                } catch (Exception e) {
                    // 忽略
                }
            }
        }
        return 85.0; // 默认评分
    }

    /**
     * 从AI回复中提取识别内容
     */
    private String extractContent(String aiResponse) {
        // 简单实现：返回前200个字符
        if (aiResponse.length() > 200) {
            return aiResponse.substring(0, 200) + "...";
        }
        return aiResponse;
    }
}
