package com.geoedu.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseRequest {
    private String userId;
    private String knowledgeId;
    private String difficulty;
    private String questionId;
    private String userAnswer;
    private Integer timeTaken;
}
