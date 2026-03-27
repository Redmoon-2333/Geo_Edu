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
public class KnowledgeDTO {
    private String id;
    private String title;
    private String content;
    private String difficulty;
    private Integer pageNumber;
    private String grade;
    private String chapter;
    private List<String> imageUrls;
}
