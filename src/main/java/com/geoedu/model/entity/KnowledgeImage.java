package com.geoedu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeImage {

    private String knowledgeId;
    private String imageId;
    private Integer displayOrder;
}
