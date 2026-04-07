package com.example.agentframework.agent;

import lombok.Data;
import java.util.Map;
import java.util.HashMap;

@Data
public class AgentContext {
    private String agentId;
    private String agentName;
    private String courseId;
    private String agentType;
    private Map<String, Object> parameters;
    private Map<String, Object> memory;
    private Map<String, Object> knowledge;

    public AgentContext() {
        this.parameters = new HashMap<>();
        this.memory = new HashMap<>();
        this.knowledge = new HashMap<>();
    }

    public void setParameter(String key, Object value) {
        parameters.put(key, value);
    }

    public Object getParameter(String key) {
        return parameters.get(key);
    }

    public void remember(String key, Object value) {
        memory.put(key, value);
    }

    public Object recall(String key) {
        return memory.get(key);
    }

    public void storeKnowledge(String key, Object value) {
        knowledge.put(key, value);
    }

    public Object getKnowledge(String key) {
        return knowledge.get(key);
    }
}
