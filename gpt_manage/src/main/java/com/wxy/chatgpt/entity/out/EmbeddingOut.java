package com.wxy.chatgpt.entity.out;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 嵌入obj
 * @author wangxingyu
 * @date 2023/03/26 20:41:22
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmbeddingOut {
    private String object;
    private Integer index;
    private List<Float> embedding;
}
