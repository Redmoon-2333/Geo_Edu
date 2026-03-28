package com.geoedu.mapper;

import com.geoedu.model.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

@Mapper
public interface UserMapper {

    @Select("SELECT COUNT(*) > 0 FROM users WHERE username = #{username}")
    boolean existsByUsername(@Param("username") String username);

    @Insert("INSERT INTO users (id, username, password_hash, role, created_at) " +
            "VALUES (#{id}, #{username}, #{passwordHash}, #{role}, #{createdAt})")
    int insert(User user);

    Optional<User> findByUsername(@Param("username") String username);

    Optional<User> findById(@Param("id") String id);
}
