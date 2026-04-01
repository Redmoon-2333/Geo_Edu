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
public class BatchImportResult<T> {

    private int totalCount;
    private int successCount;
    private int failedCount;
    private List<String> errors;
    private List<T> importedItems;

    public static <T> BatchImportResult<T> success(int total, int success, List<T> items) {
        return BatchImportResult.<T>builder()
                .totalCount(total)
                .successCount(success)
                .failedCount(total - success)
                .importedItems(items)
                .errors(List.of())
                .build();
    }

    public static <T> BatchImportResult<T> partial(int total, int success, List<T> items, List<String> errors) {
        return BatchImportResult.<T>builder()
                .totalCount(total)
                .successCount(success)
                .failedCount(total - success)
                .importedItems(items)
                .errors(errors)
                .build();
    }
}
