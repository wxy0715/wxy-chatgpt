package com.wxy.chatgpt.entity.cmd;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 嵌入api参数
 * @author wangxingyu
 * @date 2023/03/26 20:37:43
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmbeddingsApiCmd {
    private String model = "text-embedding-ada-002";
    private String input;
}
