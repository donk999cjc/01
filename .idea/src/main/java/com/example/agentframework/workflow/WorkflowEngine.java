package com.example.agentframework.workflow;

import com.example.agentframework.agent.*;
import com.example.agentframework.workflow.agent.TutorAgent;
import com.example.agentframework.workflow.agent.GraderAgent;
import com.example.agentframework.workflow.agent.AnalystAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WorkflowEngine {

    @Autowired
    private AgentRegistry agentRegistry;

    @Autowired
    private TutorAgent tutorAgent;

    @Autowired
    private GraderAgent graderAgent;

    @Autowired
    private AnalystAgent analystAgent;

    private final Map<String, WorkflowDefinition> workflowDefinitions = new ConcurrentHashMap<>();
    private final Map<String, WorkflowExecution> executionHistory = new ConcurrentHashMap<>();

    public void init() {
        agentRegistry.registerAgent(tutorAgent.getId(), tutorAgent);
        agentRegistry.registerAgent(graderAgent.getId(), graderAgent);
        agentRegistry.registerAgent(analystAgent.getId(), analystAgent);

        registerEduWorkflow();
        registerGradingWorkflow();
        registerAnalysisWorkflow();
    }

    public void registerWorkflow(WorkflowDefinition definition) {
        workflowDefinitions.put(definition.getId(), definition);
    }

    public WorkflowExecution execute(String workflowId, Map<String, Object> input) {
        WorkflowDefinition definition = workflowDefinitions.get(workflowId);
        if (definition == null) {
            WorkflowExecution execution = new WorkflowExecution();
            execution.setStatus(WorkflowExecution.WorkflowStatus.FAILED);
            execution.setError("Workflow not found: " + workflowId);
            return execution;
        }

        return executeWorkflow(definition, input);
    }

    public WorkflowExecution executeEduWorkflow(String studentId, String courseId, String question) {
        Map<String, Object> input = new HashMap<>();
        input.put("studentId", studentId);
        input.put("courseId", courseId);
        input.put("question", question);
        return execute("edu-workflow", input);
    }

    public WorkflowExecution executeGradingWorkflow(String studentId, String assignmentId, String filePath, String assignmentInfo) {
        Map<String, Object> input = new HashMap<>();
        input.put("studentId", studentId);
        input.put("assignmentId", assignmentId);
        input.put("filePath", filePath);
        input.put("assignmentInfo", assignmentInfo);
        return execute("grading-workflow", input);
    }

    public WorkflowExecution executeAnalysisWorkflow(String studentId, String courseId) {
        Map<String, Object> input = new HashMap<>();
        input.put("studentId", studentId);
        input.put("courseId", courseId);
        return execute("analysis-workflow", input);
    }

    private WorkflowExecution executeWorkflow(WorkflowDefinition definition, Map<String, Object> input) {
        WorkflowExecution execution = new WorkflowExecution();
        execution.setExecutionId(UUID.randomUUID().toString());
        execution.setWorkflowId(definition.getId());
        execution.setWorkflowName(definition.getName());
        execution.setStartTime(new Date());
        execution.setStatus(WorkflowExecution.WorkflowStatus.RUNNING);
        execution.getContext().putAll(input);

        if (definition.getVariables() != null) {
            execution.getContext().putAll(definition.getVariables());
        }

        try {
            for (WorkflowStep step : definition.getSteps()) {
                if (!evaluateCondition(step.getCondition(), execution.getContext())) {
                    WorkflowExecution.StepExecution skipExec = createStepExecution(step);
                    skipExec.setStatus(WorkflowExecution.StepStatus.SKIPPED);
                    execution.getStepExecutions().add(skipExec);
                    continue;
                }

                WorkflowExecution.StepExecution stepExec = executeStep(step, execution);
                execution.getStepExecutions().add(stepExec);

                if (stepExec.getStatus() == WorkflowExecution.StepStatus.FAILED) {
                    if (step.getFallbackSteps() != null && !step.getFallbackSteps().isEmpty()) {
                        boolean fallbackSuccess = executeFallbackSteps(step.getFallbackSteps(), execution);
                        if (!fallbackSuccess) {
                            execution.setStatus(WorkflowExecution.WorkflowStatus.FAILED);
                            execution.setError("Step failed and fallback also failed: " + step.getName());
                            break;
                        }
                    } else {
                        execution.setStatus(WorkflowExecution.WorkflowStatus.FAILED);
                        execution.setError("Step failed: " + step.getName() + " - " + stepExec.getError());
                        break;
                    }
                }

                mapOutputs(step, stepExec, execution.getContext());
            }

            if (execution.getStatus() == WorkflowExecution.WorkflowStatus.RUNNING) {
                execution.setStatus(WorkflowExecution.WorkflowStatus.COMPLETED);
            }

        } catch (Exception e) {
            execution.setStatus(WorkflowExecution.WorkflowStatus.FAILED);
            execution.setError("Workflow execution error: " + e.getMessage());
        }

        execution.setEndTime(new Date());
        executionHistory.put(execution.getExecutionId(), execution);

        return execution;
    }

    private WorkflowExecution.StepExecution executeStep(WorkflowStep step, WorkflowExecution execution) {
        WorkflowExecution.StepExecution stepExec = createStepExecution(step);
        stepExec.setStatus(WorkflowExecution.StepStatus.RUNNING);
        stepExec.setStartTime(new Date());

        try {
            Agent agent = findAgent(step.getAgentType());
            if (agent == null) {
                stepExec.setStatus(WorkflowExecution.StepStatus.FAILED);
                stepExec.setError("Agent not found: " + step.getAgentType());
                stepExec.setEndTime(new Date());
                return stepExec;
            }

            AgentRequest request = buildAgentRequest(step, execution.getContext());
            AgentResponse response = agent.process(request);

            if (response.isSuccess()) {
                stepExec.setStatus(WorkflowExecution.StepStatus.COMPLETED);
                stepExec.getOutput().put("result", response.getOutput());
                if (response.getData() != null) {
                    stepExec.getOutput().putAll(response.getData());
                }
            } else {
                stepExec.setStatus(WorkflowExecution.StepStatus.FAILED);
                stepExec.setError(response.getMessage());
            }

        } catch (Exception e) {
            stepExec.setStatus(WorkflowExecution.StepStatus.FAILED);
            stepExec.setError(e.getMessage());
        }

        stepExec.setEndTime(new Date());
        if (stepExec.getStartTime() != null && stepExec.getEndTime() != null) {
            stepExec.setDuration(stepExec.getEndTime().getTime() - stepExec.getStartTime().getTime());
        }

        return stepExec;
    }

    private boolean executeFallbackSteps(List<String> fallbackStepIds, WorkflowExecution execution) {
        return true;
    }

    private Agent findAgent(String agentType) {
        switch (agentType) {
            case "tutor": return tutorAgent;
            case "grader": return graderAgent;
            case "analyst": return analystAgent;
            default: return agentRegistry.getAgent(agentType);
        }
    }

    private AgentRequest buildAgentRequest(WorkflowStep step, Map<String, Object> context) {
        AgentRequest request = new AgentRequest();
        request.setAgentId(step.getAgentType());
        request.setAction(step.getAction());

        StringBuilder inputBuilder = new StringBuilder();
        if (step.getInputMapping() != null) {
            for (Map.Entry<String, Object> mapping : step.getInputMapping().entrySet()) {
                String key = mapping.getKey();
                Object value = mapping.getValue();

                if (value instanceof String && ((String) value).startsWith("$")) {
                    String contextKey = ((String) value).substring(1);
                    Object contextValue = context.get(contextKey);
                    if (contextValue != null) {
                        request.addParam(key, contextValue);
                        inputBuilder.append(key).append(": ").append(contextValue).append("; ");
                    }
                } else {
                    request.addParam(key, value);
                    inputBuilder.append(key).append(": ").append(value).append("; ");
                }
            }
        }

        request.setInput(inputBuilder.toString());

        if (context.containsKey("courseId")) {
            request.setCourseId((String) context.get("courseId"));
        }
        if (context.containsKey("studentId")) {
            request.setUserId((String) context.get("studentId"));
        }

        return request;
    }

    private void mapOutputs(WorkflowStep step, WorkflowExecution.StepExecution stepExec, Map<String, Object> context) {
        if (step.getOutputMapping() == null || stepExec.getOutput() == null) return;

        for (Map.Entry<String, Object> mapping : step.getOutputMapping().entrySet()) {
            String contextKey = mapping.getKey();
            Object sourceKey = mapping.getValue();

            if (sourceKey instanceof String) {
                Object value = stepExec.getOutput().get(sourceKey);
                if (value != null) {
                    context.put(contextKey, value);
                }
            }
        }

        context.put("lastStepOutput", stepExec.getOutput());
        context.put("lastStepResult", stepExec.getOutput().get("result"));
    }

    private boolean evaluateCondition(String condition, Map<String, Object> context) {
        if (condition == null || condition.isEmpty()) return true;

        if (condition.startsWith("context.")) {
            String key = condition.substring("context.".length());
            int eqIdx = key.indexOf("==");
            if (eqIdx > 0) {
                String leftKey = key.substring(0, eqIdx).trim();
                String rightVal = key.substring(eqIdx + 2).trim();
                Object leftVal = context.get(leftKey);
                return leftVal != null && leftVal.toString().equals(rightVal.replace("\"", ""));
            }
        }

        return true;
    }

    private WorkflowExecution.StepExecution createStepExecution(WorkflowStep step) {
        WorkflowExecution.StepExecution exec = new WorkflowExecution.StepExecution();
        exec.setStepId(step.getId());
        exec.setStepName(step.getName());
        exec.setAgentType(step.getAgentType());
        return exec;
    }

    private void registerEduWorkflow() {
        WorkflowDefinition workflow = new WorkflowDefinition();
        workflow.setId("edu-workflow");
        workflow.setName("智能辅导工作流");
        workflow.setDescription("Tutor+Grader+Analyst 协同辅导工作流");

        WorkflowStep step1 = new WorkflowStep();
        step1.setId("analyze-student");
        step1.setName("学情分析");
        step1.setAgentType("analyst");
        step1.setAction("analyze");
        step1.getInputMapping().put("studentId", "$studentId");
        step1.getOutputMapping().put("studentAnalysis", "result");
        workflow.addStep(step1);

        WorkflowStep step2 = new WorkflowStep();
        step2.setId("tutor-answer");
        step2.setName("智能辅导");
        step2.setAgentType("tutor");
        step2.setAction("tutor");
        step2.getInputMapping().put("question", "$question");
        step2.getInputMapping().put("courseId", "$courseId");
        step2.getOutputMapping().put("tutorResponse", "result");
        workflow.addStep(step2);

        WorkflowStep step3 = new WorkflowStep();
        step3.setId("recommend-path");
        step3.setName("学习路径推荐");
        step3.setAgentType("analyst");
        step3.setAction("recommend");
        step3.getInputMapping().put("studentId", "$studentId");
        step3.getInputMapping().put("courseId", "$courseId");
        step3.getOutputMapping().put("learningPath", "result");
        workflow.addStep(step3);

        registerWorkflow(workflow);
    }

    private void registerGradingWorkflow() {
        WorkflowDefinition workflow = new WorkflowDefinition();
        workflow.setId("grading-workflow");
        workflow.setName("智能批改工作流");
        workflow.setDescription("Grader+Analyst 协同批改工作流");

        WorkflowStep step1 = new WorkflowStep();
        step1.setId("grade-submission");
        step1.setName("智能批改");
        step1.setAgentType("grader");
        step1.setAction("grade_image");
        step1.getInputMapping().put("filePath", "$filePath");
        step1.getInputMapping().put("assignmentInfo", "$assignmentInfo");
        step1.getOutputMapping().put("gradingResult", "result");
        step1.getOutputMapping().put("score", "score");
        workflow.addStep(step1);

        WorkflowStep step2 = new WorkflowStep();
        step2.setId("generate-feedback");
        step2.setName("生成反馈");
        step2.setAgentType("grader");
        step2.setAction("feedback");
        step2.getInputMapping().put("score", "$score");
        step2.getInputMapping().put("assignmentInfo", "$assignmentInfo");
        step2.getOutputMapping().put("feedback", "result");
        workflow.addStep(step2);

        WorkflowStep step3 = new WorkflowStep();
        step3.setId("analyze-performance");
        step3.setName("学情更新");
        step3.setAgentType("analyst");
        step3.setAction("analyze");
        step3.getInputMapping().put("studentId", "$studentId");
        step3.getOutputMapping().put("performanceAnalysis", "result");
        workflow.addStep(step3);

        registerWorkflow(workflow);
    }

    private void registerAnalysisWorkflow() {
        WorkflowDefinition workflow = new WorkflowDefinition();
        workflow.setId("analysis-workflow");
        workflow.setName("学情分析工作流");
        workflow.setDescription("Analyst+Tutor 协同分析工作流");

        WorkflowStep step1 = new WorkflowStep();
        step1.setId("analyze-student");
        step1.setName("学情分析");
        step1.setAgentType("analyst");
        step1.setAction("analyze");
        step1.getInputMapping().put("studentId", "$studentId");
        step1.getOutputMapping().put("analysisResult", "result");
        workflow.addStep(step1);

        WorkflowStep step2 = new WorkflowStep();
        step2.setId("predict-performance");
        step2.setName("成绩预测");
        step2.setAgentType("analyst");
        step2.setAction("predict");
        step2.getInputMapping().put("studentId", "$studentId");
        step2.getOutputMapping().put("prediction", "result");
        workflow.addStep(step2);

        WorkflowStep step3 = new WorkflowStep();
        step3.setId("recommend-path");
        step3.setName("学习路径推荐");
        step3.setAgentType("analyst");
        step3.setAction("recommend");
        step3.getInputMapping().put("studentId", "$studentId");
        step3.getInputMapping().put("courseId", "$courseId");
        step3.getOutputMapping().put("learningPath", "result");
        workflow.addStep(step3);

        registerWorkflow(workflow);
    }

    public Collection<WorkflowDefinition> getWorkflowDefinitions() {
        return workflowDefinitions.values();
    }

    public WorkflowDefinition getWorkflowDefinition(String id) {
        return workflowDefinitions.get(id);
    }

    public Collection<WorkflowExecution> getExecutionHistory() {
        return executionHistory.values();
    }

    public WorkflowExecution getExecution(String executionId) {
        return executionHistory.get(executionId);
    }
}
