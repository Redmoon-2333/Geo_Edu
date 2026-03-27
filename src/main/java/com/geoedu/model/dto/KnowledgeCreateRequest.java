package com.geoedu.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeCreateRequest {
    @NotBlank(message = "知识ID不能为空")
    private String id;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @Builder.Default
    private String difficulty = "medium";

    private Integer pageNumber;
    private String grade;
    private String chapter;
}
