package com.wxy.chatgpt.rpc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wxy.chatgpt.config.ChatConfig;
import com.wxy.chatgpt.entity.cmd.EmbeddingsApiCmd;
import com.wxy.chatgpt.entity.out.EmbeddingsApiOut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 嵌入模型
 * @author wangxingyu
 * @date 2023/03/26 20:12:10
 */
@Component
public class EmbeddingRpcService extends AbstractRpcInfo {
    @Resource
    private ChatConfig chatConfig;

    /**
     * 请求获取Embeddings，请求出错返回null
     * @param msg 需要Embeddings的信息
     * @return 为null则请求失败，反之放回正确结果
     */
    public EmbeddingsApiOut getEmbedding(String msg){
        EmbeddingsApiCmd embeddingsApiCmd = new EmbeddingsApiCmd();
        embeddingsApiCmd.setInput(msg);
        header.put("Authorization",chatConfig.getOpenAIkey());
        JSONObject jsonObject = post("https://api.openai.com/v1/embeddings", JSON.toJSONString(embeddingsApiCmd));
        return JSON.parseObject(JSON.toJSONString(jsonObject), EmbeddingsApiOut.class);
    }
}
