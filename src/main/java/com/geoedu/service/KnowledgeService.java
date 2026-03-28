package com.geoedu.service;

import com.geoedu.exception.EntityNotFoundException;
import com.geoedu.mapper.KnowledgeMapper;
import com.geoedu.model.dto.*;
import com.geoedu.model.entity.Knowledge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
