package com.geoedu.mapper;

import com.geoedu.model.entity.UserErrorBook;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserErrorBookMapper {

    @Select("SELECT * FROM user_error_book WHERE user_id = #{userId} ORDER BY last_error_time DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "questionId", column = "question_id"),
            @Result(property = "errorCount", column = "error_count"),
            @Result(property = "lastErrorTime", column = "last_error_time"),
            @Result(property = "isFavorited", column = "is_favorited"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<UserErrorBook> findByUserId(@Param("userId") String userId);

    @Select("SELECT * FROM user_error_book WHERE user_id = #{userId} AND question_id = #{questionId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "questionId", column = "question_id"),
            @Result(property = "errorCount", column = "error_count"),
            @Result(property = "lastErrorTime", column = "last_error_time"),
            @Result(property = "isFavorited", column = "is_favorited"),
            @Result(property = "createdAt", column = "created_at")
    })
    UserErrorBook findByUserIdAndQuestionId(@Param("userId") String userId, @Param("questionId") String questionId);

    @Insert("INSERT INTO user_error_book (id, user_id, question_id, error_count, last_error_time, is_favorited, created_at) " +
            "VALUES (#{id}, #{userId}, #{questionId}, #{errorCount}, #{lastErrorTime}, #{isFavorited}, #{createdAt})")
    int insert(UserErrorBook errorBook);

    @Update("UPDATE user_error_book SET error_count = #{errorCount}, last_error_time = #{lastErrorTime}, is_favorited = #{isFavorited} WHERE id = #{id}")
    int update(UserErrorBook errorBook);

    @Update("UPDATE user_error_book SET is_favorited = #{isFavorited} WHERE id = #{id}")
    int updateFavoriteStatus(@Param("id") String id, @Param("isFavorited") Boolean isFavorited);

    @Delete("DELETE FROM user_error_book WHERE id = #{id}")
    int deleteById(@Param("id") String id);

    @Delete("DELETE FROM user_error_book WHERE user_id = #{userId} AND question_id = #{questionId}")
    int deleteByUserIdAndQuestionId(@Param("userId") String userId, @Param("questionId") String questionId);
}
