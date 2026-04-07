package com.example.agentframework.controller;

import com.example.agentframework.entity.Assignment;
import com.example.agentframework.entity.Submission;
import com.example.agentframework.service.AssignmentService;
import com.example.agentframework.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/assignments")
@CrossOrigin
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<Assignment> createAssignment(@RequestBody Assignment assignment) {
        Assignment createdAssignment = assignmentService.createAssignment(assignment);
        return new ResponseEntity<>(createdAssignment, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Assignment> getAssignmentById(@PathVariable Long id) {
        return assignmentService.getAssignmentById(id)
                .map(assignment -> new ResponseEntity<>(assignment, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/assignmentId/{assignmentId}")
    public ResponseEntity<Assignment> getAssignmentByAssignmentId(@PathVariable String assignmentId) {
        Assignment assignment = assignmentService.getAssignmentByAssignmentId(assignmentId);
        if (assignment != null) {
            return new ResponseEntity<>(assignment, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Assignment>> getAssignmentsByCourseId(@PathVariable String courseId) {
        List<Assignment> assignments = assignmentService.getAssignmentsByCourseId(courseId);
        return new ResponseEntity<>(assignments, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Assignment>> getAllAssignments() {
        List<Assignment> assignments = assignmentService.getAllAssignments();
        return new ResponseEntity<>(assignments, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Assignment> updateAssignment(@PathVariable Long id, @RequestBody Assignment assignment) {
        Assignment updatedAssignment = assignmentService.updateAssignment(id, assignment);
        if (updatedAssignment != null) {
            return new ResponseEntity<>(updatedAssignment, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Submission> submitAssignment(@PathVariable Long id, @RequestBody Submission submission) {
        submission.setAssignmentId(String.valueOf(id));
        Submission createdSubmission = submissionService.createSubmission(submission);
        return new ResponseEntity<>(createdSubmission, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<Map<String, Object>> reviewSubmission(@PathVariable Long id, @RequestBody Map<String, Object> reviewData) {
        String studentId = (String) reviewData.get("studentId");
        Double score = Double.valueOf(reviewData.get("score").toString());
        String feedback = (String) reviewData.get("feedback");
        
        Submission submission = submissionService.getSubmissionByStudentIdAndAssignmentId(studentId, String.valueOf(id));
        Map<String, Object> result = new HashMap<>();
        
        if (submission != null) {
            submission.setScore(score);
            submission.setFeedback(feedback);
            submission.setStatus("graded");
            submissionService.updateSubmission(submission.getId(), submission);
            result.put("success", true);
            result.put("message", "批改成功");
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        result.put("success", false);
        result.put("message", "未找到提交记录");
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{id}/submissions")
    public ResponseEntity<List<Submission>> getAssignmentSubmissions(@PathVariable Long id) {
        List<Submission> submissions = submissionService.getSubmissionsByAssignmentId(String.valueOf(id));
        return new ResponseEntity<>(submissions, HttpStatus.OK);
    }
}
