package com.geoedu;

import com.geoedu.model.dto.BatchImportResult;
import com.geoedu.model.dto.KnowledgeCreateRequest;
import com.geoedu.service.KnowledgeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BatchImportTest {

    @Autowired
    private KnowledgeService knowledgeService;

    @Test
    void testBatchImportFromJsonList() {
        List<KnowledgeCreateRequest> requests = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            KnowledgeCreateRequest request = KnowledgeCreateRequest.builder()
                    .id("test-batch-" + System.currentTimeMillis() + "-" + i)
                    .title("测试知识点 " + i)
                    .content("这是第 " + i + " 个测试知识点的内容，用于验证批量导入功能。")
                    .difficulty("medium")
                    .grade("高中")
                    .chapter("测试章节")
                    .build();
            requests.add(request);
        }

        BatchImportResult result = knowledgeService.importFromJsonList(requests);

        System.out.println("\n========================================");
        System.out.println("Batch Import Test Report");
        System.out.println("========================================");
        System.out.println("Total count: " + result.getTotalCount());
        System.out.println("Success count: " + result.getSuccessCount());
        System.out.println("Failed count: " + result.getFailedCount());
        
        if (!result.getErrors().isEmpty()) {
            System.out.println("\nErrors:");
            for (String error : result.getErrors()) {
                System.out.println("  - " + error);
            }
        }
        System.out.println("========================================\n");

        assertEquals(5, result.getTotalCount());
        assertTrue(result.getSuccessCount() > 0, "At least some items should be imported successfully");
    }

    @Test
    void testBatchImportWithInvalidData() {
        List<KnowledgeCreateRequest> requests = new ArrayList<>();
        
        requests.add(KnowledgeCreateRequest.builder()
                .title(null)
                .content("有效内容")
                .build());
        
        requests.add(KnowledgeCreateRequest.builder()
                .title("有效标题")
                .content(null)
                .build());
        
        requests.add(KnowledgeCreateRequest.builder()
                .title("有效标题")
                .content("有效内容")
                .build());

        BatchImportResult result = knowledgeService.importFromJsonList(requests);

        System.out.println("\n========================================");
        System.out.println("Batch Import Validation Test Report");
        System.out.println("========================================");
        System.out.println("Total count: " + result.getTotalCount());
        System.out.println("Success count: " + result.getSuccessCount());
        System.out.println("Failed count: " + result.getFailedCount());
        System.out.println("Errors: " + result.getErrors());
        System.out.println("========================================\n");

        assertTrue(result.getFailedCount() > 0, "Invalid data should cause failures");
        assertFalse(result.getErrors().isEmpty(), "Errors should be reported");
    }
}
