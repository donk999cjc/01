package com.example.agentframework.controller;

import com.example.agentframework.workflow.WorkflowDefinition;
import com.example.agentframework.workflow.WorkflowEngine;
import com.example.agentframework.workflow.WorkflowExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/workflows")
@CrossOrigin
public class WorkflowController {

    @Autowired
    private WorkflowEngine workflowEngine;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listWorkflows() {
        Collection<WorkflowDefinition> workflows = workflowEngine.getWorkflowDefinitions();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("workflows", workflows);
        result.put("count", workflows.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getWorkflow(@PathVariable String id) {
        WorkflowDefinition workflow = workflowEngine.getWorkflowDefinition(id);
        if (workflow == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "工作流不存在");
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("workflow", workflow);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<Map<String, Object>> executeWorkflow(
            @PathVariable String id,
            @RequestBody Map<String, Object> input) {

        WorkflowExecution execution = workflowEngine.execute(id, input);

        Map<String, Object> result = new HashMap<>();
        result.put("success", execution.getStatus() == WorkflowExecution.WorkflowStatus.COMPLETED);
        result.put("executionId", execution.getExecutionId());
        result.put("status", execution.getStatus().toString());
        result.put("result", execution.getResult());
        result.put("context", execution.getContext());
        result.put("stepExecutions", execution.getStepExecutions());
        result.put("error", execution.getError());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/edu/tutor")
    public ResponseEntity<Map<String, Object>> eduTutor(@RequestBody Map<String, String> request) {
        String studentId = request.get("studentId");
        String courseId = request.get("courseId");
        String question = request.get("question");

        if (question == null || question.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "问题不能为空");
            return ResponseEntity.badRequest().body(error);
        }

        WorkflowExecution execution = workflowEngine.executeEduWorkflow(studentId, courseId, question);

        Map<String, Object> result = new HashMap<>();
        result.put("success", execution.getStatus() == WorkflowExecution.WorkflowStatus.COMPLETED);
        result.put("executionId", execution.getExecutionId());

        if (execution.getContext().containsKey("tutorResponse")) {
            result.put("response", execution.getContext().get("tutorResponse"));
        }
        if (execution.getContext().containsKey("learningPath")) {
            result.put("learningPath", execution.getContext().get("learningPath"));
        }
        if (execution.getContext().containsKey("studentAnalysis")) {
            result.put("studentAnalysis", execution.getContext().get("studentAnalysis"));
        }

        result.put("stepExecutions", execution.getStepExecutions());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/edu/grade")
    public ResponseEntity<Map<String, Object>> eduGrade(@RequestBody Map<String, String> request) {
        String studentId = request.get("studentId");
        String assignmentId = request.get("assignmentId");
        String filePath = request.get("filePath");
        String assignmentInfo = request.get("assignmentInfo");

        WorkflowExecution execution = workflowEngine.executeGradingWorkflow(
                studentId, assignmentId, filePath, assignmentInfo);

        Map<String, Object> result = new HashMap<>();
        result.put("success", execution.getStatus() == WorkflowExecution.WorkflowStatus.COMPLETED);
        result.put("executionId", execution.getExecutionId());

        if (execution.getContext().containsKey("gradingResult")) {
            result.put("gradingResult", execution.getContext().get("gradingResult"));
        }
        if (execution.getContext().containsKey("feedback")) {
            result.put("feedback", execution.getContext().get("feedback"));
        }

        result.put("stepExecutions", execution.getStepExecutions());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/edu/analyze")
    public ResponseEntity<Map<String, Object>> eduAnalyze(@RequestBody Map<String, String> request) {
        String studentId = request.get("studentId");
        String courseId = request.get("courseId");

        WorkflowExecution execution = workflowEngine.executeAnalysisWorkflow(studentId, courseId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", execution.getStatus() == WorkflowExecution.WorkflowStatus.COMPLETED);
        result.put("executionId", execution.getExecutionId());

        if (execution.getContext().containsKey("analysisResult")) {
            result.put("analysisResult", execution.getContext().get("analysisResult"));
        }
        if (execution.getContext().containsKey("prediction")) {
            result.put("prediction", execution.getContext().get("prediction"));
        }
        if (execution.getContext().containsKey("learningPath")) {
            result.put("learningPath", execution.getContext().get("learningPath"));
        }

        result.put("stepExecutions", execution.getStepExecutions());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/executions")
    public ResponseEntity<Map<String, Object>> getExecutionHistory() {
        Collection<WorkflowExecution> executions = workflowEngine.getExecutionHistory();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("executions", executions);
        result.put("count", executions.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/executions/{executionId}")
    public ResponseEntity<Map<String, Object>> getExecution(@PathVariable String executionId) {
        WorkflowExecution execution = workflowEngine.getExecution(executionId);
        if (execution == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "执行记录不存在");
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("execution", execution);
        return ResponseEntity.ok(result);
    }
}
