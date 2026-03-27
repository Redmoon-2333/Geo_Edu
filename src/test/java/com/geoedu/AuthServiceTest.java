package com.geoedu;

import com.geoedu.mapper.UserMapper;
import com.geoedu.model.dto.LoginRequest;
import com.geoedu.model.dto.LoginResponse;
import com.geoedu.model.dto.RegisterRequest;
import com.geoedu.model.dto.UserDTO;
import com.geoedu.model.entity.User;
import com.geoedu.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private BCryptPasswordEncoder passwordEncoder;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        ReflectionTestUtils.setField(authService, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(authService, "jwtSecret", "test-secret-key-that-is-at-least-256-bits-long-for-hs256");
        ReflectionTestUtils.setField(authService, "jwtExpiration", 86400000L);

        sampleUser = User.builder()
                .id("1")
                .username("testuser")
                .passwordHash(passwordEncoder.encode("correctPassword"))
                .role("student")
                .build();
    }

    @Test
    void login_WithCorrectCredentials_Success() {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("correctPassword")
                .build();

        when(userMapper.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertTrue(response.getExpiresIn() > 0);
        assertEquals("testuser", response.getUser().getUsername());
        assertEquals("student", response.getUser().getRole());
        verify(userMapper, times(1)).findByUsername("testuser");
    }

    @Test
    void login_WithWrongPassword_ThrowsException() {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("wrongPassword")
                .build();

        when(userMapper.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("用户名或密码错误", exception.getMessage());
        verify(userMapper, times(1)).findByUsername("testuser");
    }

    @Test
    void login_WithNonexistentUser_ThrowsException() {
        LoginRequest request = LoginRequest.builder()
                .username("nonexistent")
                .password("anyPassword")
                .build();

        when(userMapper.findByUsername("nonexistent")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    void register_WithNewUsername_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .password("newpassword")
                .build();

        when(userMapper.existsByUsername("newuser")).thenReturn(false);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("2");
            return user;
        }).when(userMapper).insert(any(User.class));

        UserDTO result = authService.register(request);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("student", result.getRole());
        verify(userMapper, times(1)).existsByUsername("newuser");
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    void register_WithExistingUsername_ThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .password("password")
                .build();

        when(userMapper.existsByUsername("existinguser")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(request));
        assertEquals("用户名已存在", exception.getMessage());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void getUserById_Success() {
        when(userMapper.selectOneById("1")).thenReturn(sampleUser);

        UserDTO result = authService.getUserById("1");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("student", result.getRole());
    }

    @Test
    void getUserById_NotFound() {
        when(userMapper.selectOneById("nonexistent")).thenReturn(null);

        assertThrows(RuntimeException.class, () -> authService.getUserById("nonexistent"));
    }
}
