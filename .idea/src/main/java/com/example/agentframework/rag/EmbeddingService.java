package com.example.agentframework.rag;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class EmbeddingService {

    @Value("${ai.api.key:}")
    private String apiKey;

    @Value("${embedding.api.url:https://open.bigmodel.cn/api/paas/v4/embeddings}")
    private String embeddingApiUrl;

    @Value("${embedding.api.model:embedding-3}")
    private String embeddingModel;

    @Value("${milvus.dimension:1024}")
    private int dimension;

    private final OkHttpClient client;
    private final Gson gson;

    public EmbeddingService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    public List<Float> embed(String text) {
        if (apiKey == null || apiKey.isEmpty()) {
            return generateLocalEmbedding(text);
        }

        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", embeddingModel);

            JsonArray inputArray = new JsonArray();
            inputArray.add(text);
            requestBody.add("input", inputArray);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    requestBody.toString()
            );

            Request request = new Request.Builder()
                    .url(embeddingApiUrl)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return generateLocalEmbedding(text);
                }

                String responseBody = response.body().string();
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

                JsonArray data = jsonResponse.getAsJsonArray("data");
                if (data != null && data.size() > 0) {
                    JsonObject firstEmbedding = data.get(0).getAsJsonObject();
                    JsonArray embeddingArray = firstEmbedding.getAsJsonArray("embedding");

                    List<Float> embedding = new ArrayList<>();
                    for (int i = 0; i < embeddingArray.size(); i++) {
                        embedding.add(embeddingArray.get(i).getAsFloat());
                    }
                    return embedding;
                }
            }
        } catch (IOException e) {
            System.err.println("Embedding API error: " + e.getMessage());
        }

        return generateLocalEmbedding(text);
    }

    public List<List<Float>> embedBatch(List<String> texts) {
        List<List<Float>> results = new ArrayList<>();
        for (String text : texts) {
            results.add(embed(text));
        }
        return results;
    }

    private List<Float> generateLocalEmbedding(String text) {
        List<Float> embedding = new ArrayList<>();
        int hash = text.hashCode();
        java.util.Random random = new java.util.Random(hash);

        for (int i = 0; i < dimension; i++) {
            embedding.add((float) (random.nextGaussian() * 0.1));
        }

        double norm = 0;
        for (float v : embedding) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);

        List<Float> normalized = new ArrayList<>();
        for (float v : embedding) {
            normalized.add((float) (v / (norm + 1e-8)));
        }
        return normalized;
    }

    public static double cosineSimilarity(List<Float> a, List<Float> b) {
        if (a == null || b == null || a.size() != b.size()) {
            return 0;
        }
        double dotProduct = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.size(); i++) {
            dotProduct += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }
        if (normA == 0 || normB == 0) return 0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
