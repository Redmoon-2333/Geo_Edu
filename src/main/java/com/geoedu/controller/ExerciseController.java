package com.geoedu.controller;

import com.geoedu.model.dto.ApiResponse;
import com.geoedu.model.dto.ExerciseRequest;
import com.geoedu.model.dto.ExerciseResponse;
import com.geoedu.model.dto.PracticeRecordDTO;
import com.geoedu.service.ExerciseService;
import com.geoedu.util.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exercise")
@RequiredArgsConstructor
@Slf4j
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping("/start")
    public ApiResponse<ExerciseResponse> startExercise(
            @RequestParam String knowledgeId,
            @RequestParam(defaultValue = "simple") String difficulty) {
        String userId = AuthenticationUtils.getCurrentUserId();
        log.info("startExercise called: userId={}, knowledgeId={}, difficulty={}", userId, knowledgeId, difficulty);
        ExerciseResponse response = exerciseService.startExercise(userId, knowledgeId, difficulty);
        return ApiResponse.success(response);
    }

    @PostMapping("/answer")
    public ApiResponse<ExerciseResponse> submitAnswer(
            @RequestBody ExerciseRequest request) {
        String userId = AuthenticationUtils.getCurrentUserId();
        log.info("submitAnswer called: userId={}, questionId={}, userAnswer={}",
                userId, request.getQuestionId(), request.getUserAnswer());
        ExerciseResponse response = exerciseService.submitAnswer(userId, request);
        return ApiResponse.success(response);
    }

    @GetMapping("/next")
    public ApiResponse<ExerciseResponse> getNextQuestion(
            @RequestParam String knowledgeId,
            @RequestParam(defaultValue = "simple") String difficulty) {
        String userId = AuthenticationUtils.getCurrentUserId();
        ExerciseResponse response = exerciseService.getNextQuestion(userId, knowledgeId, difficulty);
        return ApiResponse.success(response);
    }

    @GetMapping("/records")
    public ApiResponse<List<PracticeRecordDTO>> getUserPracticeRecords(
            @RequestParam(required = false) String knowledgeId) {
        String userId = AuthenticationUtils.getCurrentUserId();
        List<PracticeRecordDTO> records = exerciseService.getUserPracticeRecords(userId, knowledgeId);
        return ApiResponse.success(records);
    }
}
