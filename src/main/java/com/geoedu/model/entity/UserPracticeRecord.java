package com.geoedu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPracticeRecord {
    private String id;
    private String userId;
    private String questionId;
    private String knowledgeId;
    private String userAnswer;
    private Boolean isCorrect;
    private String difficulty;
    private String practiceType;
    private Integer timeTaken;
    private LocalDateTime createdAt;
}
