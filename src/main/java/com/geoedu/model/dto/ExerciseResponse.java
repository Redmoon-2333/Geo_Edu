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
public class ExerciseResponse {
    private String questionId;
    private String questionContent;
    private List<String> options;
    private Boolean isCorrect;
    private String correctAnswer;
    private String explanation;
    private String nextQuestionId;
    private Boolean isCompleted;
    private String currentDifficulty;
    private String nextDifficulty;
}
