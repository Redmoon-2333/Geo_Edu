package com.geoedu.mapper;

import com.geoedu.model.entity.UserPracticeRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserPracticeRecordMapper {

    @Select("SELECT * FROM user_practice_record WHERE user_id = #{userId} ORDER BY created_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "questionId", column = "question_id"),
            @Result(property = "knowledgeId", column = "knowledge_id"),
            @Result(property = "userAnswer", column = "user_answer"),
            @Result(property = "isCorrect", column = "is_correct"),
            @Result(property = "difficulty", column = "difficulty"),
            @Result(property = "practiceType", column = "practice_type"),
            @Result(property = "timeTaken", column = "time_taken"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<UserPracticeRecord> findByUserId(@Param("userId") String userId);

    @Select("SELECT * FROM user_practice_record WHERE user_id = #{userId} AND knowledge_id = #{knowledgeId} ORDER BY created_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "questionId", column = "question_id"),
            @Result(property = "knowledgeId", column = "knowledge_id"),
            @Result(property = "userAnswer", column = "user_answer"),
            @Result(property = "isCorrect", column = "is_correct"),
            @Result(property = "difficulty", column = "difficulty"),
            @Result(property = "practiceType", column = "practice_type"),
            @Result(property = "timeTaken", column = "time_taken"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<UserPracticeRecord> findByUserIdAndKnowledgeId(@Param("userId") String userId, @Param("knowledgeId") String knowledgeId);

    @Select("SELECT * FROM user_practice_record WHERE user_id = #{userId} AND difficulty = #{difficulty} ORDER BY created_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "questionId", column = "question_id"),
            @Result(property = "knowledgeId", column = "knowledge_id"),
            @Result(property = "userAnswer", column = "user_answer"),
            @Result(property = "isCorrect", column = "is_correct"),
            @Result(property = "difficulty", column = "difficulty"),
            @Result(property = "practiceType", column = "practice_type"),
            @Result(property = "timeTaken", column = "time_taken"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<UserPracticeRecord> findByUserIdAndDifficulty(@Param("userId") String userId, @Param("difficulty") String difficulty);

    @Insert("INSERT INTO user_practice_record (id, user_id, question_id, knowledge_id, user_answer, is_correct, difficulty, practice_type, time_taken, created_at) " +
            "VALUES (#{id}, #{userId}, #{questionId}, #{knowledgeId}, #{userAnswer}, #{isCorrect}, #{difficulty}, #{practiceType}, #{timeTaken}, #{createdAt})")
    int insert(UserPracticeRecord record);

    @Delete("DELETE FROM user_practice_record WHERE id = #{id}")
    int deleteById(@Param("id") String id);
}
