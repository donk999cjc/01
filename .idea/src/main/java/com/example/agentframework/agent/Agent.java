package com.example.agentframework.agent;

public interface Agent {
    String getId();
    String getName();
    String getType();
    void initialize(AgentContext context);
    AgentResponse process(AgentRequest request);
    void learn(Object data);
    void reset();
}
