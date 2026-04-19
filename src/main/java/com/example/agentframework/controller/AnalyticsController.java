package com.example.agentframework.controller;

import com.example.agentframework.service.impl.AnalyticsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin
public class AnalyticsController {

    @Autowired
    private AnalyticsServiceImpl analyticsService;

    /**
     * 分析学生学情
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<Map<String, Object>> analyzeStudentPerformance(@PathVariable String studentId) {
        Map<String, Object> result = analyticsService.analyzeStudentPerformance(studentId);
        if ((Boolean) result.get("success")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }
    /**
     * 获取班级作业统计图表数据（真实数据！）
     */
    @GetMapping("/class/chart-data/{courseId}")
    public ResponseEntity<Map<String, Object>> getClassChartData(@PathVariable String courseId) {
        Map<String, Object> classData = analyticsService.analyzeClassPerformance(courseId);

        // 从班级分析结果里取出图表需要的数据
        List<String> homeworkNames = (List<String>) classData.get("homeworkNames");
        List<Double> avgScores = (List<Double>) classData.get("avgScores");
        List<Double> avgCorrectRates = (List<Double>) classData.get("avgCorrectRates");

        Map<String, Object> result = new HashMap<>();
        result.put("homeworkNames", homeworkNames);
        result.put("avgScores", avgScores);
        result.put("avgCorrectRates", avgCorrectRates);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * 生成增量练习
     */
    @PostMapping("/practice/generate")
    public ResponseEntity<List<Map<String, Object>>> generatePractice(
            @RequestParam String studentId,
            @RequestParam String courseId,
            @RequestParam(defaultValue = "5") int count) {
        List<Map<String, Object>> practices = analyticsService.generatePractice(studentId, courseId, count);
        return new ResponseEntity<>(practices, HttpStatus.OK);
    }

    /**
     * 分析班级学情
     */
    @GetMapping("/class/{courseId}")
    public ResponseEntity<Map<String, Object>> analyzeClassPerformance(@PathVariable String courseId) {
        Map<String, Object> result = analyticsService.analyzeClassPerformance(courseId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * 获取学情预警
     */
    @GetMapping("/alerts/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentAlerts(@PathVariable String studentId) {
        Map<String, Object> analysis = analyticsService.analyzeStudentPerformance(studentId);
        if ((Boolean) analysis.get("success")) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("alerts", analysis.get("alerts"));
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(analysis, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 获取班级预警
     */
    @GetMapping("/alerts/class/{courseId}")
    public ResponseEntity<Map<String, Object>> getClassAlerts(@PathVariable String courseId) {
        Map<String, Object> analysis = analyticsService.analyzeClassPerformance(courseId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("classAlerts", analysis.get("classAlerts"));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
