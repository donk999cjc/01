package com.example.agentframework.agent;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;
import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentRequest {
    private String requestId;
    private String agentId;
    private String action;
    private String input;
    private Map<String, Object> params;
    private String userId;
    private String courseId;
    private Long timestamp;

    public AgentRequest(String agentId, String action, String input) {
        this.agentId = agentId;
        this.action = action;
        this.input = input;
        this.params = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }

    public void addParam(String key, Object value) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(key, value);
    }

    public Object getParam(String key) {
        return params != null ? params.get(key) : null;
    }
}
