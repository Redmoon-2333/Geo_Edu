package com.geoedu.service;

import com.geoedu.mapper.ChatLogMapper;
import com.geoedu.mapper.KnowledgeMapper;
import com.geoedu.model.dto.ChatRequest;
import com.geoedu.model.dto.ChatResponse;
import com.geoedu.model.dto.ImageDTO;
import com.geoedu.model.dto.KnowledgeBaseDTO;
import com.geoedu.model.entity.ChatLog;
import com.geoedu.model.entity.Knowledge;
import com.geoedu.util.PromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final VectorService vectorService;
    private final KnowledgeMapper knowledgeMapper;
    private final ChatLogMapper chatLogMapper;
    private final ChatClient chatClient;

    public ChatResponse chat(ChatRequest request, String userId) {
        String question = request.getQuestion();
        int topK = request.getTopK() != null ? request.getTopK() : 3;

        log.info("Processing chat request for user: {}, question: {}", userId, question);

        Map<String, String> filters = new HashMap<>();
        log.info("Searching similar knowledge with topK: {}", topK);
        List<VectorService.SearchResult> searchResults = vectorService.searchSimilar(question, topK, filters);
        log.info("Found {} similar results", searchResults != null ? searchResults.size() : 0);

        List<Knowledge> retrievedKnowledge = new ArrayList<>();
        if (!searchResults.isEmpty()) {
            List<String> knowledgeIds = searchResults.stream()
                    .map(VectorService.SearchResult::getKnowledgeId)
                    .collect(Collectors.toList());
            log.info("Retrieving knowledge by IDs: {}", knowledgeIds);
            retrievedKnowledge = knowledgeMapper.selectAllByIds(knowledgeIds);
            log.info("Retrieved {} knowledge items", retrievedKnowledge != null ? retrievedKnowledge.size() : 0);
        }

        String answer = callOllama(question, retrievedKnowledge);

        saveChatLog(userId, question, answer, retrievedKnowledge);

        return buildChatResponse(answer, retrievedKnowledge);
    }

    private String callOllama(String question, List<Knowledge> retrieved) {
        try {
            String prompt = PromptBuilder.buildRagPrompt(question, retrieved);
            log.info("Calling Ollama with prompt length: {}", prompt.length());
            log.debug("Prompt content: {}", prompt);
            
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            
            log.info("Ollama response received, length: {}", response != null ? response.length() : 0);
            return response;
        } catch (Exception e) {
            log.error("Failed to call Ollama: {}", e.getMessage(), e);
            throw new RuntimeException("调用 AI 服务失败：" + e.getMessage(), e);
        }
    }

    private void saveChatLog(String userId, String question, String answer, List<Knowledge> relatedKnowledge) {
        try {
            ChatLog chatLog = ChatLog.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .question(question)
                    .answer(answer)
                    .retrievedKnowledge(relatedKnowledge.stream()
                            .map(Knowledge::getId)
                            .collect(Collectors.joining(",")))
                    .build();
            chatLogMapper.insert(chatLog);
        } catch (Exception e) {
            log.warn("Failed to save chat log: {}", e.getMessage());
        }
    }

    private ChatResponse buildChatResponse(String answer, List<Knowledge> knowledgeList) {
        List<KnowledgeBaseDTO> relatedKnowledgeDTOs = new ArrayList<>();

        if (knowledgeList != null) {
            for (Knowledge k : knowledgeList) {
                KnowledgeBaseDTO dto = KnowledgeBaseDTO.builder()
                        .id(k.getId())
                        .title(k.getTitle())
                        .content(k.getContent())
                        .difficulty(k.getDifficulty())
                        .grade(k.getGrade())
                        .chapter(k.getChapter())
                        .build();
                relatedKnowledgeDTOs.add(dto);
            }
        }

        return ChatResponse.builder()
                .answer(answer)
                .images(Collections.emptyList())
                .relatedKnowledge(relatedKnowledgeDTOs)
                .relatedQuestions(new ArrayList<>())
                .build();
    }
}
