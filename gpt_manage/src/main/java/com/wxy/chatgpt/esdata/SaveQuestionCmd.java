package com.wxy.chatgpt.esdata;

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
 * 保存问题
 * @author wangxingyu
 * @date 2023/03/27 21:57:51
 */
@Data
@ESMetaData(indexName = "faq",number_of_shards = 3,number_of_replicas = 0,printLog = true)
public class SaveQuestionCmd {
    @ESID
    private String questionId;
    @ESMapping(datatype = DataType.text_type,search_analyzer= Analyzer.ik_max_word,ngram=true,suggest = true)
    private String question;
    @ESMapping(datatype = DataType.text_type)
    private String answerTypeValue;
    @ESMapping(datatype = DataType.text_type)
    private String answer;
    @ESMapping(datatype = DataType.date_type)
    private Date createDate = new Date();
}
