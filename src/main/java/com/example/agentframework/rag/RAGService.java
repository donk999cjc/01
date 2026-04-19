package com.example.agentframework.rag;

import com.example.agentframework.knowledge.KnowledgeMiddleware;
import com.example.agentframework.knowledge.KnowledgeNode;
import com.example.agentframework.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RAGService {

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private VectorStoreService vectorStoreService;

    @Autowired
    private KnowledgeMiddleware knowledgeMiddleware;

    @Autowired
    private AIService aiService;

    @Value("${rag.chunk.size:500}")
    private int chunkSize;

    @Value("${rag.chunk.overlap:100}")
    private int chunkOverlap;

    @Value("${rag.max.context.length:3000}")
    private int maxContextLength;

    @Value("${rag.relevance.threshold:0.6}")
    private double relevanceThreshold;

    @Value("${milvus.topk:5}")
    private int topK;

    public void indexKnowledgeBase(String courseId) {
        List<KnowledgeNode> nodes = knowledgeMiddleware.getKnowledgeByCourse(courseId);
        for (KnowledgeNode node : nodes) {
            indexKnowledgeNode(node);
        }
        System.out.println("Indexed " + nodes.size() + " knowledge nodes for course: " + courseId);
    }

    public void indexKnowledgeNode(KnowledgeNode node) {
        List<String> chunks = chunkText(node.getContent());
        for (int i = 0; i < chunks.size(); i++) {
            String chunkId = node.getId() + "_chunk_" + i;
            String chunkText = chunks.get(i);

            List<Float> embedding = embeddingService.embed(chunkText);
            vectorStoreService.storeVector(
                    chunkId,
                    embedding,
                    chunkText,
                    node.getCourseId(),
                    node.getId()
            );
        }
    }

    public void removeFromIndex(String knowledgeId) {
        int chunkIndex = 0;
        while (vectorStoreService.deleteVector(knowledgeId + "_chunk_" + chunkIndex)) {
            chunkIndex++;
        }
    }

    public String queryWithRAG(String query, String courseId) {
        List<Map<String, Object>> relevantDocs = retrieve(query, courseId);
        return generate(query, relevantDocs);
    }

    public Map<String, Object> queryWithRAGDetailed(String query, String courseId) {
        List<Map<String, Object>> relevantDocs = retrieve(query, courseId);

        Map<String, Object> result = new HashMap<>();
        result.put("query", query);
        result.put("relevantDocuments", relevantDocs);
        result.put("documentCount", relevantDocs.size());

        String answer = generate(query, relevantDocs);
        result.put("answer", answer);
        result.put("vectorStoreAvailable", vectorStoreService.isAvailable());

        return result;
    }

    public List<Map<String, Object>> retrieve(String query, String courseId) {
        List<Float> queryEmbedding = embeddingService.embed(query);

        List<Map<String, Object>> searchResults = vectorStoreService.search(queryEmbedding, courseId, topK);

        return searchResults.stream()
                .filter(result -> {
                    Object score = result.get("score");
                    if (score instanceof Double) {
                        return (Double) score >= relevanceThreshold;
                    } else if (score instanceof Number) {
                        return ((Number) score).doubleValue() >= relevanceThreshold;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private String generate(String query, List<Map<String, Object>> relevantDocs) {
        StringBuilder contextBuilder = new StringBuilder();
        int currentLength = 0;

        for (Map<String, Object> doc : relevantDocs) {
            String content = (String) doc.get("content");
            if (content == null) continue;

            if (currentLength + content.length() > maxContextLength) {
                int remaining = maxContextLength - currentLength;
                if (remaining > 100) {
                    contextBuilder.append(content, 0, remaining).append("...\n\n");
                }
                break;
            }

            contextBuilder.append(content).append("\n\n");
            currentLength += content.length();
        }

        String context = contextBuilder.toString().trim();

        if (context.isEmpty()) {
            return aiService.chat(buildRAGSystemPrompt(false), query);
        }

        String ragQuery = "参考知识库内容：\n\n" + context + "\n\n---\n\n基于以上知识库内容，回答以下问题：\n" + query;
        return aiService.chat(buildRAGSystemPrompt(true), ragQuery);
    }

    private String buildRAGSystemPrompt(boolean hasContext) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的AI学习助手，具备知识库检索增强能力。\n\n");

        if (hasContext) {
            prompt.append("我已经为你检索了相关的知识库内容。请基于提供的知识库内容来回答问题。\n");
            prompt.append("要求：\n");
            prompt.append("1. 优先使用知识库中的信息进行回答\n");
            prompt.append("2. 如果知识库内容不足以完整回答，可以补充你自己的知识，但要明确标注\n");
            prompt.append("3. 引用知识库内容时，说明信息来源\n");
            prompt.append("4. 回答要准确、完整、有条理\n");
            prompt.append("5. 使用中文回复\n");
        } else {
            prompt.append("当前没有检索到相关的知识库内容。请基于你的通用知识来回答问题，并告知用户建议补充相关知识到知识库。\n");
        }

        return prompt.toString();
    }

    public List<String> chunkText(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.singletonList("");
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());

            if (end < text.length()) {
                int lastPeriod = text.lastIndexOf('。', end);
                int lastNewline = text.lastIndexOf('\n', end);
                int breakPoint = Math.max(lastPeriod, lastNewline);

                if (breakPoint > start + chunkSize / 2) {
                    end = breakPoint + 1;
                }
            }

            chunks.add(text.substring(start, end).trim());
            start = end - chunkOverlap;

            if (start < 0) start = 0;
            if (start >= text.length()) break;
        }

        return chunks;
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("vectorStoreAvailable", vectorStoreService.isAvailable());
        status.put("localStoreSize", getLocalStoreSize());
        status.put("chunkSize", chunkSize);
        status.put("topK", topK);
        status.put("relevanceThreshold", relevanceThreshold);
        return status;
    }

    private int getLocalStoreSize() {
        return vectorStoreService.getLocalStoreSize();
    }
}
