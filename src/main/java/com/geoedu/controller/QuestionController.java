package com.geoedu.controller;

import com.geoedu.model.dto.ApiResponse;
import com.geoedu.model.dto.QuestionDTO;
import com.geoedu.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public ApiResponse<QuestionDTO> create(
            @RequestParam String knowledgeId,
            @RequestParam String question,
            @RequestParam String answer,
            @RequestParam(required = false, defaultValue = "default") String type) {

        QuestionDTO dto = questionService.create(knowledgeId, question, answer, type);
        return ApiResponse.success(dto);
    }

    @GetMapping("/{id}")
    public ApiResponse<QuestionDTO> getById(@PathVariable String id) {
        QuestionDTO dto = questionService.getById(id);
        return ApiResponse.success(dto);
    }

    @GetMapping("/knowledge/{knowledgeId}")
    public ApiResponse<List<QuestionDTO>> getByKnowledgeId(@PathVariable String knowledgeId) {
        List<QuestionDTO> questions = questionService.getByKnowledgeId(knowledgeId);
        return ApiResponse.success(questions);
    }

    @PutMapping("/{id}")
    public ApiResponse<QuestionDTO> update(
            @PathVariable String id,
            @RequestParam(required = false) String question,
            @RequestParam(required = false) String answer,
            @RequestParam(required = false) String type) {

        QuestionDTO dto = questionService.update(id, question, answer, type);
        return ApiResponse.success(dto);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        questionService.delete(id);
        return ApiResponse.success(null);
    }
}