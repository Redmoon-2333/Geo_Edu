package com.geoedu.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    @NotBlank(message = "问题不能为空")
    @Size(max = 200, message = "问题长度不能超过200个字符")
    private String question;

    @Builder.Default
    private Integer topK = 3;
}
