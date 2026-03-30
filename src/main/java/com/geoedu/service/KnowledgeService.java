package com.geoedu.service;

import com.alibaba.excel.EasyExcel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geoedu.exception.EntityNotFoundException;
import com.geoedu.mapper.KnowledgeMapper;
import com.geoedu.model.dto.*;
import com.geoedu.model.entity.Knowledge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeService {

    private final KnowledgeMapper knowledgeMapper;
    private final VectorService vectorService;
    private final VectorStore vectorStore;

    @Transactional
    public KnowledgeDTO create(KnowledgeCreateRequest request) {
        Knowledge knowledge = Knowledge.builder()
                .id(request.getId())
                .title(request.getTitle())
                .content(request.getContent())
                .difficulty(request.getDifficulty() != null ? request.getDifficulty() : "medium")
                .pageNumber(request.getPageNumber())
                .grade(request.getGrade())
                .chapter(request.getChapter())
                .build();

        knowledgeMapper.insert(knowledge);

        generateAndSaveVector(knowledge);

        return toDTO(knowledge);
    }

    @Transactional
    public KnowledgeDTO update(String id, KnowledgeUpdateRequest request) {
        Knowledge knowledge = knowledgeMapper.selectById(id);
        if (knowledge == null) {
            throw new EntityNotFoundException("Knowledge", id);
        }

        if (request.getTitle() != null) {
            knowledge.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            knowledge.setContent(request.getContent());
        }
        if (request.getDifficulty() != null) {
            knowledge.setDifficulty(request.getDifficulty());
        }
        if (request.getPageNumber() != null) {
            knowledge.setPageNumber(request.getPageNumber());
        }

        knowledgeMapper.update(knowledge);

        generateAndSaveVector(knowledge);

        return toDTO(knowledge);
    }

    @Transactional
    public void delete(String id) {
        if (knowledgeMapper.selectById(id) == null) {
            throw new EntityNotFoundException("Knowledge", id);
        }
        knowledgeMapper.deleteById(id);
        vectorStore.delete(List.of(id));
        log.info("Deleted knowledge and its vector: {}", id);
    }

    public KnowledgeDTO getById(String id) {
        Knowledge knowledge = knowledgeMapper.selectById(id);
        if (knowledge == null) {
            throw new EntityNotFoundException("Knowledge", id);
        }
        return toDTO(knowledge);
    }

    public PageResponse<KnowledgeDTO> list(int page, int size, String grade, String chapter, String difficulty) {
        int offset = page * size;
        List<Knowledge> knowledgeList = knowledgeMapper.selectByConditions(grade, chapter, difficulty, offset, size);
        long total = knowledgeMapper.countByConditions(grade, chapter, difficulty);

        List<KnowledgeDTO> items = knowledgeList.stream()
                .map(this::toDTO)
                .toList();

        return PageResponse.<KnowledgeDTO>builder()
                .items(items)
                .total(total)
                .page(page)
                .size(size)
                .pages((int) Math.ceil((double) total / size))
                .build();
    }

    public PageResponse<KnowledgeDTO> search(String keyword, int page, int size) {
        int offset = page * size;
        List<Knowledge> knowledgeList = knowledgeMapper.searchByKeywordPaged(keyword, offset, size);
        long total = knowledgeMapper.countByKeyword(keyword);

        List<KnowledgeDTO> items = knowledgeList.stream()
                .map(this::toDTO)
                .toList();

        return PageResponse.<KnowledgeDTO>builder()
                .items(items)
                .total(total)
                .page(page)
                .size(size)
                .pages((int) Math.ceil((double) total / size))
                .build();
    }

    private void generateAndSaveVector(Knowledge knowledge) {
        try {
            String textToEmbed = knowledge.getTitle() + " " + knowledge.getContent();
            float[] vector = vectorService.embedText(textToEmbed);

            Map<String, String> metadata = new HashMap<>();
            metadata.put("title", knowledge.getTitle());
            metadata.put("grade", knowledge.getGrade() != null ? knowledge.getGrade() : "");
            metadata.put("chapter", knowledge.getChapter() != null ? knowledge.getChapter() : "");
            metadata.put("difficulty", knowledge.getDifficulty() != null ? knowledge.getDifficulty() : "");

            vectorService.saveVector(knowledge.getId(), vector, metadata);
            log.debug("Generated and saved vector for knowledge: {}", knowledge.getId());
        } catch (Exception e) {
            log.error("Failed to generate vector for knowledge {}: {}", knowledge.getId(), e.getMessage());
        }
    }

    private KnowledgeDTO toDTO(Knowledge knowledge) {
        return KnowledgeDTO.builder()
                .id(knowledge.getId())
                .title(knowledge.getTitle())
                .content(knowledge.getContent())
                .difficulty(knowledge.getDifficulty())
                .pageNumber(knowledge.getPageNumber())
                .grade(knowledge.getGrade())
                .chapter(knowledge.getChapter())
                .build();
    }

    public void generateMissingVectors() {
        log.info("Starting to check and generate missing vectors...");
        List<Knowledge> allKnowledge = knowledgeMapper.selectAll();

        int generatedCount = 0;
        for (Knowledge knowledge : allKnowledge) {
            try {
                generateAndSaveVector(knowledge);
                generatedCount++;
                log.debug("Generated vector for knowledge: {}", knowledge.getId());
            } catch (Exception e) {
                log.error("Failed to generate vector for knowledge {}: {}", knowledge.getId(), e.getMessage());
            }
        }

        log.info("Vector generation complete. Generated {} new vectors out of {} total knowledge entries.",
            generatedCount, allKnowledge.size());
    }

    public void regenerateAllVectors() {
        log.info("Starting to regenerate all vectors...");
        List<Knowledge> allKnowledge = knowledgeMapper.selectAll();

        int successCount = 0;
        for (Knowledge knowledge : allKnowledge) {
            try {
                generateAndSaveVector(knowledge);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate vector for knowledge {}: {}", knowledge.getId(), e.getMessage());
            }
        }

        log.info("Vector regeneration complete. Successfully regenerated {} vectors out of {} total.",
            successCount, allKnowledge.size());
    }

    @Transactional
    public BatchImportResult importFromExcel(MultipartFile file) {
        log.info("Starting Excel import for file: {}", file.getOriginalFilename());
        
        List<KnowledgeExcelDTO> excelData;
        try (InputStream inputStream = file.getInputStream()) {
            excelData = EasyExcel.read(inputStream)
                    .head(KnowledgeExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("Failed to read Excel file: {}", e.getMessage());
            throw new RuntimeException("读取Excel文件失败: " + e.getMessage());
        }

        return processBatchImport(excelData);
    }

    @Transactional
    public BatchImportResult importFromJson(MultipartFile file) {
        log.info("Starting JSON import for file: {}", file.getOriginalFilename());
        
        ObjectMapper objectMapper = new ObjectMapper();
        List<KnowledgeCreateRequest> jsonData;
        try (InputStream inputStream = file.getInputStream()) {
            jsonData = objectMapper.readValue(inputStream, 
                    new TypeReference<List<KnowledgeCreateRequest>>() {});
        } catch (IOException e) {
            log.error("Failed to read JSON file: {}", e.getMessage());
            throw new RuntimeException("读取JSON文件失败: " + e.getMessage());
        }

        return processBatchImportFromRequest(jsonData);
    }

    @Transactional
    public BatchImportResult importFromJsonList(List<KnowledgeCreateRequest> requests) {
        log.info("Starting batch import from JSON list, count: {}", requests.size());
        return processBatchImportFromRequest(requests);
    }

    private BatchImportResult processBatchImport(List<KnowledgeExcelDTO> excelData) {
        List<KnowledgeCreateRequest> requests = excelData.stream()
                .map(this::convertToRequest)
                .toList();
        return processBatchImportFromRequest(requests);
    }

    private KnowledgeCreateRequest convertToRequest(KnowledgeExcelDTO dto) {
        String id = generateId(dto.getGrade(), dto.getChapter(), dto.getTitle());
        return KnowledgeCreateRequest.builder()
                .id(id)
                .title(dto.getTitle())
                .content(dto.getContent())
                .difficulty(dto.getDifficulty() != null ? dto.getDifficulty() : "medium")
                .pageNumber(dto.getPageNumber())
                .grade(dto.getGrade())
                .chapter(dto.getChapter())
                .build();
    }

    private BatchImportResult processBatchImportFromRequest(List<KnowledgeCreateRequest> requests) {
        int total = requests.size();
        AtomicInteger successCount = new AtomicInteger(0);
        List<KnowledgeDTO> importedItems = Collections.synchronizedList(new ArrayList<>());
        List<String> errors = Collections.synchronizedList(new ArrayList<>());

        int batchSize = 100;
        for (int i = 0; i < total; i += batchSize) {
            int end = Math.min(i + batchSize, total);
            List<KnowledgeCreateRequest> batch = requests.subList(i, end);
            
            for (int j = 0; j < batch.size(); j++) {
                KnowledgeCreateRequest request = batch.get(j);
                int rowNum = i + j + 1;
                
                try {
                    if (request.getTitle() == null || request.getTitle().isBlank()) {
                        errors.add(String.format("第%d行: 标题不能为空", rowNum));
                        continue;
                    }
                    if (request.getContent() == null || request.getContent().isBlank()) {
                        errors.add(String.format("第%d行: 内容不能为空", rowNum));
                        continue;
                    }

                    if (request.getId() == null || request.getId().isBlank()) {
                        request.setId(generateId(request.getGrade(), request.getChapter(), request.getTitle()));
                    }

                    Knowledge knowledge = Knowledge.builder()
                            .id(request.getId())
                            .title(request.getTitle())
                            .content(request.getContent())
                            .difficulty(request.getDifficulty() != null ? request.getDifficulty() : "medium")
                            .pageNumber(request.getPageNumber())
                            .grade(request.getGrade())
                            .chapter(request.getChapter())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    knowledgeMapper.insert(knowledge);
                    
                    generateAndSaveVector(knowledge);
                    
                    importedItems.add(toDTO(knowledge));
                    successCount.incrementAndGet();
                    
                } catch (Exception e) {
                    String errorMsg = String.format("第%d行: %s", rowNum, e.getMessage());
                    errors.add(errorMsg);
                    log.warn("Failed to import knowledge at row {}: {}", rowNum, e.getMessage());
                }
            }
            
            log.info("Batch import progress: {}/{}", Math.min(end, total), total);
        }

        log.info("Batch import completed. Total: {}, Success: {}, Failed: {}", 
                total, successCount.get(), total - successCount.get());

        if (errors.isEmpty()) {
            return BatchImportResult.success(total, successCount.get(), importedItems);
        } else {
            return BatchImportResult.partial(total, successCount.get(), importedItems, errors);
        }
    }

    private String generateId(String grade, String chapter, String title) {
        StringBuilder sb = new StringBuilder();
        
        if (grade != null && !grade.isBlank()) {
            sb.append(grade.replaceAll("[^a-zA-Z0-9]", "")).append("-");
        } else {
            sb.append("G-");
        }
        
        if (chapter != null && !chapter.isBlank()) {
            sb.append(chapter.replaceAll("[^a-zA-Z0-9]", "")).append("-");
        }
        
        if (title != null && !title.isBlank()) {
            String titlePart = title.length() > 20 ? title.substring(0, 20) : title;
            sb.append(titlePart.replaceAll("[^a-zA-Z0-9\u4e00-\u9fa5]", ""));
        }
        
        sb.append("-").append(System.currentTimeMillis() % 10000);
        
        return sb.toString();
    }
}
