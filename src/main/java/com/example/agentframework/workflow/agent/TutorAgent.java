package com.example.agentframework.workflow.agent;

import com.example.agentframework.agent.*;
import com.example.agentframework.service.AIService;
import com.example.agentframework.rag.RAGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TutorAgent implements Agent {

    @Autowired
    private AIService aiService;

    @Autowired
    private RAGService ragService;

    private AgentContext context;

    @Override
    public String getId() {
        return "tutor-agent";
    }

    @Override
    public String getName() {
        return "智能辅导Agent";
    }

    @Override
    public String getType() {
        return "tutor";
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
            String courseId = request.getCourseId();

            String result;
            switch (action != null ? action : "tutor") {
                case "tutor":
                    result = tutorWithRAG(input, courseId);
                    break;
                case "explain":
                    result = explainConcept(input, courseId);
                    break;
                case "practice":
                    result = generatePractice(input);
                    break;
                case "hint":
                    result = giveHint(input);
                    break;
                default:
                    result = tutorWithRAG(input, courseId);
            }

            AgentResponse response = AgentResponse.success(result);
            response.setAgentId(getId());
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            return response;

        } catch (Exception e) {
            AgentResponse response = AgentResponse.failure("辅导Agent处理失败: " + e.getMessage());
            response.setAgentId(getId());
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            return response;
        }
    }

    private String tutorWithRAG(String question, String courseId) {
        String ragResponse = ragService.queryWithRAG(question, courseId);
        if (ragResponse != null && !ragResponse.isEmpty()) {
            return ragResponse;
        }

        String systemPrompt = buildTutorSystemPrompt();
        return aiService.chat(systemPrompt, question);
    }

    private String explainConcept(String concept, String courseId) {
        String systemPrompt = "你是一个专业的教育辅导老师。请用通俗易懂的方式解释以下概念，" +
                "使用类比和例子帮助学生理解。要求：\n" +
                "1. 先给出简洁的定义\n" +
                "2. 用生活中的类比来解释\n" +
                "3. 给出具体的应用例子\n" +
                "4. 指出常见的理解误区\n" +
                "5. 使用中文回复";

        return aiService.chat(systemPrompt, "请解释概念：" + concept);
    }

    private String generatePractice(String topic) {
        String systemPrompt = "你是一个专业的教育辅导老师。请根据给定的主题，" +
                "生成3道由浅入深的练习题，包含：\n" +
                "1. 基础理解题（1道）\n" +
                "2. 应用题（1道）\n" +
                "3. 拓展题（1道）\n" +
                "每道题都要给出标准答案和解析。使用中文回复。";

        return aiService.chat(systemPrompt, "请为以下主题生成练习题：" + topic);
    }

    private String giveHint(String question) {
        String systemPrompt = "你是一个耐心的辅导老师。学生遇到了问题，" +
                "请不要直接给出答案，而是给出提示和引导，帮助学生自己找到答案。\n" +
                "要求：\n" +
                "1. 分析问题的关键点\n" +
                "2. 给出解题方向的提示\n" +
                "3. 提供相关的知识点\n" +
                "4. 鼓励学生继续思考\n" +
                "使用中文回复。";

        return aiService.chat(systemPrompt, question);
    }

    private String buildTutorSystemPrompt() {
        return "你是一个专业的AI学习辅导老师，具备以下能力：\n" +
                "1. 解答学生的学习问题，提供清晰的解释\n" +
                "2. 根据学生的理解水平调整解释的深度\n" +
                "3. 引导学生思考，而不是直接给出答案\n" +
                "4. 提供个性化的学习建议和资源推荐\n" +
                "5. 使用鼓励性的语言，营造积极的学习氛围\n\n" +
                "回复要求：\n" +
                "- 使用中文回复\n" +
                "- 回答要准确、有条理\n" +
                "- 适当使用例子和类比帮助理解\n" +
                "- 对于复杂问题，分步骤解释";
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
