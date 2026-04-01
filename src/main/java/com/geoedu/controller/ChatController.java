package com.geoedu.controller;

import com.geoedu.mapper.ChatLogMapper;
import com.geoedu.model.dto.ApiResponse;
import com.geoedu.model.dto.ChatRequest;
import com.geoedu.model.dto.ChatResponse;
import com.geoedu.model.entity.ChatLog;
import com.geoedu.service.ChatService;
import com.geoedu.util.AuthenticationUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChatLogMapper chatLogMapper;

    @PostMapping
    public ApiResponse<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        String userId = AuthenticationUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            userId = "anonymous";
        }

        ChatResponse response = chatService.chat(request, userId);
        return ApiResponse.success(response);
    }

    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ChatLog>> getChatHistory(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String chapter) {

        String userId = AuthenticationUtils.getCurrentUserId();
        List<ChatLog> history;
        if (chapter != null && !chapter.isEmpty()) {
            history = chatLogMapper.findByUserIdWithChapter(userId, chapter);
        } else {
            history = chatLogMapper.findByUserId(userId);
        }
        return ApiResponse.success(history);
    }

    @GetMapping("/history/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ChatLog>> getChatHistoryByUserId(
            @PathVariable String userId,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String chapter) {

        List<ChatLog> history;
        if (chapter != null && !chapter.isEmpty()) {
            history = chatLogMapper.findByUserIdWithChapter(userId, chapter);
        } else {
            history = chatLogMapper.findByUserId(userId);
        }
        return ApiResponse.success(history);
    }
}
