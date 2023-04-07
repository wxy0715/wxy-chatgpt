package com.wxy.chatgpt.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zxp.esclientrhl.annotation.ESID;
import org.zxp.esclientrhl.annotation.ESMapping;
import org.zxp.esclientrhl.annotation.ESMetaData;
import org.zxp.esclientrhl.enums.Analyzer;
import org.zxp.esclientrhl.enums.DataType;

import java.util.Date;

/**
 * 问题查询
 * @author wangxingyu
 * @date 2023/03/27 21:57:51
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuestionQry {
    // 问题
    private String question;
    // 问题类型 chatgpt/faq
    private String questionType;
}
