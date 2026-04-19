package com.example.agentframework.controller;

import com.example.agentframework.entity.Submission;
import com.example.agentframework.service.SubmissionService;
import com.example.agentframework.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<Submission> createSubmission(@RequestBody Submission submission) {
        Submission createdSubmission = submissionService.createSubmission(submission);
        return new ResponseEntity<>(createdSubmission, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Submission> getSubmissionById(@PathVariable Long id) {
        return submissionService.getSubmissionById(id)
                .map(submission -> new ResponseEntity<>(submission, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Submission>> getSubmissionsByStudentId(@PathVariable String studentId) {
        List<Submission> submissions = submissionService.getSubmissionsByStudentId(studentId);
        // 为每个提交记录生成imageUrl
        submissions.forEach(submission -> {
            if (submission.getFilePath() != null && !submission.getFilePath().isEmpty()) {
                submission.setImageUrl(fileStorageService.getFileUrl(submission.getFilePath()));
            }
        });
        return new ResponseEntity<>(submissions, HttpStatus.OK);
    }

    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<List<Submission>> getSubmissionsByAssignmentId(@PathVariable String assignmentId) {
        List<Submission> submissions = submissionService.getSubmissionsByAssignmentId(assignmentId);
        // 为每个提交记录生成imageUrl
        submissions.forEach(submission -> {
            if (submission.getFilePath() != null && !submission.getFilePath().isEmpty()) {
                submission.setImageUrl(fileStorageService.getFileUrl(submission.getFilePath()));
            }
        });
        return new ResponseEntity<>(submissions, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}/assignment/{assignmentId}")
    public ResponseEntity<Submission> getSubmissionByStudentIdAndAssignmentId(@PathVariable String studentId, @PathVariable String assignmentId) {
        Submission submission = submissionService.getSubmissionByStudentIdAndAssignmentId(studentId, assignmentId);
        if (submission != null) {
            // 生成完整的图片访问URL
            if (submission.getFilePath() != null && !submission.getFilePath().isEmpty()) {
                submission.setImageUrl(fileStorageService.getFileUrl(submission.getFilePath()));
            }
            return new ResponseEntity<>(submission, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<Submission>> getAllSubmissions() {
        List<Submission> submissions = submissionService.getAllSubmissions();
        // 为每个提交记录生成imageUrl
        submissions.forEach(submission -> {
            if (submission.getFilePath() != null && !submission.getFilePath().isEmpty()) {
                submission.setImageUrl(fileStorageService.getFileUrl(submission.getFilePath()));
            }
        });
        return new ResponseEntity<>(submissions, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Submission> updateSubmission(@PathVariable Long id, @RequestBody Submission submission) {
        Submission updatedSubmission = submissionService.updateSubmission(id, submission);
        if (updatedSubmission != null) {
            return new ResponseEntity<>(updatedSubmission, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Long id) {
        submissionService.deleteSubmission(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
