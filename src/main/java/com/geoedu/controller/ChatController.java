package com.geoedu.controller;

import com.geoedu.mapper.ChatLogMapper;
import com.geoedu.model.dto.ApiResponse;
import com.geoedu.model.dto.ChatRequest;
import com.geoedu.model.dto.ChatResponse;
import com.geoedu.model.entity.ChatLog;
import com.geoedu.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication != null && authentication.getCredentials() != null
                ? authentication.getCredentials().toString()
                : "anonymous";

        ChatResponse response = chatService.chat(request, userId);
        return ApiResponse.success(response);
    }

    @GetMapping("/history")
    public ApiResponse<List<ChatLog>> getChatHistory(
            @RequestParam String userId,
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
