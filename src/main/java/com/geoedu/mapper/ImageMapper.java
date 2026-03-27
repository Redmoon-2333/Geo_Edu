package com.geoedu.mapper;

import com.geoedu.model.entity.Image;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

@Mapper
public interface ImageMapper {

    @Select("SELECT * FROM image WHERE path = #{path}")
    Optional<Image> findByPath(@Param("path") String path);

    @Insert("INSERT INTO image (id, path, original_name, file_size, mime_type, created_at) " +
            "VALUES (#{id}, #{path}, #{originalName}, #{fileSize}, #{mimeType}, #{createdAt})")
    int insert(Image image);
}
