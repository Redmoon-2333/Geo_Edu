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
public class Image {

    private String id;
    private String path;
    private String originalName;
    private Long fileSize;
    private String mimeType;
    private LocalDateTime createdAt;
}
