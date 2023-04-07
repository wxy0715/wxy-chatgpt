package com.wxy.chatgpt.service.impl;

import com.wxy.chatgpt.config.ThreadPoolFactory;
import com.wxy.chatgpt.entity.MilvusPo;
import com.wxy.chatgpt.entity.QuestionQry;
import com.wxy.chatgpt.entity.out.EmbeddingsApiOut;
import com.wxy.chatgpt.entity.out.QuestionOut;
import com.wxy.chatgpt.esdata.SaveQuestionCmd;
import com.wxy.chatgpt.repository.QuestionRepository;
import com.wxy.chatgpt.rpc.ChatGptRpcService;
import com.wxy.chatgpt.rpc.EmbeddingRpcService;
import com.wxy.chatgpt.service.IChatGptService;
import io.milvus.client.MilvusClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.collection.ReleaseCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.response.SearchResultsWrapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.zxp.esclientrhl.util.JsonUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * impl gpt聊天服务
 * @author wangxingyu
 * @date 2023/03/26 20:51:06
 */
@Service
public class ChatGptServiceImpl implements IChatGptService {
    private static final Logger log = LoggerFactory.getLogger(Class.class);
    private static final Integer SEARCH_K = 1;
    private static final String SEARCH_PARAM = "{\"nprobe\":10}";
    @Resource
    private MilvusClient milvusClient;
    @Resource
    private ChatGptRpcService chatGptRpcService;
    @Resource
    private EmbeddingRpcService embeddingRpcService;
    @Resource
    private QuestionRepository questionRepository;

