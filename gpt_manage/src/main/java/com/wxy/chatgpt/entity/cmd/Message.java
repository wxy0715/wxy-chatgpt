package com.wxy.chatgpt.entity.cmd;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 消息
 * @author wangxingyu
 * @date 2023/03/26 20:40:04
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private String role;
    private String content;
}
