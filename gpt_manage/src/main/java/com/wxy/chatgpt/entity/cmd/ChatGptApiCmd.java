package com.wxy.chatgpt.entity.cmd;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author wangxingyu
 * @date 2023/03/26 20:39:38
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChatGptApiCmd {
    private String model = "gpt-3.5-turbo";
    private List<Message> messages;
    private double temperature = 1;
    private double top_p = 1;
    private int n = 1;
    private boolean stream = false;
    private String[] stop = null;
    private int max_tokens = 3072;
    private double presence_penalty = 0;
    private double frequency_penalty = 0;
}
