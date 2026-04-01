package com.geoedu.mapper;

import com.geoedu.model.entity.Image;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface KnowledgeImageMapper {

    @Select("SELECT i.* FROM image i " +
            "INNER JOIN knowledge_image ki ON i.id = ki.image_id " +
            "WHERE ki.knowledge_id = #{knowledgeId} " +
            "ORDER BY ki.display_order ASC")
    List<Image> findImagesByKnowledgeId(@Param("knowledgeId") String knowledgeId);

    @Select("<script>" +
            "SELECT i.* FROM image i " +
            "INNER JOIN knowledge_image ki ON i.id = ki.image_id " +
            "WHERE ki.knowledge_id IN " +
            "<foreach collection='knowledgeIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "ORDER BY ki.knowledge_id, ki.display_order ASC" +
            "</script>")
    List<Image> findImagesByKnowledgeIds(@Param("knowledgeIds") List<String> knowledgeIds);

    @Insert("INSERT INTO knowledge_image (knowledge_id, image_id, display_order) " +
            "VALUES (#{knowledgeId}, #{imageId}, #{displayOrder})")
    int insert(@Param("knowledgeId") String knowledgeId, 
               @Param("imageId") String imageId, 
               @Param("displayOrder") Integer displayOrder);

    @Delete("DELETE FROM knowledge_image WHERE knowledge_id = #{knowledgeId}")
    int deleteByKnowledgeId(@Param("knowledgeId") String knowledgeId);

    @Delete("DELETE FROM knowledge_image WHERE knowledge_id = #{knowledgeId} AND image_id = #{imageId}")
    int delete(@Param("knowledgeId") String knowledgeId, @Param("imageId") String imageId);
}
