package com.geoedu.service;

import com.geoedu.config.VectorProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorService {

    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;
    private final VectorProperties vectorProperties;

    private static final String VECTOR_KEY_PREFIX = "geoedu:vector:";

    @PostConstruct
    public void initVectorIndex() {
        log.info("Vector service initialized (using SpringAI VectorStore with prefix: {}, dimension: {})", 
            VECTOR_KEY_PREFIX, vectorProperties.getDimension());
    }

    public float[] embedText(String text) {
        return embeddingModel.embed(text);
    }

    public void saveVector(String knowledgeId, float[] vector, Map<String, String> metadata) {
        String content = metadata.getOrDefault("title", "") + " " + metadata.getOrDefault("content", "");

        Document document = Document.builder()
                .id(knowledgeId)
                .text(content)
                .metadata(new HashMap<>(metadata))
                .build();

        vectorStore.add(List.of(document));
        log.debug("Saved vector for knowledge: {} via VectorStore", knowledgeId);
    }

    public List<SearchResult> searchSimilar(String queryText, int topK, Map<String, String> filters) {
        int effectiveTopK = topK > 0 ? topK : vectorProperties.getSearchTopK();
        
        List<Document> results = vectorStore.similaritySearch(SearchRequest.builder()
                .query(queryText)
                .topK(effectiveTopK)
                .filterExpression(buildFilterExpression(filters))
                .build());

        return results.stream()
                .map(doc -> {
                    Map<String, String> stringMetadata = new HashMap<>();
                    doc.getMetadata().forEach((k, v) -> stringMetadata.put(k, String.valueOf(v)));
                    return SearchResult.builder()
                            .knowledgeId(doc.getId())
                            .score(extractScoreFromDocument(doc))
                            .metadata(stringMetadata)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private float extractScoreFromDocument(Document doc) {
        // Spring AI 1.0.0-M6 不支持 getScore() 方法
        // 尝试从 metadata 中获取分数
        Object scoreFromMetadata = doc.getMetadata().get("score");
        if (scoreFromMetadata instanceof Number) {
            return ((Number) scoreFromMetadata).floatValue();
        }
        // 如果无法获取分数，返回默认值
        // TODO: 升级到 Spring AI 1.0.0-M7+ 后可以使用 doc.getScore()
        return 1.0f;
    }

    private String buildFilterExpression(Map<String, String> filters) {
        if (filters == null || filters.isEmpty()) {
            return null;
        }

        List<String> expressions = new ArrayList<>();
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            if (!"*".equals(entry.getValue())) {
                String escapedValue = entry.getValue().replace("'", "\\'");
                expressions.add(String.format("%s == '%s'", entry.getKey(), escapedValue));
            }
        }

        return expressions.isEmpty() ? null : String.join(" && ", expressions);
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SearchResult {
        private String knowledgeId;
        private float[] vector;
        private float score;
        private Map<String, String> metadata;
    }
}
