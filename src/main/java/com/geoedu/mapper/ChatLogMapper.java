package com.geoedu.mapper;

import com.geoedu.model.entity.ChatLog;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChatLogMapper {

    @Select("SELECT * FROM chat_log WHERE session_id = #{sessionId} ORDER BY created_at DESC")
    List<ChatLog> findBySessionIdOrderByCreatedAtDesc(@Param("sessionId") String sessionId);

    @Select("SELECT * FROM chat_log WHERE user_id = #{userId} ORDER BY created_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "sessionId", column = "session_id"),
            @Result(property = "question", column = "question"),
            @Result(property = "answer", column = "answer"),
            @Result(property = "retrievedKnowledge", column = "retrieved_knowledge"),
            @Result(property = "responseTime", column = "response_time"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "sessionType", column = "session_type"),
            @Result(property = "isFromErrorBook", column = "is_from_error_book")
    })
    List<ChatLog> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId);

    @Select("SELECT * FROM chat_log WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<ChatLog> findByUserId(@Param("userId") String userId);

    @Select("<script>" +
            "SELECT * FROM chat_log WHERE user_id = #{userId} " +
            "<if test='chapter != null'> AND retrieved_knowledge LIKE '%' || #{chapter} || '%' </if>" +
            "ORDER BY created_at DESC" +
            "</script>")
    List<ChatLog> findByUserIdWithChapter(@Param("userId") String userId, @Param("chapter") String chapter);

    @Select("SELECT * FROM chat_log WHERE created_at BETWEEN #{start} AND #{end}")
    List<ChatLog> findByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Insert("INSERT INTO chat_log (id, session_id, question, answer, retrieved_knowledge, response_time, user_id, created_at, session_type, is_from_error_book) " +
            "VALUES (#{id}, #{sessionId}, #{question}, #{answer}, #{retrievedKnowledge}, #{responseTime}, #{userId}, #{createdAt}, #{sessionType}, #{isFromErrorBook})")
    int insert(ChatLog chatLog);
}
