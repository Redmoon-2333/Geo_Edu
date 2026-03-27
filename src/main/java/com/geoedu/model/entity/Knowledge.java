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
public class Knowledge {

    private String id;
    private String title;
    private String content;
    private String difficulty;
    private Integer pageNumber;
    private String grade;
    private String chapter;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
