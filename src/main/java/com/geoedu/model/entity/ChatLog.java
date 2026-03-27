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
public class ChatLog {

    private String id;
    private String sessionId;
    private String question;
    private String answer;
    private String retrievedKnowledge;
    private Integer responseTime;
    private String userId;
    private LocalDateTime createdAt;
}
