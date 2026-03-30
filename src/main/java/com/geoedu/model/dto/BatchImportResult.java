package com.geoedu.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchImportResult {

    private int totalCount;
    private int successCount;
    private int failedCount;
    private List<String> errors;
    private List<KnowledgeDTO> importedItems;

    public static BatchImportResult success(int total, int success, List<KnowledgeDTO> items) {
        return BatchImportResult.builder()
                .totalCount(total)
                .successCount(success)
                .failedCount(total - success)
                .importedItems(items)
                .errors(List.of())
                .build();
    }

    public static BatchImportResult partial(int total, int success, List<KnowledgeDTO> items, List<String> errors) {
        return BatchImportResult.builder()
                .totalCount(total)
                .successCount(success)
                .failedCount(total - success)
                .importedItems(items)
                .errors(errors)
                .build();
    }
}
