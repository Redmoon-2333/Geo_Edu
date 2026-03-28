package com.geoedu.service;

import com.geoedu.config.JwtTokenProvider;
import com.geoedu.exception.AuthenticationException;
import com.geoedu.exception.BusinessException;
import com.geoedu.exception.EntityNotFoundException;
import com.geoedu.mapper.UserMapper;
import com.geoedu.model.dto.*;
import com.geoedu.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("用户名或密码错误");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .expiresIn(jwtTokenProvider.getExpiration() / 1000)
                .user(toUserDTO(user))
                .build();
    }

    @Transactional
    public UserDTO register(RegisterRequest request) {
        if (userMapper.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(request.getUsername())
                .passwordHash(hashedPassword)
                .role(request.getRole() != null ? request.getRole() : "student")
                .createdAt(LocalDateTime.now())
                .build();

        userMapper.insert(user);
        log.info("New user registered: {}", user.getUsername());

        return toUserDTO(user);
    }

    public UserDTO getUserById(String userId) {
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
        return toUserDTO(user);
    }

    private UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
