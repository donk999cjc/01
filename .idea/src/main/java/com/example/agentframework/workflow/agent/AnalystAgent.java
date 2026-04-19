package com.example.agentframework.workflow.agent;

import com.example.agentframework.agent.*;
import com.example.agentframework.service.AIService;
import com.example.agentframework.service.StudentService;
import com.example.agentframework.service.SubmissionService;
import com.example.agentframework.entity.Student;
import com.example.agentframework.entity.Submission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class AnalystAgent implements Agent {

    @Autowired
    private AIService aiService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private SubmissionService submissionService;

    private AgentContext context;

    @Override
    public String getId() {
        return "analyst-agent";
    }

    @Override
    public String getName() {
        return "学情分析Agent";
    }

    @Override
    public String getType() {
        return "analyst";
    }

    @Override
    public void initialize(AgentContext context) {
        this.context = context;
    }

    @Override
    public AgentResponse process(AgentRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            String action = request.getAction();

            Map<String, Object> result;
            switch (action != null ? action : "analyze") {
                case "analyze":
                    result = analyzeStudent(request);
                    break;
                case "class_analyze":
                    result = analyzeClass(request);
                    break;
                case "predict":
                    result = predictPerformance(request);
                    break;
                case "recommend":
                    String recommendation = recommendLearningPath(request);
                    AgentResponse recResponse = AgentResponse.success(recommendation);
                    recResponse.setAgentId(getId());
                    recResponse.setProcessingTime(System.currentTimeMillis() - startTime);
                    return recResponse;
                default:
                    result = analyzeStudent(request);
            }

            AgentResponse response = AgentResponse.success("分析完成", result);
            response.setAgentId(getId());
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            return response;

        } catch (Exception e) {
            AgentResponse response = AgentResponse.failure("分析Agent处理失败: " + e.getMessage());
            response.setAgentId(getId());
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            return response;
        }
    }

    private Map<String, Object> analyzeStudent(AgentRequest request) {
        String studentId = request.getParam("studentId") != null ?
                request.getParam("studentId").toString() : "";
        Map<String, Object> result = new HashMap<>();

        if (studentId.isEmpty()) {
            result.put("success", false);
            result.put("message", "未提供学生ID");
            return result;
        }

        List<Submission> submissions = submissionService.getSubmissionsByStudentId(studentId);

        List<Submission> graded = submissions.stream()
                .filter(s -> s.getScore() != null)
                .collect(Collectors.toList());

        if (graded.isEmpty()) {
            result.put("success", true);
            result.put("studentId", studentId);
            result.put("message", "暂无批改数据");
            return result;
        }

        double avgScore = graded.stream().mapToDouble(Submission::getScore).average().orElse(0);
        double maxScore = graded.stream().mapToDouble(Submission::getScore).max().orElse(0);
        double minScore = graded.stream().mapToDouble(Submission::getScore).min().orElse(0);

        double stdDev = 0;
        if (graded.size() > 1) {
            double finalAvg = avgScore;
            stdDev = Math.sqrt(graded.stream()
                    .mapToDouble(s -> Math.pow(s.getScore() - finalAvg, 2))
                    .average().orElse(0));
        }

        List<Submission> recent = graded.stream()
                .sorted((a, b) -> Long.compare(
                        b.getSubmittedAt() != null ? b.getSubmittedAt().getTime() : 0,
                        a.getSubmittedAt() != null ? a.getSubmittedAt().getTime() : 0))
                .limit(5)
                .collect(Collectors.toList());

        double recentAvg = recent.stream().mapToDouble(Submission::getScore).average().orElse(0);

        String trend = "stable";
        if (graded.size() >= 4) {
            int mid = graded.size() / 2;
            double firstHalfAvg = graded.subList(0, mid).stream()
                    .mapToDouble(Submission::getScore).average().orElse(0);
            double secondHalfAvg = graded.subList(mid, graded.size()).stream()
                    .mapToDouble(Submission::getScore).average().orElse(0);
            if (secondHalfAvg > firstHalfAvg + 5) trend = "improving";
            else if (secondHalfAvg < firstHalfAvg - 5) trend = "declining";
        }

        result.put("success", true);
        result.put("studentId", studentId);
        result.put("totalSubmissions", graded.size());
        result.put("averageScore", Math.round(avgScore * 100) / 100.0);
        result.put("maxScore", maxScore);
        result.put("minScore", minScore);
        result.put("standardDeviation", Math.round(stdDev * 100) / 100.0);
        result.put("recentAverageScore", Math.round(recentAvg * 100) / 100.0);
        result.put("trend", trend);
        result.put("weaknesses", identifyWeaknesses(graded));

        return result;
    }

    private List<Map<String, Object>> identifyWeaknesses(List<Submission> submissions) {
        return submissions.stream()
                .filter(s -> s.getScore() < 60)
                .map(s -> {
                    Map<String, Object> weakness = new HashMap<>();
                    weakness.put("assignmentId", s.getAssignmentId());
                    weakness.put("score", s.getScore());
                    weakness.put("feedback", s.getFeedback());
                    return weakness;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> analyzeClass(AgentRequest request) {
        String courseId = request.getParam("courseId") != null ?
                request.getParam("courseId").toString() : "";
        Map<String, Object> result = new HashMap<>();

        List<Student> students = studentService.getStudentsByCourseId(courseId);

        List<Map<String, Object>> studentAnalyses = new ArrayList<>();
        double classAvg = 0;
        int count = 0;

        for (Student student : students) {
            List<Submission> subs = submissionService.getSubmissionsByStudentId(student.getStudentId());
            List<Submission> graded = subs.stream().filter(s -> s.getScore() != null).collect(Collectors.toList());

            if (!graded.isEmpty()) {
                double avg = graded.stream().mapToDouble(Submission::getScore).average().orElse(0);
                classAvg += avg;
                count++;

                Map<String, Object> sa = new HashMap<>();
                sa.put("studentId", student.getStudentId());
                sa.put("name", student.getRealName());
                sa.put("averageScore", avg);
                sa.put("submissionCount", graded.size());
                studentAnalyses.add(sa);
            }
        }

        result.put("success", true);
        result.put("courseId", courseId);
        result.put("totalStudents", students.size());
        result.put("analyzedStudents", count);
        result.put("classAverageScore", count > 0 ? Math.round((classAvg / count) * 100) / 100.0 : 0);
        result.put("studentAnalyses", studentAnalyses);

        return result;
    }

    private Map<String, Object> predictPerformance(AgentRequest request) {
        String studentId = request.getParam("studentId") != null ?
                request.getParam("studentId").toString() : "";

        Map<String, Object> result = new HashMap<>();
        List<Submission> submissions = submissionService.getSubmissionsByStudentId(studentId);
        List<Submission> graded = submissions.stream()
                .filter(s -> s.getScore() != null)
                .sorted(Comparator.comparing(Submission::getSubmittedAt))
                .collect(Collectors.toList());

        if (graded.size() < 3) {
            result.put("success", false);
            result.put("message", "数据不足，至少需要3次提交记录");
            return result;
        }

        double recentAvg = graded.stream()
                .skip(Math.max(0, graded.size() - 3))
                .mapToDouble(Submission::getScore)
                .average().orElse(0);

        double overallAvg = graded.stream().mapToDouble(Submission::getScore).average().orElse(0);

        double predicted = recentAvg * 0.7 + overallAvg * 0.3;
        predicted = Math.max(0, Math.min(100, predicted));

        String level;
        if (predicted >= 90) level = "优秀";
        else if (predicted >= 80) level = "良好";
        else if (predicted >= 70) level = "中等";
        else if (predicted >= 60) level = "及格";
        else level = "需要帮助";

        result.put("success", true);
        result.put("studentId", studentId);
        result.put("predictedScore", Math.round(predicted * 100) / 100.0);
        result.put("level", level);
        result.put("confidence", graded.size() >= 5 ? "high" : "medium");

        return result;
    }

    private String recommendLearningPath(AgentRequest request) {
        String studentId = request.getParam("studentId") != null ?
                request.getParam("studentId").toString() : "";
        String courseId = request.getCourseId();

        List<Submission> submissions = submissionService.getSubmissionsByStudentId(studentId);
        List<Submission> graded = submissions.stream()
                .filter(s -> s.getScore() != null)
                .collect(Collectors.toList());

        double avgScore = graded.stream().mapToDouble(Submission::getScore).average().orElse(0);
        long weakCount = graded.stream().filter(s -> s.getScore() < 60).count();

        StringBuilder analysisInfo = new StringBuilder();
        analysisInfo.append("学生ID: ").append(studentId).append("\n");
        analysisInfo.append("课程ID: ").append(courseId != null ? courseId : "未知").append("\n");
        analysisInfo.append("平均分: ").append(Math.round(avgScore)).append("\n");
        analysisInfo.append("低分作业数: ").append(weakCount).append("\n");
        analysisInfo.append("总提交数: ").append(graded.size());

        String systemPrompt = "你是一个专业的学习路径规划师。根据学生的学情数据，" +
                "生成个性化的学习路径推荐。\n" +
                "要求：\n" +
                "1. 分析学生的薄弱环节\n" +
                "2. 推荐学习顺序和重点\n" +
                "3. 给出具体的学习资源建议\n" +
                "4. 设定阶段性学习目标\n" +
                "5. 提供学习策略建议\n" +
                "使用中文回复，格式清晰。";

        return aiService.chat(systemPrompt, analysisInfo.toString());
    }

    @Override
    public void learn(Object data) {
        if (context != null && data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> learningData = (Map<String, Object>) data;
            for (Map.Entry<String, Object> entry : learningData.entrySet()) {
                context.remember(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void reset() {
        if (context != null) {
            context.getMemory().clear();
        }
    }
}
