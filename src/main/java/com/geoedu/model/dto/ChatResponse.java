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
public class ChatResponse {
    private String answer;
    private List<ImageDTO> images;
    private List<KnowledgeBaseDTO> relatedKnowledge;
    private List<QuestionDTO> relatedQuestions;
    private Boolean askFavoriteError;
    private String questionId;
}
