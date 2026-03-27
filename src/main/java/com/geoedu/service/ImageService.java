package com.geoedu.service;

import com.geoedu.mapper.ImageMapper;
import com.geoedu.model.dto.ImageDTO;
import com.geoedu.model.entity.Image;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageMapper imageMapper;

    @Value("${app.upload.path}")
    private String uploadPath;

    @Transactional
    public ImageDTO uploadImage(MultipartFile file, String knowledgeId, int order) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFilename = String.format("%s_%d%s", knowledgeId, order, extension);

        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            try {
                Files.createDirectories(uploadDir);
            } catch (IOException e) {
                throw new RuntimeException("无法创建上传目录: " + e.getMessage());
            }
        }

        Path targetPath = uploadDir.resolve(uniqueFilename);
        try {
            file.transferTo(targetPath);
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败: " + e.getMessage());
        }

        String imageId = UUID.randomUUID().toString();
        Image image = Image.builder()
                .id(imageId)
                .path(targetPath.toString())
                .originalName(originalFilename)
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .createdAt(LocalDateTime.now())
                .build();

        imageMapper.insert(image);
        log.info("Uploaded image: {} for knowledge: {}", image.getId(), knowledgeId);

        return ImageDTO.builder()
                .id(image.getId())
                .path(image.getPath())
                .caption("")
                .build();
    }
}
