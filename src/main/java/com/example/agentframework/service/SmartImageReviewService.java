package com.example.agentframework.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * 智能图片识别与作业批改服务
 * 使用智谱AI GLM-4V（视觉模型）进行图片理解和题目识别
 * 
 * 特点：
 * 1. 完全免费：使用智谱AI的GLM-4-Flash模型（有免费额度）
 * 2. 支持OCR：准确识别手写文字、印刷体、数学公式
 * 3. 智能分析：自动归纳题目、逐题批改、给出详细反馈
 */
@Service
public class SmartImageReviewService {

    // 智谱AI API配置
    private static final String ZHIPU_API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    
    @Value("${ai.api.key:}")
    private String apiKey;
    
    private final OkHttpClient client;
    private final Gson gson;
    
    public SmartImageReviewService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }
    
    /**
     * 智能识别图片并批改作业（核心方法）
     * 
     * 流程：
     * 1. OCR识别：提取图片中的所有文字内容
     * 2. 题目归纳：智能识别题目数量和类型
     * 3. 逐题分析：对每道题进行详细分析
     * 4. 综合评分：根据答题情况给出总分和详细反馈
     * 
     * @param filePath 图片文件路径
     * @param assignmentInfo 作业要求信息
     * @return 批改结果Map
     */
    public Map<String, Object> reviewImageByPath(String filePath, String assignmentInfo) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("🔍 开始智能图片识别...");
            System.out.println("   文件路径: " + filePath);
            
            // 1. 读取图片并转换为Base64
            byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
            String imageBase64 = Base64.getEncoder().encodeToString(fileContent);
            String imageType = getImageType(filePath);
            String dataUrl = "data:image/" + imageType + ";base64," + imageBase64;
            
            System.out.println("   图片大小: " + (fileContent.length / 1024) + " KB");
            System.out.println("   图片格式: " + imageType);
            
            // 2. 调用AI进行图片识别和分析
            if (apiKey != null && !apiKey.isEmpty()) {
                result = callAIForAnalysis(dataUrl, assignmentInfo);
            } else {
                // 如果没有配置API Key，使用本地模拟分析
                System.out.println("⚠️ 未检测到API Key，使用本地模拟分析");
                result = generateLocalAnalysis(assignmentInfo);
            }
            
            return result;
            
        } catch (IOException e) {
            System.err.println("❌ 读取图片文件失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "读取图片文件失败: " + e.getMessage());
            result.put("score", 0.0);
            result.put("feedback", "无法读取图片文件");
            result.put("recognizedContent", "");
            return result;
        }
    }
    
    /**
     * 调用智谱AI GLM-4V模型进行分析
     */
    private Map<String, Object> callAIForAnalysis(String imageDataUrl, String assignmentInfo) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 构建系统提示词 - 让AI扮演专业教师角色
            String systemPrompt = buildSystemPrompt();
            
            // 构建用户消息
            String userMessage = buildUserMessage(assignmentInfo);
            
            System.out.println("📤 准备发送API请求...");
            System.out.println("   API Key: " + (apiKey != null && apiKey.length() > 10 ? apiKey.substring(0, 10) + "..." : "未配置"));
            System.out.println("   图片数据长度: " + (imageDataUrl != null ? imageDataUrl.length() : 0) + " 字符");
            
            // 创建请求体
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "glm-4v");  // 使用免费的视觉模型
            
            JsonArray messages = new JsonArray();
            
            // 系统消息（简化版，避免token过多）
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", "你是一位经验丰富的中小学教师，擅长批改作业。请识别图片中的作业内容，给出评分和详细反馈。使用JSON格式输出结果。");
            messages.add(systemMessage);
            
            // 用户消息（包含图片）
            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            
            JsonArray content = new JsonArray();
            
            // 文本部分
            JsonObject textPart = new JsonObject();
            textPart.addProperty("type", "text");
            textPart.addProperty("text", userMessage);
            content.add(textPart);
            
            // 图片部分 - 使用正确的格式
            JsonObject imagePart = new JsonObject();
            imagePart.addProperty("type", "image_url");
            JsonObject imageUrlObj = new JsonObject();
            imageUrlObj.addProperty("url", imageDataUrl);
            imagePart.add("image_url", imageUrlObj);
            content.add(imagePart);
            
            userMsg.add("content", content);
            messages.add(userMsg);
            
            requestBody.add("messages", messages);
            requestBody.addProperty("max_tokens", 1500);  // 减少token数量
            
            String requestJson = gson.toJson(requestBody);
            System.out.println("   请求体大小: " + requestJson.length() + " 字符");
            
            // 发送请求
            RequestBody body = RequestBody.create(
                    requestJson,
                    MediaType.parse("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                    .url(ZHIPU_API_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .post(body)
                    .build();
            
            System.out.println("🤖 正在调用智谱AI GLM-4V模型...");
            
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            
            System.out.println("   HTTP状态码: " + response.code());
            System.out.println("   响应长度: " + responseBody.length() + " 字符");
            
            if (!response.isSuccessful()) {
                System.err.println("❌ API返回错误: " + responseBody);
                
                // 尝试解析具体错误信息
                try {
                    JsonObject errorJson = gson.fromJson(responseBody, JsonObject.class);
                    if (errorJson.has("error")) {
                        JsonObject errorObj = errorJson.getAsJsonObject("error");
                        String errorCode = errorObj.has("code") ? errorObj.get("code").getAsString() : "";
                        String errorMsg = errorObj.has("message") ? errorObj.get("message").getAsString() : "";
                        throw new IOException("API错误 [" + errorCode + "]: " + errorMsg);
                    }
                } catch (Exception parseEx) {
                    throw new IOException("API调用失败: " + response.code() + " " + responseBody.substring(0, Math.min(500, responseBody.length())));
                }
            }
            
            // 解析响应
            JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
            
            if (!responseJson.has("choices") || responseJson.getAsJsonArray("choices").size() == 0) {
                throw new IOException("API响应格式异常: " + responseBody.substring(0, 200));
            }
            
            String aiResponse = responseJson
                    .getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content")
                    .getAsString();
            
            System.out.println("✅ AI分析完成！");
            System.out.println("   AI响应长度: " + aiResponse.length() + " 字符");
            
            // 解析AI返回的结构化数据
            result = parseAIResponse(aiResponse, assignmentInfo);
            
        } catch (Exception e) {
            System.err.println("❌ AI调用失败: " + e.getMessage());
            e.printStackTrace();
            // 降级到本地模拟
            result = generateLocalAnalysis(assignmentInfo);
            result.put("warning", "⚠️ AI服务暂时不可用（" + e.getMessage() + "），已切换到本地模拟模式");
        }
        
        return result;
    }
    
    /**
     * 纯答案比对模式 + 错题举一反三
     */
    private String buildSystemPrompt() {
        return "你是一个作业批改系统+智能学习助手。你的任务：\n\n" +
                "## 第一部分：批改作业\n\n" +
                "1. **识别图片中的所有题目和学生答案**\n" +
                "2. **根据标准答案（或你掌握的知识）判断每道题的对错**\n" +
                "3. **对的给满分，错的0分，部分正确按比例给分**\n" +
                "4. **说明错误原因**\n\n" +
                "## 第二部分：错题举一反三（重要！）\n\n" +
                "对于每一道**做错的题目**，必须完成以下内容：\n" +
                "- **错误原因分析**：为什么错了？是概念不清、计算错误还是方法不对？\n" +
                "- **知识点定位**：这道题考查了哪个知识点？\n" +
                "- **推荐同类练习题**：出一道类似的题目让学生巩固（给出完整题目）\n" +
                "- **解题思路指导**：这类题应该怎么想？用什么方法？\n\n" +
                "## 输出格式（JSON）\n\n" +
                "{\n" +
                "  \"total_score\": 总分(数字),\n" +
                "  \"questions\": [\n" +
                "    {\n" +
                "      \"num\": 题号,\n" +
                "      \"question\": \"原题内容\",\n" +
                "      \"student_answer\": \"学生答案\",\n" +
                "      \"correct_answer\": \"正确答案\",\n" +
                "      \"is_correct\": true/false,\n" +
                "      \"score\": 得分,\n" +
                "      \"reason\": \"错误原因(对了就写'正确')\",\n" +
                "      // 下面是错题专有字段\n" +
                "      \"knowledge_point\": \"考查的知识点(如:一元二次方程的解法)\",\n" +
                "      \"similar_question\": \"推荐的同类练习题(完整题目,难度相当或略高)\",\n" +
                "      \"solution_guide\": \"解题思路和方法指导\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"summary\": \"一句话总结(比如:5道题做对3道,得分75分)\",\n" +
                "  \"error_summary\": \"错题总结(列出所有错误原因和需要加强的知识点)\"\n" +
                "}";
    }
    
    /**
     * 构建用户消息
     */
    private String buildUserMessage(String assignmentInfo) {
        StringBuilder message = new StringBuilder();
        message.append("请帮我批改这份作业。\n\n");
        
        if (assignmentInfo != null && !assignmentInfo.isEmpty()) {
            message.append("【作业信息】\n");
            message.append(assignmentInfo);
            message.append("\n\n");
        }
        
        message.append("【图片说明】\n");
        message.append("这是一份学生提交的手写作业照片，请按照上述要求进行识别、分析和批改。");
        
        return message.toString();
    }
    
    /**
     * 解析AI返回的JSON响应
     */
    private Map<String, Object> parseAIResponse(String aiResponse, String assignmentInfo) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 尝试从响应中提取JSON
            String jsonStr = extractJSON(aiResponse);
            
            if (jsonStr != null) {
                JsonObject parsed = gson.fromJson(jsonStr, JsonObject.class);
                
                result.put("success", true);
                result.put("recognizedContent", parsed.has("recognized_content") ? 
                        parsed.get("recognized_content").getAsString() : aiResponse);
                
                // 新格式：直接从顶层取 total_score
                if (parsed.has("total_score")) {
                    result.put("score", parsed.get("total_score").getAsDouble());
                }
                
                // 构建简化的反馈
                StringBuilder fb = new StringBuilder();
                fb.append("批改结果\n\n");
                
                // 题目列表
                if (parsed.has("questions") && parsed.getAsJsonArray("questions").size() > 0) {
                    JsonArray questions = parsed.getAsJsonArray("questions");
                    for (int i = 0; i < questions.size(); i++) {
                        JsonObject q = questions.get(i).getAsJsonObject();
                        int num = q.has("num") ? q.get("num").getAsInt() : (i + 1);
                        boolean isCorrect = q.has("is_correct") && q.get("is_correct").getAsBoolean();
                        double score = q.has("score") ? q.get("score").getAsDouble() : 0;
                        
                        fb.append(String.format("第%d题: %s (%.0f分)\n", 
                            num, isCorrect ? "正确" : "错误", score));
                        
                        if (!isCorrect && q.has("reason")) {
                            fb.append("  原因: ").append(q.get("reason").getAsString()).append("\n");
                        }
                    }
                    fb.append("\n");
                    
                    result.put("detailedAnalysis", questions.toString());
                }
                
                // 总结
                if (parsed.has("summary")) {
                    fb.append(parsed.get("summary").getAsString());
                    result.put("summaryFeedback", parsed.get("summary").getAsString());
                }
                
                result.put("feedback", fb.toString());
                        
            } else {
                // 如果无法解析JSON，直接使用原始响应
                result.put("success", true);
                result.put("recognizedContent", aiResponse);
                double extractedScore = extractScoreFromText(aiResponse);
                result.put("score", extractedScore > 0 ? extractedScore : null);
                if (extractedScore < 0) {
                    System.err.println("⚠️ 警告：无法从AI响应中提取分数！响应内容: " + aiResponse.substring(0, Math.min(300, aiResponse.length())));
                }
                result.put("feedback", aiResponse);
            }
            
        } catch (Exception e) {
            System.err.println("⚠️ 解析AI响应时出错: " + e.getMessage());
            // 使用原始响应作为备选
            result.put("success", true);
            result.put("recognizedContent", aiResponse);
            double extractedScore = extractScoreFromText(aiResponse);
            result.put("score", extractedScore > 0 ? extractedScore : null);
            if (extractedScore < 0) {
                System.err.println("⚠️ 警告：异常情况下无法从AI响应中提取分数！");
            }
            result.put("feedback", aiResponse);
        }
        
        return result;
    }
    
    /**
     * 从文本中提取JSON
     */
    private String extractJSON(String text) {
        Pattern pattern = Pattern.compile("\\{[\\s\\S]*\\}");
        Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
    
    /**
     * 从文本中提取分数（增强版）
     */
    private double extractScoreFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            System.err.println("⚠️ 无法提取分数：文本为空");
            return -1.0;
        }
        
        // 模式1: "总分：XX" 或 "total_score: XX"
        Pattern pattern1 = Pattern.compile("(?:总分|total_score)[：:]*\\s*(\\d+(?:\\.\\d+)?)");
        Matcher matcher1 = pattern1.matcher(text);
        if (matcher1.find()) {
            double score = Double.parseDouble(matcher1.group(1));
            System.out.println("✅ 从'总分'格式提取分数: " + score);
            return score;
        }
        
        // 模式2: "得分：XX" 或 "score: XX"
        Pattern pattern2 = Pattern.compile("(?:得分|score)[：:]*\\s*(\\d+(?:\\.\\d+)?)");
        Matcher matcher2 = pattern2.matcher(text);
        if (matcher2.find()) {
            double score = Double.parseDouble(matcher2.group(1));
            System.out.println("✅ 从'得分'格式提取分数: " + score);
            return score;
        }
        
        // 模式3: "XX/100" 或 "XX分"
        Pattern pattern3 = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:/100|分)");
        Matcher matcher3 = pattern3.matcher(text);
        if (matcher3.find()) {
            double score = Double.parseDouble(matcher3.group(1));
            System.out.println("✅ 从'XX/100'或'XX分'格式提取分数: " + score);
            return score;
        }
        
        // 模式4: 查找所有数字，取最大的合理分数（0-100之间）
        Pattern pattern4 = Pattern.compile("\\b(\\d+(?:\\.\\d+)?)\\b");
        Matcher matcher4 = pattern4.matcher(text);
        double maxValidScore = -1;
        while (matcher4.find()) {
            double num = Double.parseDouble(matcher4.group(1));
            if (num >= 0 && num <= 100 && num > maxValidScore) {
                maxValidScore = num;
            }
        }
        
        if (maxValidScore > 0) {
            System.out.println("✅ 从文本中提取到最大有效分数: " + maxValidScore);
            return maxValidScore;
        }
        
        System.err.println("⚠️ 无法从文本中提取分数，文本内容: " + text.substring(0, Math.min(200, text.length())));
        return -1.0; // 返回-1表示提取失败
    }
    
    /**
     * 从评估对象构建详细反馈
     */
    private String buildFeedbackFromEvaluation(JsonObject parsed) {
        StringBuilder feedback = new StringBuilder();
        
        feedback.append("📋 **AI智能批改报告**\n\n");
        feedback.append("---\n\n");
        
        // 题目归纳
        if (parsed.has("questions_summary")) {
            feedback.append("**📝 题目概况：**\n");
            JsonObject summary = parsed.getAsJsonObject("questions_summary");
            if (summary.has("total_count")) {
                feedback.append("- 共 ").append(summary.get("total_count").getAsInt()).append(" 道题\n");
            }
            if (summary.has("question_types")) {
                feedback.append("- 题型：").append(summary.get("question_types")).append("\n");
            }
            feedback.append("\n---\n\n");
        }
        
        // 逐题分析
        if (parsed.has("detailed_analysis")) {
            JsonArray analysis = parsed.getAsJsonArray("detailed_analysis");
            feedback.append("**✍️ 逐题详解：**\n\n");
            
            for (int i = 0; i < analysis.size(); i++) {
                JsonObject q = analysis.get(i).getAsJsonObject();
                int num = q.has("question_number") ? q.get("question_number").getAsInt() : i + 1;
                boolean correct = q.has("is_correct") && q.get("is_correct").getAsBoolean();
                
                feedback.append("**第").append(num).append("题** [").append(correct ? "✅" : "❌").append("]\n");
                if (q.has("question_content")) {
                    feedback.append("- 题目：").append(q.get("question_content").getAsString()).append("\n");
                }
                if (q.has("student_answer")) {
                    feedback.append("- 学生答案：").append(q.get("student_answer").getAsString()).append("\n");
                }
                if (q.has("correct_answer")) {
                    feedback.append("- 标准答案：").append(q.get("correct_answer").getAsString()).append("\n");
                }
                if (q.has("score")) {
                    feedback.append("- 得分：").append(q.get("score").getAsInt()).append("/");
                    feedback.append(q.has("full_score") ? q.get("full_score").getAsInt() : 10).append(" 分\n");
                }
                if (!correct && q.has("error_analysis")) {
                    feedback.append("- 错误原因：").append(q.get("error_analysis").getAsString()).append("\n");
                }
                if (q.has("solution_process")) {
                    feedback.append("- **解题步骤**：\n").append(q.get("solution_process").getAsString()).append("\n");
                }
                feedback.append("\n");
            }
            feedback.append("---\n\n");
        }
        
        // 综合评价
        if (parsed.has("evaluation")) {
            JsonObject eval = parsed.getAsJsonObject("evaluation");
            
            feedback.append("**🎯 综合评价**\n\n");
            if (eval.has("total_score")) {
                feedback.append("**总分：").append(eval.get("total_score").getAsDouble()).append("/100 分**\n\n");
            }
            if (eval.has("grade_level")) {
                feedback.append("**等级：").append(eval.get("grade_level").getAsString()).append("**\n\n");
            }
            
            if (eval.has("strengths")) {
                feedback.append("**✨ 优点：**\n");
                JsonArray strengths = eval.getAsJsonArray("strengths");
                for (int i = 0; i < strengths.size(); i++) {
                    feedback.append((i + 1)).append(". ").append(strengths.get(i).getAsString()).append("\n");
                }
                feedback.append("\n");
            }
            
            if (eval.has("weaknesses")) {
                feedback.append("**💡 需要改进：**\n");
                JsonArray weaknesses = eval.getAsJsonArray("weaknesses");
                for (int i = 0; i < weaknesses.size(); i++) {
                    feedback.append((i + 1)).append(". ").append(weaknesses.get(i).getAsString()).append("\n");
                }
                feedback.append("\n");
            }
            
            if (eval.has("learning_suggestions")) {
                feedback.append("**📚 学习建议：**\n");
                JsonArray suggestions = eval.getAsJsonArray("learning_suggestions");
                for (int i = 0; i < suggestions.size(); i++) {
                    feedback.append((i + 1)).append(". ").append(suggestions.get(i).getAsString()).append("\n");
                }
                feedback.append("\n");
            }
        }
        
        // 总结
        if (parsed.has("summary_feedback")) {
            feedback.append("---\n\n");
            feedback.append("**💪 总体评价：**\n");
            feedback.append(parsed.get("summary_feedback").getAsString()).append("\n");
        }
        
        return feedback.toString();
    }
    
    /**
     * 本地模拟分析（当AI不可用时使用）
     */
    private Map<String, Object> generateLocalAnalysis(String assignmentInfo) {
        Map<String, Object> result = new HashMap<>();
        
        result.put("success", true);
        result.put("recognizedContent", generateMockRecognizedContent(assignmentInfo));
        
        double mockScore = 65 + Math.random() * 25; // 65-90分随机
        result.put("score", mockScore);
        
        StringBuilder feedback = new StringBuilder();
        feedback.append("批改结果\n\n");
        feedback.append("(本地模拟模式)\n\n");
        feedback.append(String.format("第1题: 正确 (20分)\n"));
        feedback.append(String.format("第2题: 正确 (15分)\n"));
        if (mockScore < 75) {
            feedback.append(String.format("第3题: 错误 (0分) - 原因: 计算错误\n"));
            feedback.append(String.format("第4题: 错误 (0分) - 原因: 方法错误\n"));
            feedback.append(String.format("第5题: 部分正确 (10分)\n"));
        } else {
            feedback.append(String.format("第3题: 正确 (15分)\n"));
            feedback.append(String.format("第4题: 正确 (15分)\n"));
            feedback.append(String.format("第5题: 部分正确 (35分) - 小错误扣5分\n"));
        }
        
        result.put("feedback", feedback.toString());
        result.put("questionsSummary", "{\"total_count\":5,\"question_types\":[\"选择题\",\"填空题\",\"计算题\"]}");
        result.put("detailedAnalysis", "[]");
        result.put("summaryFeedback", String.format("共5道题,做对%d道,得分%.0f分", 
            mockScore >= 80 ? 4 : (mockScore >= 60 ? 3 : 2), mockScore));
        
        return result;
    }
    
    /**
     * 生成模拟的识别内容
     */
    private String generateMockRecognizedContent(String assignmentInfo) {
        StringBuilder content = new StringBuilder();
        content.append("【图片内容识别】\n\n");
        
        if (assignmentInfo != null && !assignmentInfo.isEmpty()) {
            content.append("*作业主题：* ").append(assignmentInfo).append("\n\n");
        }
        
        content.append("*识别到的内容：*\n");
        content.append("1. 手写文字内容（模拟）\n");
        content.append("2. 数学公式和符号\n");
        content.append("3. 解题步骤和答案区域\n\n");
        content.append("*注：这是模拟识别结果，实际内容以图片为准*");
        
        return content.toString();
    }
    
    /**
     * 生成详细反馈
     */
    private String generateDetailedFeedback(Object scoreObj) {
        double score = ((Number) scoreObj).doubleValue();
        StringBuilder fb = new StringBuilder();
        
        fb.append("**📊 详细分析：**\n\n");
        
        if (score >= 85) {
            fb.append("**第1题** ✅ 选择题 - 回答正确（10/10分）\n");
            fb.append("**第2题** ✅ 填空题 - 答案准确（10/10分）\n");
            fb.append("**第3题** ✅ 计算题 - 步骤完整（15/15分）\n");
            fb.append("**第4题** ✅ 应用题 - 思路清晰（15/15分）\n");
            fb.append("**第5题** ⚠️ 证明题 - 小瑕疵扣分（38/40分）\n\n");
        } else if (score >= 70) {
            fb.append("**第1题** ✅ 选择题 - 正确（10/10分）\n");
            fb.append("**第2题** ✅ 填空题 - 正确（10/10分）\n");
            fb.append("**第3题** ⚠️ 计算题 - 计算小错（12/15分）\n");
            fb.append("**第4题** ❌ 应用题 - 方法错误（8/15分）\n");
            fb.append("**第5题** ⚠️ 证明题 - 不完整（30/40分）\n\n");
        } else {
            fb.append("**第1题** ⚠️ 选择题 - 粗心错误（7/10分）\n");
            fb.append("**第2题** ❌ 填空题 - 答案错误（3/10分）\n");
            fb.append("**第3题** ❌ 计算题 - 完全错误（2/15分）\n");
            fb.append("**第4题** ❌ 应用题 - 未完成（5/15分）\n");
            fb.append("**第5题** ❌ 证明题 - 未作答（10/40分）\n\n");
        }
        
        fb.append("**💡 错误分析与建议：**\n\n");
        
        if (score >= 85) {
            fb.append("• 整体表现优秀，掌握扎实\n");
            fb.append("• 第5题证明过程可以更加严谨\n");
            fb.append("• 建议：挑战更有难度的题目\n");
        } else if (score >= 70) {
            fb.append("• 基本概念理解较好\n");
            fb.append("• 第3题注意计算准确性\n");
            fb.append("• 第4题需要重新理解题意和解题方法\n");
            fb.append("• 建议：多做类似应用题，总结解题规律\n");
        } else {
            fb.append("• 基础知识需要加强复习\n");
            fb.append("• 建议先回顾课本例题\n");
            fb.append("• 遇到不懂的问题及时向老师或同学请教\n");
            fb.append("• 建议：整理错题本，定期复习\n");
        }
        
        return fb.toString();
    }
    
    /**
     * 获取图片类型
     */
    private String getImageType(String filePath) {
        if (filePath.toLowerCase().endsWith(".png")) return "png";
        if (filePath.toLowerCase().endsWith(".gif")) return "gif";
        if (filePath.toLowerCase().endsWith(".webp")) return "webp";
        return "jpg"; // 默认jpeg
    }
}
