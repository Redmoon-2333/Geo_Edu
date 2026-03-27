package com.geoedu.service;

import com.geoedu.mapper.ChatLogMapper;
import com.geoedu.mapper.KnowledgeMapper;
import com.geoedu.model.dto.ChatRequest;
import com.geoedu.model.dto.ChatResponse;
import com.geoedu.model.dto.ImageDTO;
import com.geoedu.model.dto.KnowledgeBaseDTO;
import com.geoedu.model.entity.ChatLog;
import com.geoedu.model.entity.Knowledge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final VectorService vectorService;
    private final KnowledgeMapper knowledgeMapper;
    private final ChatLogMapper chatLogMapper;
    private final RestTemplate restTemplate;

    @Value("${ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${ollama.model}")
    private String ollamaModel;

    public ChatResponse chat(ChatRequest request, String userId) {
        String question = request.getQuestion();
        int topK = request.getTopK() != null ? request.getTopK() : 3;

        float[] questionVector = vectorService.embedText(question);

        Map<String, String> filters = new HashMap<>();
        List<VectorService.SearchResult> searchResults = vectorService.searchSimilar(questionVector, topK, filters);

        List<Knowledge> retrievedKnowledge = new ArrayList<>();
        if (!searchResults.isEmpty()) {
            List<String> knowledgeIds = searchResults.stream()
                    .map(VectorService.SearchResult::getKnowledgeId)
                    .collect(Collectors.toList());
            retrievedKnowledge = knowledgeMapper.selectAllByIds(knowledgeIds);
        }

        String prompt = buildPrompt(question, retrievedKnowledge);

        String answer = callOllama(prompt);

        saveChatLog(userId, question, answer, retrievedKnowledge);

        return buildChatResponse(answer, retrievedKnowledge);
    }

    public String buildPrompt(String question, List<Knowledge> retrieved) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("你是一个地理教育助手。请根据以下参考知识回答用户的问题。\n\n");

        if (retrieved != null && !retrieved.isEmpty()) {
            promptBuilder.append("参考知识：\n");
            for (int i = 0; i < retrieved.size(); i++) {
                Knowledge k = retrieved.get(i);
                promptBuilder.append(String.format("[%d] %s (年级:%s, 章节:%s, 难度:%s)\n%s\n\n",
                        i + 1, k.getTitle(), k.getGrade(), k.getChapter(), k.getDifficulty(), k.getContent()));
            }
        }

        promptBuilder.append("用户问题：").append(question).append("\n\n");
        promptBuilder.append("请基于参考知识给出准确、详细的回答。如果参考知识不足以回答，请说明。");

        return promptBuilder.toString();
    }

    private String callOllama(String prompt) {
        String url = ollamaBaseUrl + "/api/generate";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", ollamaModel);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);

        Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);

        if (response == null || !response.containsKey("response")) {
            throw new RuntimeException("Failed to get response from Ollama");
        }

        return (String) response.get("response");
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
        List<ImageDTO> images = new ArrayList<>();

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
                .images(images)
                .relatedKnowledge(relatedKnowledgeDTOs)
                .relatedQuestions(new ArrayList<>())
                .build();
    }
}
