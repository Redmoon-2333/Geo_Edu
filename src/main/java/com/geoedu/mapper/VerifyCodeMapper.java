package com.geoedu.mapper;

import com.geoedu.model.entity.VerifyCode;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Mapper
public interface VerifyCodeMapper {

    @Select("SELECT * FROM verify_code WHERE phone = #{phone} AND type = #{type} AND used = false AND expire_at > #{now} ORDER BY created_at DESC LIMIT 1")
    Optional<VerifyCode> findByPhoneAndTypeAndUsedFalseAndExpireAtAfter(
            @Param("phone") String phone,
            @Param("type") String type,
            @Param("now") LocalDateTime now);

    @Update("UPDATE verify_code SET used = true WHERE phone = #{phone} AND type = #{type} AND used = false")
    int markAsUsed(@Param("phone") String phone, @Param("type") String type);
}
