package com.wxy.chatgpt.entity.cmd;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 保存问题
 * @author wangxingyu
 * @date 2023/03/27 21:57:51
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SaveQuestionCmd {
    private String question;
    private String answer;
}
