package com.example.agentframework.workflow;

import lombok.Data;
import java.util.*;

@Data
public class WorkflowDefinition {
    private String id;
    private String name;
    private String description;
    private List<WorkflowStep> steps;
    private Map<String, Object> variables;
    private String createdBy;
    private Date createdAt;

    public WorkflowDefinition() {
        this.steps = new ArrayList<>();
        this.variables = new HashMap<>();
        this.createdAt = new Date();
    }

    public void addStep(WorkflowStep step) {
        this.steps.add(step);
    }
}
