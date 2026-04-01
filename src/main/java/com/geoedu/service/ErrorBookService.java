package com.geoedu.service;

import com.geoedu.exception.BusinessException;
import com.geoedu.mapper.KnowledgeMapper;
import com.geoedu.mapper.QuestionMapper;
import com.geoedu.mapper.UserErrorBookMapper;
import com.geoedu.model.dto.ErrorBookDTO;
import com.geoedu.model.entity.Knowledge;
import com.geoedu.model.entity.Question;
import com.geoedu.model.entity.UserErrorBook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ErrorBookService {

    private final UserErrorBookMapper errorBookMapper;
    private final QuestionMapper questionMapper;
    private final KnowledgeMapper knowledgeMapper;

    @Transactional
    public ErrorBookDTO addToErrorBook(String userId, String questionId, boolean isFavorited) {
        log.info("Adding question {} to error book for user {}", questionId, userId);

        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException("题目不存在: " + questionId);
        }

        UserErrorBook existing = errorBookMapper.findByUserIdAndQuestionId(userId, questionId);

        if (existing != null) {
            existing.setErrorCount(existing.getErrorCount() + 1);
            existing.setLastErrorTime(LocalDateTime.now());
            existing.setIsFavorited(isFavorited);
            errorBookMapper.update(existing);
            log.info("Updated existing error book entry: {}", existing.getId());
            return toDTO(existing, question, null);
        }

        UserErrorBook errorBook = UserErrorBook.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .questionId(questionId)
                .errorCount(1)
                .lastErrorTime(LocalDateTime.now())
                .isFavorited(isFavorited)
                .createdAt(LocalDateTime.now())
                .build();

        errorBookMapper.insert(errorBook);
        log.info("Created new error book entry: {}", errorBook.getId());

        return toDTO(errorBook, question, null);
    }

    public List<ErrorBookDTO> getUserErrorBooks(String userId) {
        log.info("Getting error book for user {}", userId);
        List<UserErrorBook> errorBooks = errorBookMapper.findByUserId(userId);
        if (errorBooks.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> questionIds = errorBooks.stream()
                .map(UserErrorBook::getQuestionId)
                .toList();

        List<Question> questions = questionMapper.selectByIds(questionIds);
        Map<String, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<String> knowledgeIds = questions.stream()
                .map(Question::getKnowledgeId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();

        Map<String, Knowledge> knowledgeMap = Collections.emptyMap();
        if (!knowledgeIds.isEmpty()) {
            List<Knowledge> knowledges = knowledgeMapper.selectAllByIds(knowledgeIds);
            knowledgeMap = knowledges.stream()
                    .collect(Collectors.toMap(Knowledge::getId, k -> k));
        }

        List<ErrorBookDTO> result = new ArrayList<>();
        for (UserErrorBook eb : errorBooks) {
            Question question = questionMap.get(eb.getQuestionId());
            Knowledge knowledge = question != null ? knowledgeMap.get(question.getKnowledgeId()) : null;
            result.add(toDTO(eb, question, knowledge));
        }

        return result;
    }

    @Transactional
    public boolean removeFromErrorBook(String userId, String questionId) {
        log.info("Removing question {} from error book for user {}", questionId, userId);
        int deleted = errorBookMapper.deleteByUserIdAndQuestionId(userId, questionId);
        return deleted > 0;
    }

    @Transactional
    public boolean updateFavoriteStatus(String userId, String questionId, boolean isFavorited) {
        log.info("Updating favorite status for question {} to {} for user {}", questionId, isFavorited, userId);
        UserErrorBook existing = errorBookMapper.findByUserIdAndQuestionId(userId, questionId);
        if (existing != null) {
            existing.setIsFavorited(isFavorited);
            errorBookMapper.update(existing);
            return true;
        }
        return false;
    }

    private ErrorBookDTO toDTO(UserErrorBook errorBook, Question question, Knowledge knowledge) {
        return ErrorBookDTO.builder()
                .id(errorBook.getId())
                .questionId(errorBook.getQuestionId())
                .questionContent(question != null ? question.getQuestion() : null)
                .answer(question != null ? question.getAnswer() : null)
                .knowledgeId(question != null ? question.getKnowledgeId() : null)
                .knowledgeTitle(knowledge != null ? knowledge.getTitle() : null)
                .errorCount(errorBook.getErrorCount())
                .lastErrorTime(errorBook.getLastErrorTime())
                .isFavorited(errorBook.getIsFavorited())
                .createdAt(errorBook.getCreatedAt())
                .build();
    }
}
