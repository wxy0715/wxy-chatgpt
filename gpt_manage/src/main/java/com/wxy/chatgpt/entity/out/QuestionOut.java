package com.wxy.chatgpt.entity.out;

import com.wxy.chatgpt.entity.MilvusPo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


/**
 * todo 加上语雀等平台
 * 回答结果
 * @author wangxingyu
 * @date 2023/03/26 20:40:18
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuestionOut {
    /*gpt出参*/
    private String gpt;

    /*milvus出参*/
    private List<MilvusPo> milvusResult = new ArrayList<>();

}

