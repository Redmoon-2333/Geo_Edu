package com.geoedu.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorBookDTO {
    private String id;
    private String questionId;
    private String questionContent;
    private String answer;
    private String knowledgeId;
    private String knowledgeTitle;
    private Integer errorCount;
    private LocalDateTime lastErrorTime;
    private Boolean isFavorited;
    private LocalDateTime createdAt;
}
