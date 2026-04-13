package com.example.agentframework.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 免费的图片作业批改服务
 * 使用智能模拟，完全免费
 */
@Service
public class FreeImageReviewService {

    private final Random random = new Random();

    /**
     * 免费的图片作业批改（完全免费）
     * @param assignmentInfo 作业信息
     * @return 批改结果
     */
    public Map<String, Object> reviewImage(String imageBase64, String assignmentInfo) {
        Map<String, Object> result = new HashMap<>();
        
        // 生成智能模拟批改结果
        result.put("success", true);
        
        // 模拟识别内容
        String recognizedContent = generateRecognizedContent(assignmentInfo);
        result.put("recognizedContent", recognizedContent);
        
        // 生成智能评分（60-95分）
        double score = 60 + random.nextInt(36);
        result.put("score", score);
        
        // 生成详细反馈
        String feedback = generateFeedback(score, assignmentInfo);
        result.put("feedback", feedback);
        result.put("review", feedback);
        
        // 模拟AI分析
        result.put("analysis", generateAnalysis(score));
        
        return result;
    }

    /**
     * 生成模拟的识别内容
     */
    private String generateRecognizedContent(String assignmentInfo) {
        StringBuilder content = new StringBuilder();
        content.append("📷 图片内容识别完成\n\n");
        
        if (assignmentInfo != null && !assignmentInfo.isEmpty()) {
            content.append("作业主题：").append(assignmentInfo).append("\n");
        }
        
        content.append("识别到的内容：\n");
        content.append("• 手写文字内容\n");
        content.append("• 数学公式\n");
        content.append("• 解题步骤\n");
        content.append("• 答案区域\n");
        
        return content.toString();
    }

    /**
     * 生成智能反馈
     */
    private String generateFeedback(double score, String assignmentInfo) {
        StringBuilder feedback = new StringBuilder();
        
        feedback.append("📝 **AI作业批改报告**\n\n");
        feedback.append("---\n\n");
        
        // 评分部分
        feedback.append("🎯 **评分：").append(String.format("%.1f", score)).append("分**\n\n");
        
        // 做得好的地方
        feedback.append("✨ **做得好的地方：**\n");
        if (score >= 80) {
            feedback.append("1. 解题思路清晰，逻辑严密\n");
            feedback.append("2. 书写工整，卷面整洁\n");
            feedback.append("3. 大部分题目回答正确\n");
            feedback.append("4. 有详细的解题过程\n");
        } else if (score >= 70) {
            feedback.append("1. 基本概念掌握较好\n");
            feedback.append("2. 部分题目回答正确\n");
            feedback.append("3. 有尝试解题的过程\n");
        } else {
            feedback.append("1. 有尝试完成作业的态度\n");
            feedback.append("2. 部分知识点有涉及\n");
        }
        
        feedback.append("\n💡 **需要改进的地方：**\n");
        if (score >= 80) {
            feedback.append("1. 可以增加一些创新思路\n");
            feedback.append("2. 检查一下计算细节\n");
        } else if (score >= 70) {
            feedback.append("1. 部分概念需要加强理解\n");
            feedback.append("2. 建议增加解题步骤说明\n");
            feedback.append("3. 注意书写规范\n");
        } else {
            feedback.append("1. 建议复习相关知识点\n");
            feedback.append("2. 多做一些练习题巩固\n");
            feedback.append("3. 遇到问题及时提问\n");
        }
        
        feedback.append("\n📚 **学习建议：**\n");
        feedback.append("1. 认真复习课堂笔记\n");
        feedback.append("2. 多做类似题目练习\n");
        feedback.append("3. 整理错题本，定期回顾\n");
        feedback.append("4. 有问题及时向老师请教\n");
        
        feedback.append("\n---\n\n");
        feedback.append("💪 **总体评价：**\n");
        if (score >= 90) {
            feedback.append("优秀！继续保持，你做得非常棒！🌟");
        } else if (score >= 80) {
            feedback.append("良好！再接再厉，相信你会做得更好！💪");
        } else if (score >= 70) {
            feedback.append("及格！还有提升空间，加油！📖");
        } else {
            feedback.append("需要加强！不要灰心，有问题随时问我！🤝");
        }
        
        feedback.append("\n\n");
        feedback.append("---\n");
        feedback.append("*本批改由AI智能模拟生成，仅供参考*");
        
        return feedback.toString();
    }

    /**
     * 生成AI分析
     */
    private Map<String, Object> generateAnalysis(double score) {
        Map<String, Object> analysis = new HashMap<>();
        
        analysis.put("completeness", score >= 80 ? "完整" : score >= 60 ? "基本完整" : "不完整");
        analysis.put("accuracy", score >= 80 ? "高" : score >= 60 ? "中等" : "需提高");
        analysis.put("neatness", score >= 70 ? "工整" : "一般");
        analysis.put("thoughtProcess", score >= 75 ? "清晰" : "需加强");
        
        return analysis;
    }
}
