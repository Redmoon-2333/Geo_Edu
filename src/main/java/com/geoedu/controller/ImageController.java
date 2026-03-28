package com.geoedu.controller;

import com.geoedu.model.dto.ApiResponse;
import com.geoedu.model.dto.ImageDTO;
import com.geoedu.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public ApiResponse<ImageDTO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String knowledgeId,
            @RequestParam(required = false, defaultValue = "0") int order) {

        ImageDTO dto = imageService.uploadImage(file, knowledgeId, order);
        return ApiResponse.success(dto);
    }
}