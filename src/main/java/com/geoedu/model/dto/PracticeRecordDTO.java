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
public class PracticeRecordDTO {
    private String id;
    private String questionId;
    private String knowledgeId;
    private String questionContent;
    private String correctAnswer;
    private String userAnswer;
    private Boolean isCorrect;
    private String difficulty;
    private String practiceType;
    private Integer timeTaken;
    private LocalDateTime createdAt;
}
