package com.wxy.chatgpt.rpc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.wxy.chatgpt.config.ChatConfig;
import com.wxy.chatgpt.entity.cmd.ChatGptApiCmd;
import com.wxy.chatgpt.entity.out.ChatGptApiOut;
import com.wxy.chatgpt.entity.cmd.Message;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天gpt模型
 * @author wangxingyu
 * @date 2023/03/26 20:12:01
 */
@Component
public class ChatGptRpcService extends AbstractRpcInfo {
    private static final Logger log = LoggerFactory.getLogger(Class.class);
    private final Message systemMsg = new Message("system",
            "Use the text provided to form your answer, but avoid copying the text verbatim and avoid mentioning that you referenced the text in your answer. " +
                    "Try to use your own words when possible. Keep your answer under 5 sentences." +
                    "If the question is not relevant to the text provided you just need to reply in Chinese: \"Sorry, I can't provide you with the answer to {question}.\" " +
                    "If not ask please chat normally." +
                    "Be accurate, helpful, concise, and clear.");

    @Resource
    private ChatConfig chatConfig;
    @Autowired
    private OkHttpClient openAiHttpClient;
    /**
     * 向openAi发送请求获取问题答案
     * @param question 问题
     * @return 答案，超时或者其他异常返回默认信息
     */
    public String questionOld(String question){
        ChatGptApiCmd chatGptApiCmd = new ChatGptApiCmd();
        // 处理需要请求的信息
        List<Message> messageList = new ArrayList<>();
        messageList.add(this.systemMsg);
        Message userMessage = new Message("user", question);
        messageList.add(userMessage);
        chatGptApiCmd.setMessages(messageList);
        header.put("Authorization",chatConfig.getOpenAIkey());
        JSONObject jsonObject = post("https://api.openai.com/v1/chat/completions", JSON.toJSONString(chatGptApiCmd));
        ChatGptApiOut chatGptApiOut = JSON.parseObject(JSON.toJSONString(jsonObject), ChatGptApiOut.class);
        log.info("chatgpt获取结果为:{}",chatGptApiOut);
        try {
            return chatGptApiOut.getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            log.error("chatgpt获取结果错误为:{}---{}",e.getCause(),e.getMessage());
        }
        return "";
    }

    /**
     * 向openAi发送请求获取问题答案
     * @param question 问题
     * @return 答案，超时或者其他异常返回默认信息
     */
    public String question(String question){
        ChatGptApiCmd chatGptApiCmd = new ChatGptApiCmd();
        // 处理需要请求的信息
        List<Message> messageList = new ArrayList<>();
        messageList.add(this.systemMsg);
        Message userMessage = new Message("user", question);
        messageList.add(userMessage);
        chatGptApiCmd.setMessages(messageList);
        Gson gson = new Gson();
        RequestBody requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), gson.toJson(chatGptApiCmd));
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Content-Type","application/json")
                .addHeader("Authorization",chatConfig.getOpenAIkey())
                .post(requestBody).build();
        Message gptMessage;
        try (Response response = this.openAiHttpClient.newCall(request).execute()) {
            if(response.code() == 200){
                ChatGptApiOut chatGptApiResult = gson.fromJson(response.body().string(), ChatGptApiOut.class);
                //取出gpt回应信息
                gptMessage = chatGptApiResult.getChoices().get(0).getMessage();
            }else {
                return "chatGpt出错了,错误码:"+response.code();
            }
        } catch (Exception e){
            log.error("发送请求失败:{}", e.getMessage());
            return "发送请求失败:"+e.getMessage();
        }
        return gptMessage.getContent();
    }
}
