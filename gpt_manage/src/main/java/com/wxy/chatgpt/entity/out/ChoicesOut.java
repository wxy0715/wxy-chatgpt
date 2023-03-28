package com.wxy.chatgpt.entity.out;

import com.wxy.chatgpt.entity.cmd.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 返回结果
 * @author wangxingyu
 * @date 2023/03/26 20:41:01
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChoicesOut {
    private Message message;
    private int index;
    private String finish_reason;
}
