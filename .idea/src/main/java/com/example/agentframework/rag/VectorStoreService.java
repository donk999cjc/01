package com.example.agentframework.rag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VectorStoreService {

    @Value("${milvus.host:localhost}")
    private String milvusHost;

    @Value("${milvus.port:19530}")
    private int milvusPort;

    @Value("${milvus.collection.name:knowledge_vectors}")
    private String collectionName;

    @Value("${milvus.dimension:1024}")
    private int dimension;

    @Value("${milvus.nlist:1024}")
    private int nlist;

    @Value("${milvus.nprobe:16}")
    private int nprobe;

    @Value("${milvus.topk:5}")
    private int topK;

    private Object milvusClient;
    private boolean milvusAvailable = false;

    private final Map<String, List<Float>> localVectorStore = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> localMetadataStore = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        try {
            Class<?> connectParamClass = Class.forName("io.milvus.param.ConnectParam");
            Object connectParam = connectParamClass.getMethod("newBuilder").invoke(null);
            Object builder = connectParamClass.getMethod("withHost", String.class)
                    .invoke(connectParam, milvusHost);
            builder = connectParamClass.getMethod("withPort", int.class)
                    .invoke(builder, milvusPort);
            Object builtParam = connectParamClass.getMethod("build").invoke(builder);

            Class<?> clientClass = Class.forName("io.milvus.client.MilvusServiceClient");
            milvusClient = clientClass.getConstructor(connectParamClass).newInstance(builtParam);

            Object healthCheck = clientClass.getMethod("checkHealth").invoke(milvusClient);
            Class<?> rClass = Class.forName("io.milvus.param.R");
            Object status = rClass.getMethod("getStatus").invoke(healthCheck);
            int statusCode = (int) rClass.getMethod("getCode").invoke(status);

            Class<?> statusClass = Class.forName("io.milvus.param.R$Status");
            int successCodeValue = (int) statusClass.getMethod("getCode").invoke(statusClass.getField("Success").get(null));

            if (statusCode == successCodeValue) {
                milvusAvailable = true;
                ensureCollection();
                System.out.println("Milvus connected successfully");
            } else {
                milvusAvailable = false;
                System.out.println("Milvus not available, using local vector store");
            }
        } catch (Exception e) {
            milvusAvailable = false;
            System.out.println("Milvus connection failed, using local vector store: " + e.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        if (milvusClient != null) {
            try {
                milvusClient.getClass().getMethod("close").invoke(milvusClient);
            } catch (Exception e) {
                System.err.println("Error closing Milvus client: " + e.getMessage());
            }
        }
    }

    private void ensureCollection() {
        if (!milvusAvailable) return;
        try {
            Class<?> hasCollectionParamClass = Class.forName("io.milvus.param.collection.HasCollectionParam");
            Object paramBuilder = hasCollectionParamClass.getMethod("newBuilder").invoke(null);
            paramBuilder = hasCollectionParamClass.getMethod("withCollectionName", String.class)
                    .invoke(paramBuilder, collectionName);
            Object hasParam = hasCollectionParamClass.getMethod("build").invoke(paramBuilder);

            Object hasResponse = milvusClient.getClass()
                    .getMethod("hasCollection", hasCollectionParamClass).invoke(milvusClient, hasParam);

            Class<?> rClass = Class.forName("io.milvus.param.R");
            Object data = rClass.getMethod("getData").invoke(hasResponse);

            boolean hasCollection = false;
            if (data != null) {
                try {
                    Object value = data.getClass().getMethod("getValue").invoke(data);
                    hasCollection = Boolean.TRUE.equals(value);
                } catch (NoSuchMethodException e) {
                    hasCollection = false;
                }
            }

            if (!hasCollection) {
                createCollection();
            }
        } catch (Exception e) {
            System.err.println("Error checking collection: " + e.getMessage());
            try {
                createCollection();
            } catch (Exception ex) {
                System.err.println("Error creating collection: " + ex.getMessage());
            }
        }
    }

    private void createCollection() {
        if (!milvusAvailable) return;
        try {
            Class<?> fieldTypeClass = Class.forName("io.milvus.param.collection.FieldType");
            Class<?> dataTypeClass = Class.forName("io.milvus.grpc.DataType");

            Object varCharType = dataTypeClass.getField("VarChar").get(null);
            Object floatVectorType = dataTypeClass.getField("FloatVector").get(null);

            Object idFieldBuilder = fieldTypeClass.getMethod("newBuilder").invoke(null);
            idFieldBuilder = fieldTypeClass.getMethod("withName", String.class).invoke(idFieldBuilder, "id");
            idFieldBuilder = fieldTypeClass.getMethod("withDataType", dataTypeClass).invoke(idFieldBuilder, varCharType);
            idFieldBuilder = fieldTypeClass.getMethod("withMaxLength", long.class).invoke(idFieldBuilder, 256L);
            idFieldBuilder = fieldTypeClass.getMethod("withPrimaryKey", boolean.class).invoke(idFieldBuilder, true);
            idFieldBuilder = fieldTypeClass.getMethod("withAutoID", boolean.class).invoke(idFieldBuilder, false);
            Object idField = fieldTypeClass.getMethod("build").invoke(idFieldBuilder);

            Object embFieldBuilder = fieldTypeClass.getMethod("newBuilder").invoke(null);
            embFieldBuilder = fieldTypeClass.getMethod("withName", String.class).invoke(embFieldBuilder, "embedding");
            embFieldBuilder = fieldTypeClass.getMethod("withDataType", dataTypeClass).invoke(embFieldBuilder, floatVectorType);
            embFieldBuilder = fieldTypeClass.getMethod("withDimension", int.class).invoke(embFieldBuilder, dimension);
            Object embField = fieldTypeClass.getMethod("build").invoke(embFieldBuilder);

            Object contentFieldBuilder = fieldTypeClass.getMethod("newBuilder").invoke(null);
            contentFieldBuilder = fieldTypeClass.getMethod("withName", String.class).invoke(contentFieldBuilder, "content");
            contentFieldBuilder = fieldTypeClass.getMethod("withDataType", dataTypeClass).invoke(contentFieldBuilder, varCharType);
            contentFieldBuilder = fieldTypeClass.getMethod("withMaxLength", long.class).invoke(contentFieldBuilder, 65535L);
            Object contentField = fieldTypeClass.getMethod("build").invoke(contentFieldBuilder);

            Object courseIdFieldBuilder = fieldTypeClass.getMethod("newBuilder").invoke(null);
            courseIdFieldBuilder = fieldTypeClass.getMethod("withName", String.class).invoke(courseIdFieldBuilder, "course_id");
            courseIdFieldBuilder = fieldTypeClass.getMethod("withDataType", dataTypeClass).invoke(courseIdFieldBuilder, varCharType);
            courseIdFieldBuilder = fieldTypeClass.getMethod("withMaxLength", long.class).invoke(courseIdFieldBuilder, 128L);
            Object courseIdField = fieldTypeClass.getMethod("build").invoke(courseIdFieldBuilder);

            Object knowledgeIdFieldBuilder = fieldTypeClass.getMethod("newBuilder").invoke(null);
            knowledgeIdFieldBuilder = fieldTypeClass.getMethod("withName", String.class).invoke(knowledgeIdFieldBuilder, "knowledge_id");
            knowledgeIdFieldBuilder = fieldTypeClass.getMethod("withDataType", dataTypeClass).invoke(knowledgeIdFieldBuilder, varCharType);
            knowledgeIdFieldBuilder = fieldTypeClass.getMethod("withMaxLength", long.class).invoke(knowledgeIdFieldBuilder, 256L);
            Object knowledgeIdField = fieldTypeClass.getMethod("build").invoke(knowledgeIdFieldBuilder);

            Class<?> createCollectionParamClass = Class.forName("io.milvus.param.collection.CreateCollectionParam");
            Object ccBuilder = createCollectionParamClass.getMethod("newBuilder").invoke(null);
            ccBuilder = createCollectionParamClass.getMethod("withCollectionName", String.class).invoke(ccBuilder, collectionName);
            ccBuilder = createCollectionParamClass.getMethod("withDescription", String.class).invoke(ccBuilder, "Knowledge base vector collection for RAG");
            ccBuilder = createCollectionParamClass.getMethod("withShardsNum", int.class).invoke(ccBuilder, 2);
            ccBuilder = createCollectionParamClass.getMethod("addFieldType", fieldTypeClass).invoke(ccBuilder, idField);
            ccBuilder = createCollectionParamClass.getMethod("addFieldType", fieldTypeClass).invoke(ccBuilder, embField);
            ccBuilder = createCollectionParamClass.getMethod("addFieldType", fieldTypeClass).invoke(ccBuilder, contentField);
            ccBuilder = createCollectionParamClass.getMethod("addFieldType", fieldTypeClass).invoke(ccBuilder, courseIdField);
            ccBuilder = createCollectionParamClass.getMethod("addFieldType", fieldTypeClass).invoke(ccBuilder, knowledgeIdField);
            Object createParam = createCollectionParamClass.getMethod("build").invoke(ccBuilder);

            milvusClient.getClass().getMethod("createCollection", createCollectionParamClass)
                    .invoke(milvusClient, createParam);

            createIndex();
        } catch (Exception e) {
            System.err.println("Error creating collection: " + e.getMessage());
        }
    }

    private void createIndex() {
        if (!milvusAvailable) return;
        try {
            Class<?> createIndexParamClass = Class.forName("io.milvus.param.index.CreateIndexParam");
            Class<?> indexTypeClass = Class.forName("io.milvus.param.index.IndexType");
            Class<?> metricTypeClass = Class.forName("io.milvus.param.MetricType");

            Object indexTypeVal = Enum.valueOf((Class<Enum>) indexTypeClass, "IVF_FLAT");
            Object metricTypeVal = Enum.valueOf((Class<Enum>) metricTypeClass, "COSINE");

            Object idxBuilder = createIndexParamClass.getMethod("newBuilder").invoke(null);
            idxBuilder = createIndexParamClass.getMethod("withCollectionName", String.class).invoke(idxBuilder, collectionName);
            idxBuilder = createIndexParamClass.getMethod("withFieldName", String.class).invoke(idxBuilder, "embedding");
            idxBuilder = createIndexParamClass.getMethod("withIndexType", indexTypeClass).invoke(idxBuilder, indexTypeVal);
            idxBuilder = createIndexParamClass.getMethod("withMetricType", metricTypeClass).invoke(idxBuilder, metricTypeVal);
            idxBuilder = createIndexParamClass.getMethod("withExtraParam", String.class).invoke(idxBuilder, "{\"nlist\":" + nlist + "}");
            Object idxParam = createIndexParamClass.getMethod("build").invoke(idxBuilder);

            milvusClient.getClass().getMethod("createIndex", createIndexParamClass)
                    .invoke(milvusClient, idxParam);

            Class<?> loadCollectionParamClass = Class.forName("io.milvus.param.collection.LoadCollectionParam");
            Object loadBuilder = loadCollectionParamClass.getMethod("newBuilder").invoke(null);
            loadBuilder = loadCollectionParamClass.getMethod("withCollectionName", String.class).invoke(loadBuilder, collectionName);
            Object loadParam = loadCollectionParamClass.getMethod("build").invoke(loadBuilder);

            milvusClient.getClass().getMethod("loadCollection", loadCollectionParamClass)
                    .invoke(milvusClient, loadParam);
        } catch (Exception e) {
            System.err.println("Error creating index: " + e.getMessage());
        }
    }

    public boolean storeVector(String id, List<Float> embedding, String content, String courseId, String knowledgeId) {
        if (milvusAvailable) {
            return storeToMilvus(id, embedding, content, courseId, knowledgeId);
        } else {
            localVectorStore.put(id, embedding);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("content", content);
            metadata.put("courseId", courseId);
            metadata.put("knowledgeId", knowledgeId);
            localMetadataStore.put(id, metadata);
            return true;
        }
    }

    private boolean storeToMilvus(String id, List<Float> embedding, String content, String courseId, String knowledgeId) {
        try {
            Class<?> insertParamClass = Class.forName("io.milvus.param.dml.InsertParam");
            Class<?> fieldClass = Class.forName("io.milvus.param.dml.InsertParam$Field");

            List<Object> fields = new ArrayList<>();

            Object idField = fieldClass.getConstructor(String.class, List.class)
                    .newInstance("id", Collections.singletonList(id));
            Object embField = fieldClass.getConstructor(String.class, List.class)
                    .newInstance("embedding", Collections.singletonList(embedding));
            Object contentField = fieldClass.getConstructor(String.class, List.class)
                    .newInstance("content", Collections.singletonList(content));
            Object courseIdField = fieldClass.getConstructor(String.class, List.class)
                    .newInstance("course_id", Collections.singletonList(courseId));
            Object knowledgeIdField = fieldClass.getConstructor(String.class, List.class)
                    .newInstance("knowledge_id", Collections.singletonList(knowledgeId));

            fields.add(idField);
            fields.add(embField);
            fields.add(contentField);
            fields.add(courseIdField);
            fields.add(knowledgeIdField);

            Object insertBuilder = insertParamClass.getMethod("newBuilder").invoke(null);
            insertBuilder = insertParamClass.getMethod("withCollectionName", String.class).invoke(insertBuilder, collectionName);
            insertBuilder = insertParamClass.getMethod("withFields", List.class).invoke(insertBuilder, fields);
            Object insertParam = insertParamClass.getMethod("build").invoke(insertBuilder);

            Object insertResponse = milvusClient.getClass().getMethod("insert", insertParamClass)
                    .invoke(milvusClient, insertParam);

            Class<?> rClass = Class.forName("io.milvus.param.R");
            Object status = rClass.getMethod("getStatus").invoke(insertResponse);
            int statusCode = (int) status.getClass().getMethod("getCode").invoke(status);
            return statusCode == 0;
        } catch (Exception e) {
            System.err.println("Error storing to Milvus: " + e.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> search(List<Float> queryEmbedding, String courseId, int topK) {
        if (milvusAvailable) {
            return searchMilvus(queryEmbedding, courseId, topK);
        } else {
            return searchLocal(queryEmbedding, courseId, topK);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> searchMilvus(List<Float> queryEmbedding, String courseId, int topK) {
        try {
            Class<?> searchParamClass = Class.forName("io.milvus.param.dml.SearchParam");
            Class<?> metricTypeClass = Class.forName("io.milvus.param.MetricType");

            Object metricTypeVal = Enum.valueOf((Class<Enum>) metricTypeClass, "COSINE");

            Object searchBuilder = searchParamClass.getMethod("newBuilder").invoke(null);
            searchParamClass.getMethod("withCollectionName", String.class).invoke(searchBuilder, collectionName);
            searchParamClass.getMethod("withMetricType", metricTypeClass).invoke(searchBuilder, metricTypeVal);
            searchParamClass.getMethod("withTopK", int.class).invoke(searchBuilder, topK);
            searchParamClass.getMethod("withVectors", List.class).invoke(searchBuilder, Collections.singletonList(queryEmbedding));
            searchParamClass.getMethod("withVectorFieldName", String.class).invoke(searchBuilder, "embedding");

            try {
                searchParamClass.getMethod("withParams", String.class).invoke(searchBuilder,
                        "{\"nlist\":" + nlist + ",\"nprobe\":" + nprobe + "}");
            } catch (NoSuchMethodException e) {
                try {
                    searchParamClass.getMethod("withSearchParams", String.class).invoke(searchBuilder,
                            "{\"nlist\":" + nlist + ",\"nprobe\":" + nprobe + "}");
                } catch (NoSuchMethodException e2) {
                    System.err.println("Cannot set search params, using defaults");
                }
            }

            if (courseId != null && !courseId.isEmpty()) {
                searchParamClass.getMethod("withExpr", String.class).invoke(searchBuilder,
                        "course_id == \"" + courseId + "\"");
            }

            searchParamClass.getMethod("addOutField", String.class).invoke(searchBuilder, "content");
            searchParamClass.getMethod("addOutField", String.class).invoke(searchBuilder, "course_id");
            searchParamClass.getMethod("addOutField", String.class).invoke(searchBuilder, "knowledge_id");

            Object searchParam = searchParamClass.getMethod("build").invoke(searchBuilder);

            Object searchResponse = milvusClient.getClass().getMethod("search", searchParamClass)
                    .invoke(milvusClient, searchParam);

            Class<?> rClass = Class.forName("io.milvus.param.R");
            Object status = rClass.getMethod("getStatus").invoke(searchResponse);
            int statusCode = (int) status.getClass().getMethod("getCode").invoke(status);
            if (statusCode != 0) {
                return new ArrayList<>();
            }

            Object data = rClass.getMethod("getData").invoke(searchResponse);
            Object results = data.getClass().getMethod("getResults").invoke(data);

            Class<?> wrapperClass = Class.forName("io.milvus.response.SearchResultsWrapper");
            Object wrapper = wrapperClass.getConstructor(results.getClass()).newInstance(results);

            List<?> idScores = (List<?>) wrapperClass.getMethod("getIDScore", int.class).invoke(wrapper, 0);

            List<Map<String, Object>> resultList = new ArrayList<>();
            for (int i = 0; i < idScores.size(); i++) {
                Object idScore = idScores.get(i);
                Map<String, Object> result = new HashMap<>();

                try {
                    Object strId = idScore.getClass().getMethod("getStrID").invoke(idScore);
                    result.put("id", strId);
                } catch (NoSuchMethodException e) {
                    result.put("id", String.valueOf(i));
                }

                double score = (double) idScore.getClass().getMethod("getScore").invoke(idScore);
                result.put("score", score);

                try {
                    List<?> rowRecords = (List<?>) wrapperClass.getMethod("getRowRecords", int.class).invoke(wrapper, 0);
                    if (i < rowRecords.size()) {
                        Object record = rowRecords.get(i);
                        result.put("content", record.getClass().getMethod("get", String.class).invoke(record, "content"));
                        result.put("courseId", record.getClass().getMethod("get", String.class).invoke(record, "course_id"));
                        result.put("knowledgeId", record.getClass().getMethod("get", String.class).invoke(record, "knowledge_id"));
                    }
                } catch (Exception e) {
                    result.put("content", "");
                    result.put("courseId", courseId != null ? courseId : "");
                    result.put("knowledgeId", "");
                }

                resultList.add(result);
            }

            return resultList;
        } catch (Exception e) {
            System.err.println("Error searching Milvus: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> searchLocal(List<Float> queryEmbedding, String courseId, int topK) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (Map.Entry<String, List<Float>> entry : localVectorStore.entrySet()) {
            Map<String, Object> metadata = localMetadataStore.get(entry.getKey());
            if (metadata == null) continue;

            if (courseId != null && !courseId.isEmpty() && !courseId.equals(metadata.get("courseId"))) {
                continue;
            }

            double similarity = EmbeddingService.cosineSimilarity(queryEmbedding, entry.getValue());

            Map<String, Object> result = new HashMap<>();
            result.put("id", entry.getKey());
            result.put("score", similarity);
            result.put("content", metadata.get("content"));
            result.put("courseId", metadata.get("courseId"));
            result.put("knowledgeId", metadata.get("knowledgeId"));

            results.add(result);
        }

        results.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));

        if (results.size() > topK) {
            return results.subList(0, topK);
        }
        return results;
    }

    public boolean deleteVector(String id) {
        if (milvusAvailable) {
            try {
                Class<?> deleteParamClass = Class.forName("io.milvus.param.dml.DeleteParam");
                Object deleteBuilder = deleteParamClass.getMethod("newBuilder").invoke(null);
                deleteParamClass.getMethod("withCollectionName", String.class).invoke(deleteBuilder, collectionName);
                deleteParamClass.getMethod("withExpr", String.class).invoke(deleteBuilder, "id == \"" + id + "\"");
                Object deleteParam = deleteParamClass.getMethod("build").invoke(deleteBuilder);

                Object deleteResponse = milvusClient.getClass().getMethod("delete", deleteParamClass)
                        .invoke(milvusClient, deleteParam);

                Class<?> rClass = Class.forName("io.milvus.param.R");
                Object status = rClass.getMethod("getStatus").invoke(deleteResponse);
                int statusCode = (int) status.getClass().getMethod("getCode").invoke(status);
                return statusCode == 0;
            } catch (Exception e) {
                return false;
            }
        } else {
            localVectorStore.remove(id);
            localMetadataStore.remove(id);
            return true;
        }
    }

    public boolean isAvailable() {
        return milvusAvailable;
    }

    public int getLocalStoreSize() {
        return localVectorStore.size();
    }
}