    /**
     * 聊天
     * @param questionQry 问题
     * @return {@code String}
     */
    @Override
    public List<SaveQuestionCmd> question(QuestionQry questionQry) {
        List<SaveQuestionCmd> list = new ArrayList<>();
        SearchResponse search;
        try {
            if (!ObjectUtils.isEmpty(questionQry.getQuestion())) {
                QueryBuilder queryBuilder = QueryBuilders.matchQuery("question",questionQry.getQuestion());
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.query(queryBuilder);
                SearchRequest searchRequest = new SearchRequest();
                searchSourceBuilder.sort(new FieldSortBuilder("createDate").order(SortOrder.DESC));
                searchRequest.source(searchSourceBuilder);
                search = questionRepository.search(searchRequest);
            } else {
                MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.query(matchAllQueryBuilder);
                SearchRequest searchRequest = new SearchRequest();
                searchSourceBuilder.sort(new FieldSortBuilder("createDate").order(SortOrder.DESC));
                searchRequest.source(searchSourceBuilder);
                search = questionRepository.search(searchRequest);
            }
        } catch (Exception e) {
            throw new RuntimeException("查询失败:"+e.getMessage());
        }
        SearchHits hits = search.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            SaveQuestionCmd t = JsonUtils.string2Obj(hit.getSourceAsString(), SaveQuestionCmd.class);
            //将_id字段重新赋值给@ESID注解的字段
            t.setQuestionId(hit.getId());
            list.add(t);
        }
        return list;
    }

    /**
     * 保存向量数据
     * @param saveQuestionCmd 问题
     */
    @Override
    public Long saveQuestion(SaveQuestionCmd saveQuestionCmd){
        try {
            questionRepository.save(saveQuestionCmd);
        } catch (Exception e) {
            throw new RuntimeException("插入数据失败");
        }
        return 1L;
    }





    /**
     * @param question 问题
     * @return {@code String}
     */
    public QuestionOut questionOld(String question) {
        QuestionOut questionOut = new QuestionOut();
        // 1.直接从chatgpt获取结果
        CompletableFuture<String> integerCompletableFuture = CompletableFuture.supplyAsync(() -> {
            String result = chatGptRpcService.question(question);
            return result;
        }, ThreadPoolFactory.threadPoolExecutor);
        CompletableFuture<List<MilvusPo>> integerCompletableFuture1 = CompletableFuture.supplyAsync(() -> {
            // 2.获取question的向量,根据向量匹配获取结果
            EmbeddingsApiOut embedding = embeddingRpcService.getEmbedding(question);
            if(embedding != null) {
                List<Float> vector = embedding.getData().get(0).getEmbedding();
                return milvusSearch(Collections.singletonList(vector));
            }
            return new ArrayList<>();
        }, ThreadPoolFactory.threadPoolExecutor);
        try {
            String gpt = integerCompletableFuture.get();
            List<MilvusPo> questionOut2 = integerCompletableFuture1.get();
            questionOut.setGpt(gpt);
            questionOut.setMilvusResult(questionOut2);
        } catch (Exception e) {
            return questionOut;
        }
        return questionOut;
    }

    /**
     * 搜索
     * @param vectors 向量
     * @return {@code List<MilvusPo>}
     */
    @Override
    public List<MilvusPo> milvusSearch(List<List<Float>> vectors){
        // 把milvus数据加载到内存
        milvusClient.loadCollection(LoadCollectionParam.newBuilder()
                        .withCollectionName("chatgpt_question")
                        .build()
        );
        SearchParam searchParam = SearchParam.newBuilder()
                //集合名称
                .withCollectionName("chatgpt_question")
                //计算方式
                .withMetricType(MetricType.L2)
                .withConsistencyLevel(ConsistencyLevelEnum.STRONG)
                .withOutFields(Collections.singletonList("id"))
                .withOutFields(Collections.singletonList("question"))
                .withOutFields(Collections.singletonList("answer"))
                //返回多少条结果
                .withTopK(SEARCH_K)
                //搜索的向量值
                .withVectors(vectors)
                //搜索的Field
                .withVectorFieldName("question_vector")
                .withParams(SEARCH_PARAM)
                .build();
        R<SearchResults> respSearch = milvusClient.search(searchParam);
        if (respSearch.getData() == null) {
            return new ArrayList<>();
        }
        List<MilvusPo> milvusPoList = new ArrayList<>();
        if(respSearch.getStatus() == R.Status.Success.getCode()){
            if (respSearch.getData() != null) {
                SearchResultsWrapper wrapperSearch = new SearchResultsWrapper(respSearch.getData().getResults());
                List<SearchResultsWrapper.IDScore> idScore = wrapperSearch.getIDScore(0);
                if (!ObjectUtils.isEmpty(idScore)) {
                    List<String> question = (List<String>) wrapperSearch.getFieldData("question", 0);
                    List<String> answer = (List<String>) wrapperSearch.getFieldData("answer", 0);
                    int i = 0;
                    for (SearchResultsWrapper.IDScore score : idScore) {
                        MilvusPo milvusPo = new MilvusPo();
                        milvusPo.setScore(score.getScore());
                        milvusPo.setId(score.getLongID());
                        milvusPo.setAnswer(answer.get(i));
                        milvusPo.setQuestion(question.get(i));
                        i++;
                        milvusPoList.add(milvusPo);
                    }
                }
            }
        }
        // 释放milvus内存
        milvusClient.releaseCollection(
                ReleaseCollectionParam.newBuilder()
                        .withCollectionName("chatgpt_question")
                        .build());
        return milvusPoList;
    }

    /**
     * 保存向量数据
     * @param saveQuestionCmd 问题
     */

    public Long saveQuestionOld(SaveQuestionCmd saveQuestionCmd){
        if (ObjectUtils.isEmpty(saveQuestionCmd.getQuestion().trim())) {
            return null;
        }
        List<List<Float>> contentVector = new ArrayList<>();
        EmbeddingsApiOut embedding = embeddingRpcService.getEmbedding(saveQuestionCmd.getQuestion());
        if(embedding == null){
            log.warn("未获取到向量结果");
            return null;
        }
        contentVector.add(embedding.getData().get(0).getEmbedding());
        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field("question", Collections.singletonList(saveQuestionCmd.getQuestion())));
        fields.add(new InsertParam.Field("question_vector", contentVector));
        fields.add(new InsertParam.Field("answer", Collections.singletonList(saveQuestionCmd.getAnswer())));
        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName("chatgpt_question")
                .withFields(fields)
                .build();
        //插入向量数据
        log.info("插入向量数据:{}",insertParam);
        R<MutationResult> insert = milvusClient.insert(insertParam);
        log.info("插入向量数据返回结果ID:{}",insert.getData().getIDs().getIntId().getData(0));
        return insert.getData().getIDs().getIntId().getData(0);
   }


}
