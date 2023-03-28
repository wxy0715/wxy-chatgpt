package com.wxy.chatgpt.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 向量po
 * @author wangxingyu
 * @date 2023/03/26 20:49:49
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MilvusPo {
    /*数据库主键id*/
    private Long id;
    /*搜索内容*/
    private String question;
    /*答案*/
    private String answer;
    /*距离*/
    private float score;
}
