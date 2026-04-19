package com.example.agentframework.rag;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RAGServiceTest {

    @Test
    void testChunkTextWithShortText() {
        String text = "Short text.";
        RAGService ragService = createRAGService();
        java.util.List<String> chunks = ragService.chunkText(text);
        assertNotNull(chunks);
        assertTrue(chunks.size() >= 1, "Should return at least one chunk");
    }

    @Test
    void testChunkTextWithEmptyInput() {
        RAGService ragService = createRAGService();
        java.util.List<String> chunks = ragService.chunkText("");
        assertNotNull(chunks);
    }

    @Test
    void testChunkTextReturnsList() {
        RAGService ragService = createRAGService();
        java.util.List<String> chunks = ragService.chunkText("Test content.");
        assertNotNull(chunks, "Should return a list");
        assertTrue(chunks instanceof java.util.List, "Should return List type");
    }

    @Test
    void testChunkTextWithNull() {
        RAGService ragService = createRAGService();
        java.util.List<String> chunks = ragService.chunkText(null);
        assertNotNull(chunks, "Should handle null gracefully");
    }

    private RAGService createRAGService() {
        try {
            java.lang.reflect.Constructor<RAGService> constructor = RAGService.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create RAGService for testing", e);
        }
    }
}