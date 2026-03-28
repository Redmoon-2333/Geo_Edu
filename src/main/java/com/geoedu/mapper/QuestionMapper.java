package com.geoedu.mapper;

import com.geoedu.model.entity.Question;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuestionMapper {

    @Select("SELECT * FROM question WHERE id = #{id}")
    Question selectById(@Param("id") String id);

    @Select("SELECT * FROM question WHERE knowledge_id = #{knowledgeId}")
    List<Question> findByKnowledgeId(@Param("knowledgeId") String knowledgeId);

    @Select("SELECT * FROM question WHERE knowledge_id = #{knowledgeId} AND type = #{type}")
    List<Question> findByKnowledgeIdAndType(@Param("knowledgeId") String knowledgeId, @Param("type") String type);

    @Insert("INSERT INTO question (id, knowledge_id, question, answer, type, created_at) " +
            "VALUES (#{id}, #{knowledgeId}, #{question}, #{answer}, #{type}, #{createdAt})")
    int insert(Question question);

    @Update("UPDATE question SET question = #{question}, answer = #{answer}, type = #{type} WHERE id = #{id}")
    int update(Question question);

    @Delete("DELETE FROM question WHERE id = #{id}")
    int deleteById(@Param("id") String id);
}