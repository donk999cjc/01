package com.example.agentframework.bkt;

import com.example.agentframework.entity.StudentMastery;
import com.example.agentframework.mapper.StudentMasteryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BKTService {

    @Autowired
    private StudentMasteryMapper studentMasteryMapper;

    @Value("${bkt.pl0:0.3}")
    private double defaultPL0;

    @Value("${bkt.pt:0.1}")
    private double defaultPT;

    @Value("${bkt.pg:0.2}")
    private double defaultPG;

    @Value("${bkt.ps:0.1}")
    private double defaultPS;

    private static final double MASTERY_THRESHOLD = 0.95;

    public BKTModel getOrCreateModel(String studentId, String knowledgeId, String courseId) {
        StudentMastery mastery = studentMasteryMapper.findByStudentAndKnowledge(studentId, knowledgeId);

        if (mastery != null) {
            BKTModel model = new BKTModel(
                    mastery.getPL0() != null ? mastery.getPL0() : defaultPL0,
                    mastery.getPT() != null ? mastery.getPT() : defaultPT,
                    mastery.getPG() != null ? mastery.getPG() : defaultPG,
                    mastery.getPS() != null ? mastery.getPS() : defaultPS
            );
            model.setPL(mastery.getPL() != null ? mastery.getPL() : defaultPL0);
            return model;
        }

        return new BKTModel(defaultPL0, defaultPT, defaultPG, defaultPS);
    }

    public double recordAttempt(String studentId, String knowledgeId, String courseId, boolean correct) {
        BKTModel model = getOrCreateModel(studentId, knowledgeId, courseId);
        double newPL = model.update(correct);

        StudentMastery mastery = studentMasteryMapper.findByStudentAndKnowledge(studentId, knowledgeId);

        if (mastery == null) {
            mastery = new StudentMastery();
            mastery.setStudentId(studentId);
            mastery.setKnowledgeId(knowledgeId);
            mastery.setCourseId(courseId);
            mastery.setPL0(model.getPL0());
            mastery.setPT(model.getPT());
            mastery.setPG(model.getPG());
            mastery.setPS(model.getPS());
            mastery.setTotalAttempts(1);
            mastery.setCorrectAttempts(correct ? 1 : 0);
            mastery.setLastAttemptAt(new Date());
            mastery.setPL(newPL);
            studentMasteryMapper.insert(mastery);
        } else {
            mastery.setPL(newPL);
            mastery.setTotalAttempts(mastery.getTotalAttempts() != null ? mastery.getTotalAttempts() + 1 : 1);
            mastery.setCorrectAttempts(mastery.getCorrectAttempts() != null ?
                    mastery.getCorrectAttempts() + (correct ? 1 : 0) : (correct ? 1 : 0));
            mastery.setLastAttemptAt(new Date());
            studentMasteryMapper.update(mastery);
        }

        return newPL;
    }

    public Map<String, Object> getStudentMasteryProfile(String studentId, String courseId) {
        List<StudentMastery> masteries;
        if (courseId != null && !courseId.isEmpty()) {
            masteries = studentMasteryMapper.findByStudentAndCourse(studentId, courseId);
        } else {
            masteries = studentMasteryMapper.findByStudentId(studentId);
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("studentId", studentId);
        profile.put("courseId", courseId);
        profile.put("totalKnowledgePoints", masteries.size());

        long masteredCount = masteries.stream()
                .filter(m -> m.getPL() != null && m.getPL() >= MASTERY_THRESHOLD)
                .count();
        profile.put("masteredCount", masteredCount);

        long learningCount = masteries.stream()
                .filter(m -> m.getPL() != null && m.getPL() >= 0.5 && m.getPL() < MASTERY_THRESHOLD)
                .count();
        profile.put("learningCount", learningCount);

        long notLearnedCount = masteries.stream()
                .filter(m -> m.getPL() != null && m.getPL() < 0.5)
                .count();
        profile.put("notLearnedCount", notLearnedCount);

        double avgMastery = masteries.stream()
                .filter(m -> m.getPL() != null)
                .mapToDouble(StudentMastery::getPL)
                .average()
                .orElse(0);
        profile.put("averageMastery", Math.round(avgMastery * 1000) / 1000.0);

        List<Map<String, Object>> knowledgeMastery = masteries.stream()
                .map(m -> {
                    Map<String, Object> km = new HashMap<>();
                    km.put("knowledgeId", m.getKnowledgeId());
                    km.put("masteryLevel", m.getPL());
                    km.put("totalAttempts", m.getTotalAttempts());
                    km.put("correctAttempts", m.getCorrectAttempts());
                    km.put("accuracy", m.getTotalAttempts() != null && m.getTotalAttempts() > 0 ?
                            Math.round((m.getCorrectAttempts() != null ? m.getCorrectAttempts() : 0) * 1000.0 / m.getTotalAttempts()) / 10.0 : 0);
                    km.put("masteryLabel", getMasteryLabel(m.getPL()));
                    km.put("isMastered", m.getPL() != null && m.getPL() >= MASTERY_THRESHOLD);
                    return km;
                })
                .sorted((a, b) -> Double.compare(
                        (Double) b.get("masteryLevel"),
                        (Double) a.get("masteryLevel")))
                .collect(Collectors.toList());

        profile.put("knowledgeMastery", knowledgeMastery);

        List<Map<String, Object>> weakPoints = knowledgeMastery.stream()
                .filter(km -> (Double) km.get("masteryLevel") < 0.7)
                .collect(Collectors.toList());
        profile.put("weakPoints", weakPoints);

        return profile;
    }

    public List<Map<String, Object>> recommendLearningPath(String studentId, String courseId) {
        List<StudentMastery> masteries = studentMasteryMapper.findByStudentAndCourse(studentId, courseId);

        return masteries.stream()
                .filter(m -> m.getPL() != null && m.getPL() < MASTERY_THRESHOLD)
                .sorted(Comparator.comparingDouble(StudentMastery::getPL))
                .map(m -> {
                    Map<String, Object> rec = new HashMap<>();
                    rec.put("knowledgeId", m.getKnowledgeId());
                    rec.put("currentMastery", m.getPL());
                    rec.put("masteryLabel", getMasteryLabel(m.getPL()));

                    BKTModel model = new BKTModel(
                            m.getPL0() != null ? m.getPL0() : defaultPL0,
                            m.getPT() != null ? m.getPT() : defaultPT,
                            m.getPG() != null ? m.getPG() : defaultPG,
                            m.getPS() != null ? m.getPS() : defaultPS
                    );
                    model.setPL(m.getPL());

                    rec.put("estimatedProblemsToMastery", model.estimateProblemsToMastery(MASTERY_THRESHOLD));
                    rec.put("priority", m.getPL() < 0.3 ? "high" : m.getPL() < 0.7 ? "medium" : "low");
                    return rec;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> getClassMasteryOverview(String courseId) {
        List<StudentMastery> allMasteries = studentMasteryMapper.findByCourseId(courseId);

        Map<String, Object> overview = new HashMap<>();
        overview.put("courseId", courseId);
        overview.put("totalRecords", allMasteries.size());

        Map<String, List<StudentMastery>> byKnowledge = allMasteries.stream()
                .collect(Collectors.groupingBy(StudentMastery::getKnowledgeId));

        List<Map<String, Object>> knowledgeOverview = byKnowledge.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> ko = new HashMap<>();
                    ko.put("knowledgeId", entry.getKey());
                    ko.put("studentCount", entry.getValue().size());

                    double avgMastery = entry.getValue().stream()
                            .filter(m -> m.getPL() != null)
                            .mapToDouble(StudentMastery::getPL)
                            .average()
                            .orElse(0);
                    ko.put("averageMastery", Math.round(avgMastery * 1000) / 1000.0);

                    long masteredStudents = entry.getValue().stream()
                            .filter(m -> m.getPL() != null && m.getPL() >= MASTERY_THRESHOLD)
                            .count();
                    ko.put("masteredStudents", masteredStudents);
                    ko.put("masteryRate", entry.getValue().size() > 0 ?
                            Math.round(masteredStudents * 1000.0 / entry.getValue().size()) / 10.0 : 0);

                    return ko;
                })
                .sorted((a, b) -> Double.compare((Double) a.get("averageMastery"), (Double) b.get("averageMastery")))
                .collect(Collectors.toList());

        overview.put("knowledgeOverview", knowledgeOverview);

        return overview;
    }

    private String getMasteryLabel(Double pL) {
        if (pL == null) return "未评估";
        if (pL >= 0.95) return "精通";
        if (pL >= 0.85) return "熟练";
        if (pL >= 0.7) return "掌握";
        if (pL >= 0.5) return "学习中";
        if (pL >= 0.3) return "初步了解";
        return "未掌握";
    }
}
