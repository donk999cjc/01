package com.example.agentframework.workflow;

import lombok.Data;
import java.util.*;

@Data
public class WorkflowExecution {
    private String executionId;
    private String workflowId;
    private String workflowName;
    private WorkflowStatus status;
    private List<StepExecution> stepExecutions;
    private Map<String, Object> context;
    private Map<String, Object> result;
    private Date startTime;
    private Date endTime;
    private String error;

    public WorkflowExecution() {
        this.stepExecutions = new ArrayList<>();
        this.context = new HashMap<>();
        this.result = new HashMap<>();
        this.status = WorkflowStatus.PENDING;
    }

    public enum WorkflowStatus {
        PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
    }

    @Data
    public static class StepExecution {
        private String stepId;
        private String stepName;
        private String agentType;
        private StepStatus status;
        private Map<String, Object> input;
        private Map<String, Object> output;
        private Date startTime;
        private Date endTime;
        private long duration;
        private String error;
        private int retryAttempt;

        public StepExecution() {
            this.input = new HashMap<>();
            this.output = new HashMap<>();
            this.status = StepStatus.PENDING;
        }
    }

    public enum StepStatus {
        PENDING, RUNNING, COMPLETED, FAILED, SKIPPED
    }
}
