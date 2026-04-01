package com.geoedu.controller;

import com.geoedu.model.dto.ApiResponse;
import com.geoedu.model.dto.ExerciseRequest;
import com.geoedu.model.dto.ExerciseResponse;
import com.geoedu.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/exercise")
@RequiredArgsConstructor
@Slf4j
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping("/start")
    public ApiResponse<ExerciseResponse> startExercise(
            @RequestParam String userId,
            @RequestParam String knowledgeId,
            @RequestParam(defaultValue = "simple") String difficulty) {
        log.info("startExercise called: userId={}, knowledgeId={}, difficulty={}", userId, knowledgeId, difficulty);
        ExerciseResponse response = exerciseService.startExercise(userId, knowledgeId, difficulty);
        return ApiResponse.success(response);
    }

    @PostMapping("/answer")
    public ApiResponse<ExerciseResponse> submitAnswer(
            @RequestBody ExerciseRequest request) {
        log.info("submitAnswer called: userId={}, questionId={}, userAnswer={}",
                request.getUserId(), request.getQuestionId(), request.getUserAnswer());
        try {
            ExerciseResponse response = exerciseService.submitAnswer(request.getUserId(), request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("submitAnswer failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/next")
    public ApiResponse<ExerciseResponse> getNextQuestion(
            @RequestParam String userId,
            @RequestParam String knowledgeId,
            @RequestParam(defaultValue = "simple") String difficulty) {
        ExerciseResponse response = exerciseService.getNextQuestion(userId, knowledgeId, difficulty);
        return ApiResponse.success(response);
    }
}
