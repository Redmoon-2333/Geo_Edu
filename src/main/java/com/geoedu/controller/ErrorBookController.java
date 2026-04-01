package com.geoedu.controller;

import com.geoedu.model.dto.ApiResponse;
import com.geoedu.model.dto.ErrorBookDTO;
import com.geoedu.service.ErrorBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/error-book")
@RequiredArgsConstructor
public class ErrorBookController {

    private final ErrorBookService errorBookService;

    @GetMapping
    public ApiResponse<List<ErrorBookDTO>> getUserErrorBooks() {
        String userId = getCurrentUserId();
        List<ErrorBookDTO> errorBooks = errorBookService.getUserErrorBooks(userId);
        return ApiResponse.success(errorBooks);
    }

    @PostMapping("/{questionId}")
    public ApiResponse<ErrorBookDTO> addToErrorBook(
            @PathVariable String questionId,
            @RequestParam(defaultValue = "false") boolean isFavorited) {
        String userId = getCurrentUserId();
        ErrorBookDTO result = errorBookService.addToErrorBook(userId, questionId, isFavorited);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{questionId}")
    public ApiResponse<Boolean> removeFromErrorBook(@PathVariable String questionId) {
        String userId = getCurrentUserId();
        boolean result = errorBookService.removeFromErrorBook(userId, questionId);
        return ApiResponse.success(result);
    }

    @PatchMapping("/{questionId}/favorite")
    public ApiResponse<Boolean> updateFavoriteStatus(
            @PathVariable String questionId,
            @RequestParam boolean isFavorited) {
        String userId = getCurrentUserId();
        boolean result = errorBookService.updateFavoriteStatus(userId, questionId, isFavorited);
        return ApiResponse.success(result);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getCredentials() == null) {
            throw new RuntimeException("User not authenticated");
        }
        return authentication.getCredentials().toString();
    }
}
