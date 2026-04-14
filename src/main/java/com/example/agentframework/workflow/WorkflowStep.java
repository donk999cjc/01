package com.example.agentframework.workflow;

import lombok.Data;
import java.util.*;

@Data
public class WorkflowStep {
    private String id;
    private String name;
    private String agentType;
    private String action;
    private Map<String, Object> inputMapping;
    private Map<String, Object> outputMapping;
    private String condition;
    private List<String> nextSteps;
    private List<String> fallbackSteps;
    private int timeout;
    private int retryCount;
    private int retryDelay;

    public WorkflowStep() {
        this.inputMapping = new HashMap<>();
        this.outputMapping = new HashMap<>();
        this.nextSteps = new ArrayList<>();
        this.fallbackSteps = new ArrayList<>();
        this.timeout = 60;
        this.retryCount = 1;
        this.retryDelay = 1000;
    }
}
