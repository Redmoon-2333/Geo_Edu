package com.geoedu.controller;

import com.geoedu.model.dto.ApiResponse;
import com.geoedu.model.dto.BatchImportResult;
import com.geoedu.model.dto.QuestionDTO;
import com.geoedu.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
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

    @GetMapping("/search")
    public ApiResponse<List<QuestionDTO>> searchByKnowledgeIdAndType(
            @RequestParam String knowledgeId,
            @RequestParam(required = false) String type) {
        List<QuestionDTO> questions;
        if (type != null && !type.isEmpty()) {
            questions = questionService.getByKnowledgeIdAndType(knowledgeId, type);
        } else {
            questions = questionService.getByKnowledgeId(knowledgeId);
        }
        return ApiResponse.success(questions);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<QuestionDTO> update(
            @PathVariable String id,
            @RequestParam(required = false) String question,
            @RequestParam(required = false) String answer,
            @RequestParam(required = false) String type) {

        QuestionDTO dto = questionService.update(id, question, answer, type);
        return ApiResponse.success(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<Void> delete(@PathVariable String id) {
        questionService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/batch/excel")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<BatchImportResult> importFromExcel(@RequestParam("file") MultipartFile file) {
        BatchImportResult result = questionService.importFromExcel(file);
        return ApiResponse.success(result);
    }

    @PostMapping("/batch/json")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<BatchImportResult> importFromJson(@RequestParam("file") MultipartFile file) {
        BatchImportResult result = questionService.importFromJson(file);
        return ApiResponse.success(result);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<BatchImportResult> importFromJsonList(@Valid @RequestBody List<QuestionDTO> requests) {
        BatchImportResult result = questionService.importFromJsonList(requests);
        return ApiResponse.success(result);
    }
}