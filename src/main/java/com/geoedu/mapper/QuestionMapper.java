package com.geoedu.mapper;

import com.geoedu.model.entity.Question;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuestionMapper {

    @Select("SELECT * FROM question WHERE knowledge_id = #{knowledgeId}")
    List<Question> findByKnowledgeId(@Param("knowledgeId") String knowledgeId);

    @Select("SELECT * FROM question WHERE knowledge_id = #{knowledgeId} AND type = #{type}")
    List<Question> findByKnowledgeIdAndType(@Param("knowledgeId") String knowledgeId, @Param("type") String type);
}
