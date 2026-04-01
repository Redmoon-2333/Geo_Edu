package com.geoedu.controller;

import com.geoedu.mapper.KnowledgeImageMapper;
import com.geoedu.model.dto.*;
import com.geoedu.model.entity.Image;
import com.geoedu.service.KnowledgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/knowledge")
@RequiredArgsConstructor
@Slf4j
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    private final KnowledgeImageMapper knowledgeImageMapper;

    @GetMapping
    public ApiResponse<PageResponse<KnowledgeDTO>> list(
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String chapter,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<KnowledgeDTO> response = knowledgeService.list(page, size, grade, chapter, difficulty);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgeDTO> getById(@PathVariable String id) {
        KnowledgeDTO dto = knowledgeService.getById(id);
        return ApiResponse.success(dto);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<KnowledgeDTO> create(@Valid @RequestBody KnowledgeCreateRequest request) {
        KnowledgeDTO dto = knowledgeService.create(request);
        return ApiResponse.success(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<KnowledgeDTO> update(
            @PathVariable String id,
            @Valid @RequestBody KnowledgeUpdateRequest request) {
        KnowledgeDTO dto = knowledgeService.update(id, request);
        return ApiResponse.success(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable String id) {
        knowledgeService.delete(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<KnowledgeDTO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<KnowledgeDTO> response = knowledgeService.search(keyword, page, size);
        return ApiResponse.success(response);
    }

    @PostMapping("/batch/excel")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<BatchImportResult> importFromExcel(
            @RequestParam("file") MultipartFile file) {

        BatchImportResult result = knowledgeService.importFromExcel(file);
        return ApiResponse.success(result);
    }

    @PostMapping("/batch/json")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<BatchImportResult> importFromJson(
            @RequestParam("file") MultipartFile file) {

        BatchImportResult result = knowledgeService.importFromJson(file);
        return ApiResponse.success(result);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<BatchImportResult> importFromJsonList(
            @RequestBody List<KnowledgeCreateRequest> requests) {

        BatchImportResult result = knowledgeService.importFromJsonList(requests);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}/images")
    public ApiResponse<List<ImageDTO>> getKnowledgeImages(@PathVariable String id) {
        List<Image> images = knowledgeImageMapper.findImagesByKnowledgeId(id);
        List<ImageDTO> imageDTOs = images.stream()
                .map(img -> ImageDTO.builder()
                        .id(img.getId())
                        .path(img.getPath())
                        .caption(img.getOriginalName())
                        .build())
                .collect(Collectors.toList());
        return ApiResponse.success(imageDTOs);
    }

    @PostMapping("/{id}/images")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<Void> bindImageToKnowledge(
            @PathVariable String id,
            @RequestBody KnowledgeImageBindRequest request) {
        try {
            knowledgeImageMapper.insert(id, request.getImageId(), request.getDisplayOrder());
            log.info("Bound image {} to knowledge {}", request.getImageId(), id);
            return ApiResponse.success(null);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            log.warn("Image {} already bound to knowledge {}", request.getImageId(), id);
            return ApiResponse.error(400, "图片已绑定到该知识点");
        }
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<Void> unbindImageFromKnowledge(
            @PathVariable String id,
            @PathVariable String imageId) {
        knowledgeImageMapper.delete(id, imageId);
        log.info("Unbound image {} from knowledge {}", imageId, id);
        return ApiResponse.success(null);
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class KnowledgeImageBindRequest {
        private String imageId;
        private Integer displayOrder;
    }
}
