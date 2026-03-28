package com.geoedu.service;

import com.geoedu.mapper.QuestionMapper;
import com.geoedu.model.dto.PageResponse;
import com.geoedu.model.dto.QuestionDTO;
import com.geoedu.model.entity.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuestionMapper questionMapper;

    @Transactional
    public QuestionDTO create(String knowledgeId, String questionText, String answer, String type) {
        Question question = Question.builder()
                .id(UUID.randomUUID().toString())
                .knowledgeId(knowledgeId)
                .question(questionText)
                .answer(answer)
                .type(type != null ? type : "default")
                .createdAt(LocalDateTime.now())
                .build();

        questionMapper.insert(question);
        log.info("Created question: {} for knowledge: {}", question.getId(), knowledgeId);

        return toDTO(question);
    }

    public QuestionDTO getById(String id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new RuntimeException("Question not found with id: " + id);
        }
        return toDTO(question);
    }

    public List<QuestionDTO> getByKnowledgeId(String knowledgeId) {
        return questionMapper.findByKnowledgeId(knowledgeId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<QuestionDTO> getByKnowledgeIdAndType(String knowledgeId, String type) {
        return questionMapper.findByKnowledgeIdAndType(knowledgeId, type).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public QuestionDTO update(String id, String questionText, String answer, String type) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new RuntimeException("Question not found with id: " + id);
        }

        if (questionText != null) {
            question.setQuestion(questionText);
        }
        if (answer != null) {
            question.setAnswer(answer);
        }
        if (type != null) {
            question.setType(type);
        }

        questionMapper.update(question);
        log.info("Updated question: {}", id);

        return toDTO(question);
    }

    @Transactional
    public void delete(String id) {
        if (questionMapper.selectById(id) == null) {
            throw new RuntimeException("Question not found with id: " + id);
        }
        questionMapper.deleteById(id);
        log.info("Deleted question: {}", id);
    }

    private QuestionDTO toDTO(Question question) {
        return QuestionDTO.builder()
                .id(question.getId())
                .content(question.getQuestion())
                .type(question.getType())
                .answer(question.getAnswer())
                .knowledgeId(question.getKnowledgeId())
                .build();
    }
}