package com.example.agentframework.controller;

import com.example.agentframework.service.impl.AssignmentReviewServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/review")
@CrossOrigin
public class ReviewController {

    @Autowired
    private AssignmentReviewServiceImpl reviewService;

    /**
     * 精细化作业批注
     */
    @PostMapping("/assignment/{submissionId}")
    public ResponseEntity<Map<String, Object>> reviewAssignment(@PathVariable Long submissionId) {
        Map<String, Object> result = reviewService.reviewAssignment(submissionId);
        if ((Boolean) result.get("success")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 批量批改作业
     */
    @PostMapping("/batch")
    public ResponseEntity<List<Map<String, Object>>> batchReviewAssignments(@RequestBody List<Long> submissionIds) {
        List<Map<String, Object>> results = reviewService.batchReviewAssignments(submissionIds);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    /**
     * 生成作业统计报告
     */
    @GetMapping("/report/{assignmentId}")
    public ResponseEntity<Map<String, Object>> generateReviewReport(@PathVariable String assignmentId) {
        Map<String, Object> report = reviewService.generateReviewReport(assignmentId);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }
}
