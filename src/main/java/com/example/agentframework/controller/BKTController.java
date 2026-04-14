package com.example.agentframework.controller;

import com.example.agentframework.bkt.BKTService;
import com.example.agentframework.bkt.BKTModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/bkt")
@CrossOrigin
public class BKTController {

    @Autowired
    private BKTService bktService;

    @PostMapping("/attempt")
    public ResponseEntity<Map<String, Object>> recordAttempt(@RequestBody Map<String, Object> request) {
        String studentId = (String) request.get("studentId");
        String knowledgeId = (String) request.get("knowledgeId");
        String courseId = (String) request.get("courseId");
        Boolean correct = (Boolean) request.get("correct");

        if (studentId == null || knowledgeId == null || correct == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "缺少必要参数: studentId, knowledgeId, correct");
            return ResponseEntity.badRequest().body(error);
        }

        double newMastery = bktService.recordAttempt(studentId, knowledgeId, courseId != null ? courseId : "", correct);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("studentId", studentId);
        result.put("knowledgeId", knowledgeId);
        result.put("correct", correct);
        result.put("newMasteryLevel", newMastery);
        result.put("masteryLabel", getMasteryLabel(newMastery));
        result.put("isMastered", newMastery >= 0.95);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/batch-attempts")
    public ResponseEntity<Map<String, Object>> batchRecordAttempts(@RequestBody Map<String, Object> request) {
        String studentId = (String) request.get("studentId");
        String courseId = (String) request.get("courseId");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> attempts = (List<Map<String, Object>>) request.get("attempts");

        if (studentId == null || attempts == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "缺少必要参数");
            return ResponseEntity.badRequest().body(error);
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> attempt : attempts) {
            String knowledgeId = (String) attempt.get("knowledgeId");
            Boolean correct = (Boolean) attempt.get("correct");

            if (knowledgeId != null && correct != null) {
                double newMastery = bktService.recordAttempt(studentId, knowledgeId, courseId != null ? courseId : "", correct);

                Map<String, Object> r = new HashMap<>();
                r.put("knowledgeId", knowledgeId);
                r.put("correct", correct);
                r.put("newMasteryLevel", newMastery);
                r.put("masteryLabel", getMasteryLabel(newMastery));
                results.add(r);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("results", results);
        result.put("count", results.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/mastery/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentMastery(
            @PathVariable String studentId,
            @RequestParam(required = false) String courseId) {

        Map<String, Object> profile = bktService.getStudentMasteryProfile(studentId, courseId);
        profile.put("success", true);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/recommend/{studentId}")
    public ResponseEntity<Map<String, Object>> recommendLearningPath(
            @PathVariable String studentId,
            @RequestParam String courseId) {

        List<Map<String, Object>> recommendations = bktService.recommendLearningPath(studentId, courseId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("studentId", studentId);
        result.put("courseId", courseId);
        result.put("recommendations", recommendations);
        result.put("count", recommendations.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/class-overview/{courseId}")
    public ResponseEntity<Map<String, Object>> getClassMasteryOverview(@PathVariable String courseId) {
        Map<String, Object> overview = bktService.getClassMasteryOverview(courseId);
        overview.put("success", true);
        return ResponseEntity.ok(overview);
    }

    @PostMapping("/simulate")
    public ResponseEntity<Map<String, Object>> simulateBKT(@RequestBody Map<String, Object> request) {
        Double pL0 = (Double) request.getOrDefault("pL0", 0.3);
        Double pT = (Double) request.getOrDefault("pT", 0.1);
        Double pG = (Double) request.getOrDefault("pG", 0.2);
        Double pS = (Double) request.getOrDefault("pS", 0.1);
        @SuppressWarnings("unchecked")
        List<Boolean> responses = (List<Boolean>) request.get("responses");

        if (responses == null || responses.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "请提供答题记录 responses");
            return ResponseEntity.badRequest().body(error);
        }

        BKTModel model = new BKTModel(pL0, pT, pG, pS);
        List<Map<String, Object>> trajectory = new ArrayList<>();

        for (int i = 0; i < responses.size(); i++) {
            boolean correct = responses.get(i);
            double beforePL = model.getMasteryLevel();
            double afterPL = model.update(correct);

            Map<String, Object> step = new HashMap<>();
            step.put("step", i + 1);
            step.put("correct", correct);
            step.put("masteryBefore", Math.round(beforePL * 1000) / 1000.0);
            step.put("masteryAfter", Math.round(afterPL * 1000) / 1000.0);
            trajectory.add(step);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("finalMastery", model.getMasteryLevel());
        result.put("masteryLabel", getMasteryLabel(model.getMasteryLevel()));
        result.put("isMastered", model.isMastered(0.95));
        result.put("estimatedProblemsToMastery", model.estimateProblemsToMastery(0.95));
        result.put("trajectory", trajectory);
        return ResponseEntity.ok(result);
    }

    private String getMasteryLabel(double pL) {
        if (pL >= 0.95) return "精通";
        if (pL >= 0.85) return "熟练";
        if (pL >= 0.7) return "掌握";
        if (pL >= 0.5) return "学习中";
        if (pL >= 0.3) return "初步了解";
        return "未掌握";
    }
}
