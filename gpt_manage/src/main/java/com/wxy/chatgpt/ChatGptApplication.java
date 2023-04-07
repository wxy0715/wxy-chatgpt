package com.wxy.chatgpt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.zxp.esclientrhl.annotation.EnableESTools;

@SpringBootApplication
@EnableESTools(basePackages={"com.wxy.chatgpt.repository"},entityPath = {"com.wxy.chatgpt.esdata"})
public class ChatGptApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatGptApplication.class, args);
    }
}
