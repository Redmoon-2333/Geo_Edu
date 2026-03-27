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
    List<ChatLog> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId);

    @Select("SELECT * FROM chat_log WHERE created_at BETWEEN #{start} AND #{end}")
    List<ChatLog> findByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Insert("INSERT INTO chat_log (id, session_id, question, answer, retrieved_knowledge, response_time, user_id, created_at) " +
            "VALUES (#{id}, #{sessionId}, #{question}, #{answer}, #{retrievedKnowledge}, #{responseTime}, #{userId}, #{createdAt})")
    int insert(ChatLog chatLog);
}
