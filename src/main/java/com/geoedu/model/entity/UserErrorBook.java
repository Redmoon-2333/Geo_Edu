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
public class UserErrorBook {
    private String id;
    private String userId;
    private String questionId;
    private Integer errorCount;
    private LocalDateTime lastErrorTime;
    private Boolean isFavorited;
    private LocalDateTime createdAt;
}
