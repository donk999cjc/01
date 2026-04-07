package com.example.agentframework.agent;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;
import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {
    private String requestId;
    private String agentId;
    private boolean success;
    private String output;
    private String message;
    private Map<String, Object> data;
    private Map<String, Object> metadata;
    private long processingTime;

    public static AgentResponse success(String output) {
        AgentResponse response = new AgentResponse();
        response.setSuccess(true);
        response.setOutput(output);
        response.setData(new HashMap<>());
        response.setMetadata(new HashMap<>());
        return response;
    }

    public static AgentResponse success(String output, Map<String, Object> data) {
        AgentResponse response = success(output);
        response.setData(data);
        return response;
    }

    public static AgentResponse failure(String message) {
        AgentResponse response = new AgentResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setData(new HashMap<>());
        response.setMetadata(new HashMap<>());
        return response;
    }

    public void addData(String key, Object value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(key, value);
    }

    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }
}
