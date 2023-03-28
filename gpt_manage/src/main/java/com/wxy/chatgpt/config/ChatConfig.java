package com.wxy.chatgpt.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import lombok.Data;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 配置
 * @author wangxingyu
 * @date 2023/03/26 21:23:43
 */
@Configuration
@Data
public class ChatConfig {
    @Value("${milvus.ip}")
    private String milvusIp;
    @Value("${milvus.port}")
    private int milvusPort;
    @Value("${openAIkey}")
    private String openAIkey;

    @Bean
    public OkHttpClient openAIHttpClient(){
        return new OkHttpClient.Builder().callTimeout(Duration.ofMinutes(1)).connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                //.proxy(new Proxy(Proxy.Type.HTTP,new InetSocketAddress("p1.bxy-node.top",31107)))
                .build();
    }

    @Bean
    public MilvusServiceClient milvusClient(){
        return new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withHost(milvusIp)
                        .withPort(milvusPort)
                        .build()
        );
    }
}
