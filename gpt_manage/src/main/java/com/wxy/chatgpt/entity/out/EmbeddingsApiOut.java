package com.wxy.chatgpt.entity.out;

import com.google.api.Usage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * @author wangxingyu
 * @date 2023/03/26 20:41:39
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmbeddingsApiOut {
    private String object;
    private List<EmbeddingOut> data;
    private String model;
    private Usage usage;
}
