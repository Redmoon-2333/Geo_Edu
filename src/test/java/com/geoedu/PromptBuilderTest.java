package com.geoedu;

import com.geoedu.model.entity.Knowledge;
import com.geoedu.util.PromptBuilder;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PromptBuilderTest {

    @Test
    void buildRagPrompt_WithMultipleKnowledge_ReturnsFormattedPrompt() {
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

        List<Knowledge> sampleKnowledgeList = Arrays.asList(knowledge1, knowledge2);
        
        String question = "地球是如何运动的？";
        String prompt = PromptBuilder.buildRagPrompt(question, sampleKnowledgeList);

        assertNotNull(prompt);
        assertTrue(prompt.contains("角色设定"));
        assertTrue(prompt.contains("知识参考"));
        assertTrue(prompt.contains("地球自转"));
        assertTrue(prompt.contains("地球公转"));
        assertTrue(prompt.contains("学生问题"));
        assertTrue(prompt.contains("回答要求"));
    }

    @Test
    void buildRagPrompt_WithEmptyKnowledge_ReturnsPromptWithoutReferences() {
        String question = "什么是地理？";
        String prompt = PromptBuilder.buildRagPrompt(question, Collections.emptyList());

        assertNotNull(prompt);
        assertTrue(prompt.contains("角色设定"));
        assertTrue(prompt.contains("学生问题"));
        assertTrue(prompt.contains("什么是地理？"));
    }

    @Test
    void buildRagPrompt_WithNullKnowledge_ReturnsPromptWithoutReferences() {
        String question = "什么是地理？";
        String prompt = PromptBuilder.buildRagPrompt(question, null);

        assertNotNull(prompt);
        assertTrue(prompt.contains("角色设定"));
        assertTrue(prompt.contains("学生问题"));
        assertTrue(prompt.contains("什么是地理？"));
    }

    @Test
    void buildRagPrompt_WithSingleKnowledge_FormatsCorrectly() {
        Knowledge knowledge1 = Knowledge.builder()
                .id("1")
                .title("地球自转")
                .content("地球绕地轴自西向东转动，周期为 24 小时")
                .difficulty("easy")
                .grade("grade1")
                .chapter("chapter1")
                .pageNumber(10)
                .build();

        List<Knowledge> singleKnowledge = Collections.singletonList(knowledge1);
        
        String question = "自转是什么？";
        String prompt = PromptBuilder.buildRagPrompt(question, singleKnowledge);

        assertNotNull(prompt);
        assertTrue(prompt.contains("1. 【地球自转】"));
        assertTrue(prompt.contains("地球绕地轴自西向东转动"));
    }
}
