package com.wxy.chatgpt.controller;

import com.wxy.chatgpt.entity.QuestionQry;
import com.wxy.chatgpt.esdata.SaveQuestionCmd;
import com.wxy.chatgpt.service.IChatGptService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

    @PostMapping("/question")
    public List<SaveQuestionCmd> question(@RequestBody QuestionQry questionQry){
        return chatGptService.question(questionQry);
    }

    @PostMapping("/saveQuestion")
    public Long saveQuestion(@RequestBody SaveQuestionCmd saveQuestionCmd){
        return chatGptService.saveQuestion(saveQuestionCmd);
    }
}
