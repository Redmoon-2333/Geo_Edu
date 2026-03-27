package com.geoedu.mapper;

import com.geoedu.model.entity.Knowledge;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface KnowledgeMapper {

    @Select("SELECT * FROM knowledge WHERE id = #{id}")
    Knowledge selectById(@Param("id") String id);

    @Select("SELECT * FROM knowledge")
    List<Knowledge> selectAll();

    List<Knowledge> selectAllByIds(@Param("ids") List<String> ids);

    @Select("SELECT * FROM knowledge WHERE grade = #{grade}")
    List<Knowledge> findByGrade(@Param("grade") String grade);

    @Select("SELECT * FROM knowledge WHERE grade = #{grade} AND chapter = #{chapter}")
    List<Knowledge> findByGradeAndChapter(@Param("grade") String grade, @Param("chapter") String chapter);

    @Select("SELECT * FROM knowledge WHERE difficulty = #{difficulty}")
    List<Knowledge> findByDifficulty(@Param("difficulty") String difficulty);

    @Select("SELECT * FROM knowledge WHERE grade = #{grade} AND chapter = #{chapter} AND difficulty = #{difficulty}")
    List<Knowledge> findByGradeAndChapterAndDifficulty(
            @Param("grade") String grade,
            @Param("chapter") String chapter,
            @Param("difficulty") String difficulty);

    @Select("SELECT * FROM knowledge WHERE LOWER(title) LIKE LOWER(CONCAT('%', #{keyword}, '%')) " +
            "OR LOWER(content) LIKE LOWER(CONCAT('%', #{keyword}, '%'))")
    List<Knowledge> searchByKeyword(@Param("keyword") String keyword);

    @Insert("INSERT INTO knowledge (id, title, content, difficulty, page_number, grade, chapter, created_at, updated_at) " +
            "VALUES (#{id}, #{title}, #{content}, #{difficulty}, #{pageNumber}, #{grade}, #{chapter}, #{createdAt}, #{updatedAt})")
    int insert(Knowledge knowledge);

    @Update("UPDATE knowledge SET title = #{title}, content = #{content}, difficulty = #{difficulty}, " +
            "page_number = #{pageNumber}, grade = #{grade}, chapter = #{chapter}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Knowledge knowledge);

    @Delete("DELETE FROM knowledge WHERE id = #{id}")
    int deleteById(@Param("id") String id);
}