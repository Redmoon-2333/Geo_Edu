package com.geoedu;

import com.geoedu.model.dto.ChatRequest;
import com.geoedu.model.dto.ChatResponse;
import com.geoedu.service.ChatService;
import com.geoedu.service.VectorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChatServiceIntegrationTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private VectorService vectorService;

    @Test
    void testVectorSearchReturnsResults() {
        String query = "地球的内部结构";
        List<VectorService.SearchResult> results = vectorService.searchSimilar(query, 3, null);
        
        assertNotNull(results, "Search results should not be null");
        
        System.out.println("Search results for query: " + query);
        for (VectorService.SearchResult result : results) {
            System.out.printf("  - ID: %s, Score: %.4f%n", 
                    result.getKnowledgeId(), result.getScore());
        }
    }

    @Test
    void testChatWithValidQuestion() {
        ChatRequest request = ChatRequest.builder()
                .question("什么是大气环流？")
                .topK(3)
                .build();

        ChatResponse response = chatService.chat(request, "test-user");

        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getAnswer(), "Answer should not be null");
        assertFalse(response.getAnswer().isEmpty(), "Answer should not be empty");
        
        System.out.println("Question: " + request.getQuestion());
        System.out.println("Answer: " + response.getAnswer());
        System.out.println("Related knowledge count: " + response.getRelatedKnowledge().size());
        System.out.println("Related questions count: " + response.getRelatedQuestions().size());
    }

    @Test
    void testChatResponseContainsRelatedKnowledge() {
        ChatRequest request = ChatRequest.builder()
                .question("板块构造")
                .topK(5)
                .build();

        ChatResponse response = chatService.chat(request, "test-user");

        assertNotNull(response.getRelatedKnowledge());
        System.out.println("Found " + response.getRelatedKnowledge().size() + " related knowledge items");
    }
}
