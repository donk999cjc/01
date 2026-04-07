package com.example.agentframework.engine.impl;

import com.example.agentframework.engine.IntelligentEngine;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class IntelligentEngineImpl implements IntelligentEngine {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "的", "是", "在", "了", "和", "与", "或", "有", "被", "这", "那", "个", "们",
            "也", "就", "都", "而", "及", "着", "对", "把", "让", "给", "向", "从", "到"
    ));

    @Override
    public Map<String, Object> analyze(String content, String type) {
        Map<String, Object> result = new HashMap<>();
        
        result.put("length", content.length());
        result.put("wordCount", countWords(content));
        result.put("sentenceCount", countSentences(content));
        result.put("paragraphCount", countParagraphs(content));
        
        List<Map<String, Object>> keywords = extractKeywords(content);
        result.put("keywords", keywords);
        result.put("readabilityScore", calculateReadability(content));
        result.put("complexity", analyzeComplexity(content));
        
        if ("code".equals(type)) {
            result.put("codeMetrics", analyzeCode(content));
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> extractKeywords(String content) {
        Map<String, Integer> wordFreq = new HashMap<>();
        
        String[] words = content.split("[\\s\\p{Punct}]+");
        for (String word : words) {
            if (word.length() > 1 && !STOP_WORDS.contains(word)) {
                wordFreq.merge(word, 1, Integer::sum);
            }
        }
        
        return wordFreq.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .map(e -> {
                    Map<String, Object> keyword = new HashMap<>();
                    keyword.put("word", e.getKey());
                    keyword.put("frequency", e.getValue());
                    keyword.put("weight", calculateWordWeight(e.getKey(), e.getValue(), content.length()));
                    return keyword;
                })
                .collect(Collectors.toList());
    }

    @Override
    public double calculateSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) return 0;
        
        Set<String> words1 = Arrays.stream(text1.toLowerCase().split("[\\s\\p{Punct}]+"))
                .filter(w -> w.length() > 1)
                .collect(Collectors.toSet());
        
        Set<String> words2 = Arrays.stream(text2.toLowerCase().split("[\\s\\p{Punct}]+"))
                .filter(w -> w.length() > 1)
                .collect(Collectors.toSet());
        
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
    }

    @Override
    public String generateSummary(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        
        String[] sentences = content.split("[。！？.!?]");
        if (sentences.length == 0) return content.substring(0, maxLength);
        
        StringBuilder summary = new StringBuilder();
        for (String sentence : sentences) {
            if (summary.length() + sentence.length() + 1 <= maxLength) {
                if (summary.length() > 0) summary.append("。");
                summary.append(sentence.trim());
            } else {
                break;
            }
        }
        
        return summary.toString();
    }

    @Override
    public List<String> suggestCorrections(String content, String type) {
        List<String> corrections = new ArrayList<>();
        
        Pattern doubleSpace = Pattern.compile("\\s{2,}");
        Matcher matcher = doubleSpace.matcher(content);
        if (matcher.find()) {
            corrections.add("建议删除多余空格");
        }
        
        Pattern doublePunctuation = Pattern.compile("[，。！？、；：]{2,}");
        matcher = doublePunctuation.matcher(content);
        if (matcher.find()) {
            corrections.add("标点符号重复，请检查");
        }
        
        if ("code".equals(type)) {
            corrections.addAll(analyzeCodeIssues(content));
        }
        
        return corrections;
    }

    @Override
    public Map<String, Object> evaluateAnswer(String studentAnswer, String standardAnswer) {
        Map<String, Object> evaluation = new HashMap<>();
        
        double similarity = calculateSimilarity(studentAnswer, standardAnswer);
        evaluation.put("similarity", similarity);
        
        double score = similarity * 100;
        
        List<String> keywords = extractKeywords(standardAnswer).stream()
                .map(k -> (String) k.get("word"))
                .collect(Collectors.toList());
        
        int matchedKeywords = 0;
        for (String keyword : keywords) {
            if (studentAnswer.contains(keyword)) {
                matchedKeywords++;
            }
        }
        
        double keywordScore = keywords.isEmpty() ? 0 : (double) matchedKeywords / keywords.size() * 100;
        evaluation.put("keywordMatch", matchedKeywords);
        evaluation.put("keywordTotal", keywords.size());
        evaluation.put("keywordScore", keywordScore);
        
        double finalScore = similarity * 0.6 + keywordScore * 0.4;
        evaluation.put("score", finalScore);
        evaluation.put("grade", getGrade(finalScore));
        
        return evaluation;
    }

    @Override
    public List<Map<String, Object>> generateQuestions(String content, int count) {
        List<Map<String, Object>> questions = new ArrayList<>();
        
        List<Map<String, Object>> keywords = extractKeywords(content);
        
        for (int i = 0; i < Math.min(count, keywords.size()); i++) {
            Map<String, Object> question = new HashMap<>();
            String keyword = (String) keywords.get(i).get("word");
            
            question.put("type", "definition");
            question.put("question", "请解释\"" + keyword + "\"的含义");
            question.put("keyword", keyword);
            question.put("difficulty", 2);
            
            questions.add(question);
        }
        
        String[] sentences = content.split("[。！？.!?]");
        for (int i = questions.size(); i < count && i < sentences.length; i++) {
            if (sentences[i].trim().length() > 10) {
                Map<String, Object> question = new HashMap<>();
                question.put("type", "comprehension");
                question.put("question", "关于以下内容，请说明其核心观点：\n" + sentences[i].trim());
                question.put("difficulty", 3);
                questions.add(question);
            }
        }
        
        return questions;
    }

    @Override
    public String generateFeedback(Map<String, Object> analysisResult) {
        StringBuilder feedback = new StringBuilder();
        
        Double score = (Double) analysisResult.get("score");
        if (score != null) {
            if (score >= 90) {
                feedback.append("优秀！答案准确完整，理解深刻。");
            } else if (score >= 80) {
                feedback.append("良好！答案基本正确，可以进一步完善细节。");
            } else if (score >= 60) {
                feedback.append("及格。答案部分正确，建议加强对核心概念的理解。");
            } else {
                feedback.append("需要改进。请重新学习相关知识点，加深理解。");
            }
        }
        
        Integer keywordMatch = (Integer) analysisResult.get("keywordMatch");
        Integer keywordTotal = (Integer) analysisResult.get("keywordTotal");
        if (keywordMatch != null && keywordTotal != null && keywordMatch < keywordTotal) {
            feedback.append("\n建议补充以下关键点。");
        }
        
        return feedback.toString();
    }

    @Override
    public double predictScore(Map<String, Object> features) {
        double score = 50;
        
        if (features.containsKey("previousScore")) {
            score = score * 0.3 + ((Number) features.get("previousScore")).doubleValue() * 0.7;
        }
        
        if (features.containsKey("completionRate")) {
            double rate = ((Number) features.get("completionRate")).doubleValue();
            score = score * (1 - rate * 0.2) + rate * 100 * 0.2;
        }
        
        if (features.containsKey("studyTime")) {
            int studyTime = ((Number) features.get("studyTime")).intValue();
            if (studyTime > 300) {
                score += 5;
            } else if (studyTime < 60) {
                score -= 5;
            }
        }
        
        return Math.max(0, Math.min(100, score));
    }

    @Override
    public List<String> recommendResources(String userId, String courseId) {
        List<String> resources = new ArrayList<>();
        
        resources.add("基础知识复习材料");
        resources.add("重点难点讲解视频");
        resources.add("练习题库");
        resources.add("历年真题");
        
        return resources;
    }

    private int countWords(String content) {
        return content.split("[\\s\\p{Punct}]+").length;
    }

    private int countSentences(String content) {
        return content.split("[。！？.!?]").length;
    }

    private int countParagraphs(String content) {
        return content.split("\\n\\s*\\n").length;
    }

    private double calculateReadability(String content) {
        int words = countWords(content);
        int sentences = countSentences(content);
        
        if (sentences == 0) return 0;
        
        double avgWordsPerSentence = (double) words / sentences;
        
        if (avgWordsPerSentence < 15) return 90;
        if (avgWordsPerSentence < 20) return 80;
        if (avgWordsPerSentence < 25) return 70;
        if (avgWordsPerSentence < 30) return 60;
        return 50;
    }

    private String analyzeComplexity(String content) {
        int avgWordLength = content.length() / Math.max(1, countWords(content));
        
        if (avgWordLength < 3) return "简单";
        if (avgWordLength < 5) return "中等";
        return "复杂";
    }

    private Map<String, Object> analyzeCode(String content) {
        Map<String, Object> metrics = new HashMap<>();
        
        String[] lines = content.split("\n");
        metrics.put("linesOfCode", lines.length);
        metrics.put("commentLines", countCommentLines(content));
        metrics.put("codeLines", lines.length - countCommentLines(content) - countBlankLines(content));
        
        return metrics;
    }

    private int countCommentLines(String code) {
        int count = 0;
        for (String line : code.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("//") || trimmed.startsWith("#") || 
                trimmed.startsWith("/*") || trimmed.startsWith("*")) {
                count++;
            }
        }
        return count;
    }

    private int countBlankLines(String code) {
        int count = 0;
        for (String line : code.split("\n")) {
            if (line.trim().isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private double calculateWordWeight(String word, int frequency, int totalLength) {
        double tf = (double) frequency / totalLength;
        double lengthBonus = word.length() / 10.0;
        return tf + lengthBonus * 0.1;
    }

    private List<String> analyzeCodeIssues(String code) {
        List<String> issues = new ArrayList<>();
        
        if (code.contains("System.out.println")) {
            issues.add("建议使用日志框架替代System.out.println");
        }
        
        if (code.contains("TODO") || code.contains("FIXME")) {
            issues.add("存在未完成的TODO或FIXME标记");
        }
        
        return issues;
    }

    private String getGrade(double score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }
}
