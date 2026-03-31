package com.geoedu.service;

import com.geoedu.exception.BusinessException;
import com.geoedu.mapper.KnowledgeMapper;
import com.geoedu.mapper.QuestionMapper;
import com.geoedu.mapper.UserPracticeRecordMapper;
import com.geoedu.model.dto.ExerciseRequest;
import com.geoedu.model.dto.ExerciseResponse;
import com.geoedu.model.entity.Question;
import com.geoedu.model.entity.UserPracticeRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseService {

    private final QuestionMapper questionMapper;
    private final KnowledgeMapper knowledgeMapper;
    private final UserPracticeRecordMapper practiceRecordMapper;

    private static final List<String> DIFFICULTY_ORDER = List.of("simple", "medium", "hard");

    public ExerciseResponse startExercise(String userId, String knowledgeId, String difficulty) {
        log.info("Starting exercise for user {} with knowledge {} at difficulty {}", userId, knowledgeId, difficulty);

        String actualDifficulty = (difficulty != null && !difficulty.isEmpty()) ? difficulty : "default";

        List<Question> questions = questionMapper.findByKnowledgeId(knowledgeId);
        if (questions.isEmpty()) {
            return ExerciseResponse.builder()
                    .isCompleted(true)
                    .currentDifficulty(actualDifficulty)
                    .nextDifficulty(actualDifficulty)
                    .build();
        }

        Collections.shuffle(questions);
        Question firstQuestion = questions.get(0);

        return buildExerciseResponse(firstQuestion, null, false);
    }

    @Transactional
    public ExerciseResponse submitAnswer(String userId, ExerciseRequest request) {
        log.info("Submitting answer for user {} on question {}", userId, request.getQuestionId());

        Question question = questionMapper.selectById(request.getQuestionId());
        if (question == null) {
            throw new BusinessException("题目不存在: " + request.getQuestionId());
        }

        boolean isCorrect = checkAnswer(question.getAnswer(), request.getUserAnswer());

        UserPracticeRecord record = UserPracticeRecord.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .questionId(request.getQuestionId())
                .knowledgeId(question.getKnowledgeId())
                .userAnswer(request.getUserAnswer())
                .isCorrect(isCorrect)
                .difficulty(question.getType() != null ? question.getType() : "default")
                .practiceType("exercise")
                .timeTaken(request.getTimeTaken())
                .createdAt(LocalDateTime.now())
                .build();

        practiceRecordMapper.insert(record);
        log.info("Saved practice record: {}, isCorrect: {}", record.getId(), isCorrect);

        List<Question> nextQuestions = questionMapper.findByKnowledgeId(question.getKnowledgeId());
        Question nextQuestion = findNextQuestion(nextQuestions, request.getQuestionId());

        return buildExerciseResponse(question, isCorrect, nextQuestion == null);
    }

    public ExerciseResponse getNextQuestion(String userId, String knowledgeId, String difficulty) {
        log.info("Getting next question for user {} at difficulty {}", userId, difficulty);

        List<Question> questions = questionMapper.findByKnowledgeId(knowledgeId);
        Question nextQuestion = findNextQuestion(questions, null);

        if (nextQuestion == null) {
            return ExerciseResponse.builder()
                    .isCompleted(true)
                    .build();
        }

        return buildExerciseResponse(nextQuestion, null, false);
    }

    private Question findNextQuestion(List<Question> questions, String excludeQuestionId) {
        if (questions == null || questions.isEmpty()) {
            return null;
        }

        List<Question> filtered = questions.stream()
                .filter(q -> !q.getId().equals(excludeQuestionId))
                .collect(java.util.stream.Collectors.toList());

        if (filtered.isEmpty()) {
            return null;
        }

        Collections.shuffle(filtered);
        return filtered.get(0);
    }

    private boolean checkAnswer(String correctAnswer, String userAnswer) {
        if (correctAnswer == null || userAnswer == null) {
            return false;
        }
        String correct = extractCorrectAnswer(correctAnswer);
        return correct.equalsIgnoreCase(userAnswer.trim());
    }

    private String extractCorrectAnswer(String answer) {
        if (answer != null && answer.contains("|")) {
            String[] parts = answer.split("\\|");
            return parts[parts.length - 1].trim();
        }
        return answer;
    }

    private ExerciseResponse buildExerciseResponse(Question question, Boolean isCorrect, boolean isCompleted) {
        boolean isChoice = isChoiceQuestion(question.getAnswer());

        return ExerciseResponse.builder()
                .questionId(question.getId())
                .questionContent(question.getQuestion())
                .options(isChoice ? parseOptions(question.getAnswer()) : null)
                .isCorrect(isCorrect)
                .correctAnswer(isCorrect != null && !isCorrect ? question.getAnswer() : null)
                .nextQuestionId(null)
                .isCompleted(isCompleted)
                .currentDifficulty(question.getType() != null ? question.getType() : "default")
                .nextDifficulty(question.getType() != null ? question.getType() : "default")
                .build();
    }

    private boolean isChoiceQuestion(String answer) {
        if (answer == null) {
            return false;
        }
        return answer.contains("|") && answer.split("\\|").length >= 2;
    }

    private List<String> parseOptions(String answer) {
        List<String> options = new ArrayList<>();
        if (answer != null && answer.contains("|")) {
            String[] parts = answer.split("\\|");
            int optionCount = parts.length - 1;
            for (int i = 0; i < optionCount; i++) {
                options.add((char) ('A' + i) + ". " + parts[i].trim());
            }
        }
        return options;
    }
}
