package com.geoedu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyCode {

    private String id;
    private String phone;
    private String code;
    private String type;
    private LocalDateTime expireAt;
    private Boolean used;
    private LocalDateTime createdAt;
}
