package com.wxy.chatgpt.entity.out;

import com.google.api.Usage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * 聊天gpt api结果
 * @author wangxingyu
 * @date 2023/03/26 20:40:18
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChatGptApiOut {
    private String id;
    private String object;
    private Long created;
    private String model;
    private Usage usage;
    private List<ChoicesOut> choices;
}
