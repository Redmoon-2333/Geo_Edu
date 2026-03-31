package com.geoedu.mapper;

import com.geoedu.model.entity.Question;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuestionMapper {

    @Select("SELECT * FROM question WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "knowledgeId", column = "knowledge_id"),
            @Result(property = "question", column = "question"),
            @Result(property = "answer", column = "answer"),
            @Result(property = "type", column = "type"),
            @Result(property = "createdAt", column = "created_at")
    })
    Question selectById(@Param("id") String id);

    @Select("SELECT * FROM question WHERE knowledge_id = #{knowledgeId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "knowledgeId", column = "knowledge_id"),
            @Result(property = "question", column = "question"),
            @Result(property = "answer", column = "answer"),
            @Result(property = "type", column = "type"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<Question> findByKnowledgeId(@Param("knowledgeId") String knowledgeId);

    @Select("SELECT * FROM question WHERE knowledge_id = #{knowledgeId} AND type = #{type}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "knowledgeId", column = "knowledge_id"),
            @Result(property = "question", column = "question"),
            @Result(property = "answer", column = "answer"),
            @Result(property = "type", column = "type"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<Question> findByKnowledgeIdAndType(@Param("knowledgeId") String knowledgeId, @Param("type") String type);

    @Insert("INSERT INTO question (id, knowledge_id, question, answer, type, created_at) " +
            "VALUES (#{id}, #{knowledgeId}, #{question}, #{answer}, #{type}, #{createdAt})")
    int insert(Question question);

    @Update("UPDATE question SET question = #{question}, answer = #{answer}, type = #{type} WHERE id = #{id}")
    int update(Question question);

    @Delete("DELETE FROM question WHERE id = #{id}")
    int deleteById(@Param("id") String id);

    @Select("<script>" +
            "SELECT * FROM question WHERE knowledge_id IN " +
            "<foreach collection='knowledgeIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "ORDER BY created_at DESC LIMIT #{limit}" +
            "</script>")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "knowledgeId", column = "knowledge_id"),
            @Result(property = "question", column = "question"),
            @Result(property = "answer", column = "answer"),
            @Result(property = "type", column = "type"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<Question> findByKnowledgeIds(@Param("knowledgeIds") List<String> knowledgeIds, @Param("limit") int limit);

    @Select("SELECT * FROM question ORDER BY created_at DESC LIMIT #{limit}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "knowledgeId", column = "knowledge_id"),
            @Result(property = "question", column = "question"),
            @Result(property = "answer", column = "answer"),
            @Result(property = "type", column = "type"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<Question> findRecentQuestions(@Param("limit") int limit);

    @Select("<script>" +
            "SELECT * FROM question WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "knowledgeId", column = "knowledge_id"),
            @Result(property = "question", column = "question"),
            @Result(property = "answer", column = "answer"),
            @Result(property = "type", column = "type"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<Question> selectByIds(@Param("ids") List<String> ids);
}
