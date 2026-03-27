package com.geoedu;

import com.geoedu.mapper.ChatLogMapper;
import com.geoedu.mapper.KnowledgeMapper;
import com.geoedu.model.entity.Knowledge;
import com.geoedu.service.ChatService;
import com.geoedu.service.VectorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PromptBuilderTest {

    @Mock
    private VectorService vectorService;

    @Mock
    private KnowledgeMapper knowledgeMapper;

    @Mock
    private ChatLogMapper chatLogMapper;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ChatService chatService;

    private List<Knowledge> sampleKnowledgeList;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(chatService, "ollamaBaseUrl", "http://localhost:11434");
        ReflectionTestUtils.setField(chatService, "ollamaModel", "llama3");

        Knowledge knowledge1 = Knowledge.builder()
                .id("1")
                .title("地球自转")
                .content("地球绕地轴自西向东转动，周期为 24 小时")
                .difficulty("easy")
                .grade("grade1")
                .chapter("chapter1")
                .pageNumber(10)
                .build();

        Knowledge knowledge2 = Knowledge.builder()
                .id("2")
                .title("地球公转")
                .content("地球绕太阳转动，周期为一年")
                .difficulty("medium")
                .grade("grade1")
                .chapter("chapter2")
                .pageNumber(20)
                .build();

        sampleKnowledgeList = Arrays.asList(knowledge1, knowledge2);
    }

    @Test
    void buildPrompt_WithMultipleKnowledge_ReturnsFormattedPrompt() {
        String question = "地球是如何运动的？";
        String prompt = chatService.buildPrompt(question, sampleKnowledgeList);

        assertNotNull(prompt);
        assertTrue(prompt.contains("你是一个地理教育助手"));
        assertTrue(prompt.contains("参考知识"));
        assertTrue(prompt.contains("地球自转"));
        assertTrue(prompt.contains("地球公转"));
        assertTrue(prompt.contains("[1]"));
        assertTrue(prompt.contains("[2]"));
        assertTrue(prompt.contains("年级:grade1"));
        assertTrue(prompt.contains("章节:chapter1"));
        assertTrue(prompt.contains("难度:easy"));
        assertTrue(prompt.contains("用户问题：地球是如何运动的？"));
        assertTrue(prompt.contains("请基于参考知识给出准确、详细的回答"));
    }

    @Test
    void buildPrompt_WithEmptyKnowledge_ReturnsPromptWithoutReferences() {
        String question = "什么是地理？";
        String prompt = chatService.buildPrompt(question, Collections.emptyList());

        assertNotNull(prompt);
        assertTrue(prompt.contains("你是一个地理教育助手"));
        assertFalse(prompt.contains("参考知识"));
        assertTrue(prompt.contains("用户问题：什么是地理？"));
    }

    @Test
    void buildPrompt_WithNullKnowledge_ReturnsPromptWithoutReferences() {
        String question = "什么是地理？";
        String prompt = chatService.buildPrompt(question, null);

        assertNotNull(prompt);
        assertTrue(prompt.contains("你是一个地理教育助手"));
        assertFalse(prompt.contains("参考知识"));
        assertTrue(prompt.contains("用户问题：什么是地理？"));
    }

    @Test
    void buildPrompt_WithSingleKnowledge_FormatsCorrectly() {
        String question = "自转是什么？";
        List<Knowledge> singleKnowledge = Collections.singletonList(sampleKnowledgeList.get(0));
        String prompt = chatService.buildPrompt(question, singleKnowledge);

        assertNotNull(prompt);
        assertTrue(prompt.contains("[1]"));
        assertFalse(prompt.contains("[2]"));
        assertTrue(prompt.contains("地球自转"));
        assertTrue(prompt.contains("地球绕地轴自西向东转动，周期为 24 小时"));
    }

    @Test
    void buildPrompt_ContainsAllRequiredSections() {
        String question = "测试问题";
        String prompt = chatService.buildPrompt(question, sampleKnowledgeList);

        String[] requiredSections = {
                "你是一个地理教育助手",
                "参考知识",
                "[1]",
                "地球自转",
                "[2]",
                "地球公转",
                "用户问题：测试问题",
                "请基于参考知识给出准确、详细的回答"
        };

        for (String section : requiredSections) {
            assertTrue(prompt.contains(section), "Prompt should contain: " + section);
        }
    }
}
