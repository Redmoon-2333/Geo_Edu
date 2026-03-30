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
public class KnowledgeExcelDTO {

    @ExcelProperty(value = "标题", index = 0)
    private String title;

    @ExcelProperty(value = "内容", index = 1)
    private String content;

    @ExcelProperty(value = "难度", index = 2)
    private String difficulty;

    @ExcelProperty(value = "页码", index = 3)
    private Integer pageNumber;

    @ExcelProperty(value = "年级", index = 4)
    private String grade;

    @ExcelProperty(value = "章节", index = 5)
    private String chapter;
}
