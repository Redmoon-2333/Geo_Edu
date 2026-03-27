package com.geoedu.util;

import com.geoedu.model.entity.Knowledge;

import java.util.List;

public class PromptBuilder {

    public static String buildRagPrompt(String question, List<Knowledge> retrieved) {
        StringBuilder knowledgeText = new StringBuilder();
        for (int i = 0; i < retrieved.size(); i++) {
            Knowledge k = retrieved.get(i);
            knowledgeText.append(String.format("%d. 【%s】%s - %s",
                    i + 1,
                    k.getTitle(),
                    k.getChapter() != null ? k.getChapter() : "",
                    k.getContent()));
            if (i < retrieved.size() - 1) {
                knowledgeText.append("\n\n");
            }
        }

        return String.format("""
## 角色设定
你是一位专业的中学地理教师，负责解答学生提出的地理问题。

## 知识参考
以下是相关的知识点供参考：
%s

## 回答要求
1. 回答控制在150字以内
2. 使用清晰的分点说明
3. 适当使用地理专业术语

## 学生问题
%s

## 回答""", knowledgeText.toString(), question);
    }
}