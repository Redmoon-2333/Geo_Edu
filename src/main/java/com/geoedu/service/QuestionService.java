package com.geoedu.service;

import com.alibaba.excel.EasyExcel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geoedu.exception.BusinessException;
import com.geoedu.mapper.QuestionMapper;
import com.geoedu.model.dto.BatchImportResult;
import com.geoedu.model.dto.PageResponse;
import com.geoedu.model.dto.QuestionDTO;
import com.geoedu.model.dto.QuestionExcelDTO;
import com.geoedu.model.entity.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
            throw new BusinessException("题目不存在: " + id);
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
            throw new BusinessException("题目不存在: " + id);
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
            throw new BusinessException("题目不存在: " + id);
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

    @Transactional
    public BatchImportResult importFromExcel(MultipartFile file) {
        log.info("Starting Excel import for file: {}", file.getOriginalFilename());
        
        List<QuestionExcelDTO> excelData;
        try (InputStream inputStream = file.getInputStream()) {
            excelData = EasyExcel.read(inputStream)
                    .head(QuestionExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("Failed to read Excel file: {}", e.getMessage());
            throw new BusinessException("读取Excel文件失败: " + e.getMessage());
        }

        List<QuestionDTO> questions = excelData.stream()
                .map(dto -> QuestionDTO.builder()
                        .knowledgeId(dto.getKnowledgeId())
                        .content(dto.getQuestion())
                        .answer(dto.getAnswer())
                        .type(dto.getType() != null ? dto.getType() : "default")
                        .build())
                .collect(Collectors.toList());

        return processBatchImport(questions);
    }

    @Transactional
    public BatchImportResult importFromJson(MultipartFile file) {
        log.info("Starting JSON import for file: {}", file.getOriginalFilename());
        
        ObjectMapper objectMapper = new ObjectMapper();
        List<QuestionDTO> jsonData;
        try (InputStream inputStream = file.getInputStream()) {
            jsonData = objectMapper.readValue(inputStream, 
                    new TypeReference<List<QuestionDTO>>() {});
        } catch (IOException e) {
            log.error("Failed to read JSON file: {}", e.getMessage());
            throw new BusinessException("读取JSON文件失败: " + e.getMessage());
        }

        return processBatchImport(jsonData);
    }

    @Transactional
    public BatchImportResult importFromJsonList(List<QuestionDTO> requests) {
        log.info("Starting batch import from JSON list, count: {}", requests.size());
        return processBatchImport(requests);
    }

    private BatchImportResult processBatchImport(List<QuestionDTO> questions) {
        int total = questions.size();
        AtomicInteger successCount = new AtomicInteger(0);
        List<QuestionDTO> importedItems = Collections.synchronizedList(new ArrayList<>());
        List<String> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < total; i++) {
            QuestionDTO dto = questions.get(i);
            int rowNum = i + 1;
            
            try {
                if (dto.getContent() == null || dto.getContent().isBlank()) {
                    errors.add(String.format("第%d行: 题目不能为空", rowNum));
                    continue;
                }
                if (dto.getAnswer() == null || dto.getAnswer().isBlank()) {
                    errors.add(String.format("第%d行: 答案不能为空", rowNum));
                    continue;
                }
                if (dto.getKnowledgeId() == null || dto.getKnowledgeId().isBlank()) {
                    errors.add(String.format("第%d行: 知识点ID不能为空", rowNum));
                    continue;
                }

                QuestionDTO created = create(dto.getKnowledgeId(), dto.getContent(), dto.getAnswer(), dto.getType());
                importedItems.add(created);
                successCount.incrementAndGet();
                
            } catch (Exception e) {
                String errorMsg = String.format("第%d行: %s", rowNum, e.getMessage());
                errors.add(errorMsg);
                log.warn("Failed to import question at row {}: {}", rowNum, e.getMessage());
            }
        }

        log.info("Batch import completed. Total: {}, Success: {}, Failed: {}",
                total, successCount.get(), total - successCount.get());

        if (errors.isEmpty()) {
            return BatchImportResult.success(total, successCount.get(), importedItems);
        } else {
            return BatchImportResult.partial(total, successCount.get(), importedItems, errors);
        }
    }
}