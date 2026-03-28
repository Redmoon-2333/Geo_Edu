package com.geoedu;

import com.geoedu.config.JwtTokenProvider;
import com.geoedu.exception.AuthenticationException;
import com.geoedu.exception.BusinessException;
import com.geoedu.exception.EntityNotFoundException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id("1")
                .username("testuser")
                .passwordHash("hashedPassword")
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
        when(passwordEncoder.matches("correctPassword", "hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("1", "testuser", "student")).thenReturn("test-token");
        when(jwtTokenProvider.getExpiration()).thenReturn(86400000L);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("test-token", response.getToken());
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
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> authService.login(request));
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

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> authService.login(request));
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    void register_WithNewUsername_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .password("newpassword")
                .build();

        when(userMapper.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("newpassword")).thenReturn("hashedPassword");
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("2");
            return 1;
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

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.register(request));
        assertEquals("用户名已存在", exception.getMessage());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void getUserById_Success() {
        when(userMapper.findById("1")).thenReturn(Optional.of(sampleUser));

        UserDTO result = authService.getUserById("1");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("student", result.getRole());
    }

    @Test
    void getUserById_NotFound() {
        when(userMapper.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> authService.getUserById("nonexistent"));
    }
}
