package com.example.agentframework.service;

import com.example.agentframework.entity.Agent;
import com.example.agentframework.mapper.AgentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AgentService {

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private AIService aiService;

    public Agent createAgent(Agent agent) {
        agentMapper.insert(agent);
        return agent;
    }

    public Optional<Agent> getAgentById(Long id) {
        Agent agent = agentMapper.findById(id);
        return Optional.ofNullable(agent);
    }

    public List<Agent> getAgentsByCourseId(String courseId) {
        return agentMapper.findByCourseId(courseId);
    }

    public List<Agent> getAllAgents() {
        return agentMapper.findAll();
    }

    public Agent updateAgent(Long id, Agent agent) {
        Agent existingAgent = agentMapper.findById(id);
        if (existingAgent != null) {
            existingAgent.setName(agent.getName());
            existingAgent.setCourseId(agent.getCourseId());
            existingAgent.setDescription(agent.getDescription());
            existingAgent.setConfig(agent.getConfig());
            if (agent.getAvatar() != null) {
                // 限制avatar字段长度，避免数据库截断错误
                String avatar = agent.getAvatar();
                if (avatar.length() > 250) {
                    avatar = avatar.substring(0, 250);
                }
                existingAgent.setAvatar(avatar);
            }
            agentMapper.update(existingAgent);
            return existingAgent;
        }
        return null;
    }

    public void deleteAgent(Long id) {
        agentMapper.deleteById(id);
    }

    public String chatWithAgent(Long agentId, String message) {
        Agent agent = agentMapper.findById(agentId);
        if (agent == null) {
            return "未找到该智能体。";
        }
        
        // 构建系统提示词，定义AI角色
        String systemPrompt = buildSystemPrompt(agent);
        
        // 调用AI服务进行对话
        return aiService.chat(systemPrompt, message);
    }

    /**
     * 构建系统提示词，定义AI助手的角色和行为
     */
    private String buildSystemPrompt(Agent agent) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是").append(agent.getName()).append("，一个专业的AI学习助手。\n\n");
        
        // 添加智能体描述
        if (agent.getDescription() != null && !agent.getDescription().isEmpty()) {
            prompt.append("你的专业领域：").append(agent.getDescription()).append("\n\n");
        }
        
        // 定义AI的行为准则
        prompt.append("你的职责：\n");
        prompt.append("1. 帮助学生解答学习问题，提供清晰、准确的解释\n");
        prompt.append("2. 引导学生思考，而不是直接给出答案\n");
        prompt.append("3. 使用友好、鼓励的语气，营造轻松的学习氛围\n");
        prompt.append("4. 根据学生的问题，提供相关的学习资源和建议\n");
        prompt.append("5. 如果问题不清楚，主动询问以获取更多信息\n\n");
        
        prompt.append("回复要求：\n");
        prompt.append("1. 使用中文回复\n");
        prompt.append("2. 回答要简洁明了，重点突出\n");
        prompt.append("3. 适当使用emoji增加亲和力\n");
        prompt.append("4. 对于复杂问题，分步骤解释\n");
        prompt.append("5. 鼓励学生继续提问和探索\n");
        
        return prompt.toString();
    }
}
