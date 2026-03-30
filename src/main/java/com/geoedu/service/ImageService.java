package com.geoedu.service;

import com.geoedu.mapper.ImageMapper;
import com.geoedu.mapper.KnowledgeImageMapper;
import com.geoedu.model.dto.ImageDTO;
import com.geoedu.model.entity.Image;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageMapper imageMapper;
    private final KnowledgeImageMapper knowledgeImageMapper;

    @Value("${app.upload.path}")
    private String uploadPath;

    @Value("${app.upload.max-width:1920}")
    private int maxWidth;

    @Value("${app.upload.max-height:1080}")
    private int maxHeight;

    @Value("${app.upload.quality:0.85}")
    private float quality;

    @Value("${app.upload.thumbnail.enabled:true}")
    private boolean thumbnailEnabled;

    @Value("${app.upload.thumbnail.width:200}")
    private int thumbnailWidth;

    @Value("${app.upload.thumbnail.height:200}")
    private int thumbnailHeight;

    private static final Set<String> SUPPORTED_FORMATS = Set.of("jpg", "jpeg", "png", "gif", "bmp");

    @Transactional
    public ImageDTO uploadImage(MultipartFile file, String knowledgeId, int order) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        
        validateImageFormat(extension);

        Path uploadDir = Paths.get(uploadPath);
        ensureDirectoryExists(uploadDir);

        String baseFilename = String.format("%s_%d", 
                knowledgeId != null ? knowledgeId : UUID.randomUUID().toString().substring(0, 8), 
                order);
        
        String mainFilename = baseFilename + "." + extension;
        Path mainPath = uploadDir.resolve(mainFilename);

        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new RuntimeException("无法读取图片文件");
            }

            BufferedImage processedImage = resizeIfNeeded(originalImage);
            
            saveImage(processedImage, mainPath, extension);
            long mainFileSize = Files.size(mainPath);

            String thumbnailPath = null;
            if (thumbnailEnabled) {
                thumbnailPath = generateThumbnail(processedImage, uploadDir, baseFilename, extension);
            }

            String imageId = UUID.randomUUID().toString();
            Image image = Image.builder()
                    .id(imageId)
                    .path(mainPath.toString())
                    .originalName(originalFilename)
                    .fileSize(mainFileSize)
                    .mimeType(file.getContentType())
                    .createdAt(LocalDateTime.now())
                    .build();

            imageMapper.insert(image);

            if (knowledgeId != null) {
                knowledgeImageMapper.insert(knowledgeId, imageId, order);
            }

            log.info("Uploaded image: {} ({}x{}, {} bytes) for knowledge: {}", 
                    image.getId(), processedImage.getWidth(), processedImage.getHeight(), 
                    mainFileSize, knowledgeId);

            return ImageDTO.builder()
                    .id(image.getId())
                    .path(image.getPath())
                    .caption(originalFilename)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("图片处理失败: " + e.getMessage(), e);
        }
    }

    private BufferedImage resizeIfNeeded(BufferedImage original) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return original;
        }

        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        log.debug("Resizing image from {}x{} to {}x{}", originalWidth, originalHeight, newWidth, newHeight);

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        graphics.drawImage(original, 0, 0, newWidth, newHeight, null);
        graphics.dispose();

        return resized;
    }

    private String generateThumbnail(BufferedImage original, Path uploadDir, String baseFilename, String extension) {
        String thumbnailFilename = baseFilename + "_thumb." + extension;
        Path thumbnailPath = uploadDir.resolve(thumbnailFilename);

        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        double ratio = Math.min(
                (double) thumbnailWidth / originalWidth,
                (double) thumbnailHeight / originalHeight
        );

        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        BufferedImage thumbnail = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = thumbnail.createGraphics();
        
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        graphics.drawImage(original, 0, 0, newWidth, newHeight, null);
        graphics.dispose();

        try {
            saveImage(thumbnail, thumbnailPath, extension);
            log.debug("Generated thumbnail: {} ({}x{})", thumbnailFilename, newWidth, newHeight);
            return thumbnailPath.toString();
        } catch (IOException e) {
            log.warn("Failed to generate thumbnail: {}", e.getMessage());
            return null;
        }
    }

    private void saveImage(BufferedImage image, Path path, String format) throws IOException {
        if ("png".equalsIgnoreCase(format)) {
            ImageIO.write(image, "png", path.toFile());
        } else {
            BufferedImage rgbImage = new BufferedImage(
                    image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgbImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            ImageIO.write(rgbImage, "jpg", path.toFile());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private void validateImageFormat(String extension) {
        if (!SUPPORTED_FORMATS.contains(extension.toLowerCase())) {
            throw new RuntimeException("不支持的图片格式: " + extension + 
                    "。支持的格式: " + String.join(", ", SUPPORTED_FORMATS));
        }
    }

    private void ensureDirectoryExists(Path path) {
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException("无法创建上传目录: " + e.getMessage());
            }
        }
    }

    public void deleteImage(String imageId) {
        Image image = imageMapper.findById(imageId)
                .orElseThrow(() -> new RuntimeException("图片不存在: " + imageId));

        try {
            Path mainPath = Paths.get(image.getPath());
            Files.deleteIfExists(mainPath);

            String pathStr = image.getPath();
            String thumbnailPath = pathStr.replace(".", "_thumb.");
            Files.deleteIfExists(Paths.get(thumbnailPath));

            imageMapper.deleteById(imageId);
            log.info("Deleted image: {}", imageId);
        } catch (IOException e) {
            log.warn("Failed to delete image file: {}", e.getMessage());
        }
    }
}
