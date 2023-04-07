package com.wxy.chatgpt.service;

import com.wxy.chatgpt.entity.MilvusPo;
import com.wxy.chatgpt.entity.QuestionQry;
import com.wxy.chatgpt.esdata.SaveQuestionCmd;

import java.util.List;

/**
 * @author wangxingyu
 * @date 2023/03/26 20:10:55
 */
public interface IChatGptService {
    /**
     * 聊天
     * @param questionQry 问题
     * @return {@code String}
     */
    List<SaveQuestionCmd> question(QuestionQry questionQry);

    /**
     * 搜索
     * @param search_vectors 搜索向量
     * @return {@code List<MilvusPo>}
     */
    List<MilvusPo> milvusSearch(List<List<Float>> search_vectors);

    /**
     * 保存向量数据
     * @param saveQuestionCmd 问题
     */
    Long saveQuestion(SaveQuestionCmd saveQuestionCmd);
}
