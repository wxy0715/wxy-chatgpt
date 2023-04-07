package com.wxy.chatgpt.repository;

import com.wxy.chatgpt.esdata.SaveQuestionCmd;
import org.zxp.esclientrhl.auto.intfproxy.ESCRepository;

public interface QuestionRepository extends ESCRepository<SaveQuestionCmd,String> {
}
