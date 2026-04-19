package com.example.agentframework.workflow.agent;

import com.example.agentframework.agent.*;
import com.example.agentframework.service.AIService;
import com.example.agentframework.service.SmartImageReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GraderAgent implements Agent {

    @Autowired
    private AIService aiService;

    @Autowired
    private SmartImageReviewService smartImageReviewService;

    private AgentContext context;

    @Override
    public String getId() {
        return "grader-agent";
    }

    @Override
    public String getName() {
        return "智能批改Agent";
    }

    @Override
    public String getType() {
        return "grader";
    }

    @Override
    public void initialize(AgentContext context) {
        this.context = context;
    }

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            String action = request.getAction();
            String input = request.getInput();

            Map<String, Object> result;
            switch (action != null ? action : "grade") {
                case "grade":
                    result = gradeSubmission(request);
                    break;
                case "grade_image":
                    result = gradeImage(request);
                    break;
                case "review":
                    result = reviewSubmission(input);
                    break;
                case "feedback":
                    String feedback = generateFeedback(request);
                    AgentResponse fbResponse = AgentResponse.success(feedback);
                    fbResponse.setAgentId(getId());
                    fbResponse.setProcessingTime(System.currentTimeMillis() - startTime);
                    return fbResponse;
                default:
                    result = gradeSubmission(request);
            }

            AgentResponse response = AgentResponse.success("批改完成", result);
            response.setAgentId(getId());
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            return response;

        } catch (Exception e) {
            AgentResponse response = AgentResponse.failure("批改Agent处理失败: " + e.getMessage());
            response.setAgentId(getId());
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            return response;
        }
    }

    private Map<String, Object> gradeSubmission(AgentRequest request) {
        Map<String, Object> result = new HashMap<>();
        String input = request.getInput();
        String assignmentInfo = request.getParam("assignmentInfo") != null ?
                request.getParam("assignmentInfo").toString() : "";

        String systemPrompt = buildGraderSystemPrompt();
        String userMessage = "作业内容：" + input;
        if (!assignmentInfo.isEmpty()) {
            userMessage = "作业要求：" + assignmentInfo + "\n\n学生答案：" + input;
        }

        String aiResponse = aiService.chat(systemPrompt, userMessage);

        result.put("feedback", aiResponse);
        result.put("score", extractScore(aiResponse));
        result.put("gradedAt", new Date());

        return result;
    }

    private Map<String, Object> gradeImage(AgentRequest request) {
        String filePath = request.getParam("filePath") != null ?
                request.getParam("filePath").toString() : "";
        String assignmentInfo = request.getParam("assignmentInfo") != null ?
                request.getParam("assignmentInfo").toString() : "";

        if (!filePath.isEmpty()) {
            return smartImageReviewService.reviewImageByPath(filePath, assignmentInfo);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "未提供图片路径");
        return result;
    }

    private Map<String, Object> reviewSubmission(String submission) {
        String systemPrompt = "你是一个专业的作业审查老师。请审查学生提交的作业，" +
                "评估其完成度、正确性和改进空间。给出详细的审查意见。\n" +
                "要求使用JSON格式输出：\n" +
                "{\"completeness\": 0-100, \"correctness\": 0-100, \"suggestions\": [\"建议1\", \"建议2\"]}";

        String aiResponse = aiService.chat(systemPrompt, submission);

        Map<String, Object> result = new HashMap<>();
        result.put("review", aiResponse);
        return result;
    }

    private String generateFeedback(AgentRequest request) {
        String input = request.getInput();
        Object scoreObj = request.getParam("score");
        String score = scoreObj != null ? scoreObj.toString() : "未知";

        String systemPrompt = "你是一个鼓励性的老师。根据学生的作业得分和内容，" +
                "生成个性化的学习反馈和改进建议。\n" +
                "要求：\n" +
                "1. 先肯定学生的努力和进步\n" +
                "2. 指出具体的改进方向\n" +
                "3. 给出可操作的学习建议\n" +
                "4. 使用鼓励性的语言\n" +
                "使用中文回复。";

        return aiService.chat(systemPrompt, "学生得分：" + score + "分\n作业内容摘要：" + input);
    }

    private double extractScore(String text) {
        if (text == null) return 0;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*分");
        java.util.regex.Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private String buildGraderSystemPrompt() {
        return "你是一个专业的作业批改老师。请对学生的作业进行批改，要求：\n" +
                "1. 仔细评估每道题的对错\n" +
                "2. 给出具体评分（0-100分）\n" +
                "3. 对错误题目给出详细解析\n" +
                "4. 对错题提供举一反三的建议\n" +
                "5. 给出学习建议和改进方向\n\n" +
                "输出格式（JSON）：\n" +
                "{\"total_score\": 数字, \"questions\": [{\"num\": 题号, \"is_correct\": true/false, " +
                "\"score\": 得分, \"reason\": \"原因\", \"knowledge_point\": \"知识点\", " +
                "\"similar_question\": \"同类练习题\", \"solution_guide\": \"解题思路\"}], " +
                "\"summary\": \"总结\", \"suggestions\": [\"建议1\", \"建议2\"]}\n" +
                "使用中文回复。";
    }

    @Override
    public void learn(Object data) {
        if (context != null && data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> learningData = (Map<String, Object>) data;
            for (Map.Entry<String, Object> entry : learningData.entrySet()) {
                context.remember(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void reset() {
        if (context != null) {
            context.getMemory().clear();
        }
    }
}
