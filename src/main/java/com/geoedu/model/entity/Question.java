package com.geoedu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    private String id;
    private String knowledgeId;
    private String question;
    private String answer;
    private String type;
    private LocalDateTime createdAt;
}
