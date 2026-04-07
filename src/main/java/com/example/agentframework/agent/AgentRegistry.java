package com.example.agentframework.agent;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgentRegistry {
    
    private final Map<String, Class<? extends Agent>> agentTypes = new ConcurrentHashMap<>();
    private final Map<String, Agent> agentInstances = new ConcurrentHashMap<>();

    public void registerAgentType(String type, Class<? extends Agent> agentClass) {
        agentTypes.put(type, agentClass);
    }

    public void registerAgent(String id, Agent agent) {
        agentInstances.put(id, agent);
    }

    public Agent getAgent(String id) {
        return agentInstances.get(id);
    }

    public void unregisterAgent(String id) {
        agentInstances.remove(id);
    }

    public boolean hasAgent(String id) {
        return agentInstances.containsKey(id);
    }

    public Class<? extends Agent> getAgentType(String type) {
        return agentTypes.get(type);
    }

    public Map<String, Agent> getAllAgents() {
        return new HashMap<>(agentInstances);
    }
}
