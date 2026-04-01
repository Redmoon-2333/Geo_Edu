package com.geoedu.model.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionExcelDTO {

    @ExcelProperty(value = "知识点ID", index = 0)
    private String knowledgeId;

    @ExcelProperty(value = "题目", index = 1)
    private String question;

    @ExcelProperty(value = "答案", index = 2)
    private String answer;

    @ExcelProperty(value = "类型", index = 3)
    private String type;
}
