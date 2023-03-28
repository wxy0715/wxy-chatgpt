package com.wxy.chatgpt.controller;

import com.wxy.chatgpt.entity.cmd.SaveQuestionCmd;
import com.wxy.chatgpt.entity.out.QuestionOut;
import com.wxy.chatgpt.service.IChatGptService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 问答
 * @author wangxingyu
 * @date 2023/03/26 20:07:17
 */
@RestController
@RequestMapping("/api")
public class ChatGptController {
    @Resource
    private IChatGptService chatGptService;

    @GetMapping("/question")
    public QuestionOut question(@RequestParam String question){
        return chatGptService.question(question);
    }

    @PostMapping("/saveQuestion")
    public Long saveQuestion(@RequestBody SaveQuestionCmd saveQuestionCmd){
        return chatGptService.saveQuestion(saveQuestionCmd);
    }
}
