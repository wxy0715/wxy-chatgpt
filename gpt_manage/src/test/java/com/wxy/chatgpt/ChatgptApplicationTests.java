package com.wxy.chatgpt;

import com.alibaba.fastjson.JSON;
import com.wxy.chatgpt.rpc.EmbeddingRpcService;
import com.wxy.chatgpt.entity.out.EmbeddingsApiOut;
import com.wxy.chatgpt.entity.MilvusPo;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.DataType;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SpringBootTest
class ChatgptApplicationTests {
    private static final Logger log = LoggerFactory.getLogger(Class.class);
    @Autowired
    MilvusServiceClient milvusClient;
    @Resource
    EmbeddingRpcService embeddingRpcService;
    private static final Integer SEARCH_K = 7;
    private static final String SEARCH_PARAM = "{\"nprobe\":10}";

    @Test
    void prepare() {
        dropCollection(milvusClient);
        createCollection(milvusClient);
        buildIndex(milvusClient);
    }
    void buildIndex(MilvusServiceClient client){
        final String INDEX_PARAM = "{\"nlist\":1024}";
        client.createIndex(
                CreateIndexParam.newBuilder()
                        .withCollectionName("chatgpt_question")
                        .withFieldName("question_vector")
                        .withIndexType(IndexType.IVF_FLAT)
                        .withMetricType(MetricType.L2)
                        .withExtraParam(INDEX_PARAM)
                        .withSyncMode(Boolean.FALSE)
                        .build()
        );
    }
    void dropCollection(MilvusServiceClient client){
        client.dropCollection(
                DropCollectionParam.newBuilder()
                        .withCollectionName("chatgpt_question")
                        .build()
        );
    }
    void createCollection(MilvusServiceClient client){
        FieldType fieldType1 = FieldType.newBuilder()
                .withName("id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(true)
                .build();
        FieldType fieldType3 = FieldType.newBuilder()
                .withName("question")
                .withDataType(DataType.VarChar)
                .withMaxLength(1024)
                .build();
        FieldType fieldType2 = FieldType.newBuilder()
                .withName("answer")
                .withDataType(DataType.VarChar)
                .withMaxLength(9072)
                .build();
        FieldType fieldType4 = FieldType.newBuilder()
                .withName("question_vector")
                .withDataType(DataType.FloatVector)
                .withDimension(1536)
                .build();
        CreateCollectionParam createCollectionReq = CreateCollectionParam.newBuilder()
                .withCollectionName("chatgpt_question")
                .withShardsNum(4)
                .addFieldType(fieldType1)
                .addFieldType(fieldType2)
                .addFieldType(fieldType3)
                .addFieldType(fieldType4)
                .build();
        client.createCollection(createCollectionReq);
    }

    /*把集合加载到内存中(milvus查询前必须把数据加载到内存中)*/
    @Test
    public void loadCollection() {
        R<RpcStatus> response = milvusClient.loadCollection(LoadCollectionParam.newBuilder()
                //集合名称
                .withCollectionName("chatgpt_question")
                .build());
        log.info("loadCollection------------->{}", response);
    }

    // 释放milvus内存
    @Test
    public void releaseCollection() {
        milvusClient.releaseCollection(
                ReleaseCollectionParam.newBuilder()
                        .withCollectionName("chatgpt_question")
                        .build());
    }


    @Test
    public void search() {
        loadCollection();
        EmbeddingsApiOut embeddingsApiResult = embeddingRpcService.getEmbedding("预算编制是什么");
        List<Float> vector = embeddingsApiResult.getData().get(0).getEmbedding();
        List<List<Float>> list = Arrays.asList(vector);
        SearchParam searchParam = SearchParam.newBuilder()
                //集合名称
                .withCollectionName("chatgpt_question")
                .withConsistencyLevel(ConsistencyLevelEnum.STRONG)
                .withMetricType(MetricType.L2)
                .withOutFields(Collections.singletonList("id"))
                .withOutFields(Collections.singletonList("question"))
                .withOutFields(Collections.singletonList("answer"))
                //要返回的最相似结果的数量
                .withTopK(SEARCH_K)
                //搜索的向量值
                .withVectors(list)
                //搜索的Field
                .withVectorFieldName("question_vector")
                .withParams(SEARCH_PARAM)
                .build();
        R<SearchResults> respSearch = milvusClient.search(searchParam);
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
                }
            }
        }
        releaseCollection();
    }


    @Test
    public void saveQuestion(){
        List<String> list = new ArrayList<>();
        list.add("js是什么");
        list.add("预算编制是什么");
        list.add("预算调整是什么");
        list.add("预算中台是什么");
        list.add("css的作用");
        list.add("java和javascript的区别");
        for (String question : list) {
            if (ObjectUtils.isEmpty(question.trim())) {
                continue;
            }
            List<List<Float>> contentVector = new ArrayList<>();
            EmbeddingsApiOut embedding = embeddingRpcService.getEmbedding(question);
            if(embedding == null){
                log.warn("未获取到向量结果");
                continue;
            }
            contentVector.add(embedding.getData().get(0).getEmbedding());
            List<InsertParam.Field> fields = new ArrayList<>();
            fields.add(new InsertParam.Field("question", Collections.singletonList(question)));
            fields.add(new InsertParam.Field("question_vector", contentVector));
            fields.add(new InsertParam.Field("answer", Collections.singletonList("1")));
            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName("chatgpt_question")
                    .withFields(fields)
                    .build();
            //插入向量数据
            log.info("插入向量数据:{}",insertParam);
            R<MutationResult> insert = milvusClient.insert(insertParam);
            log.info("插入向量数据返回结果ID:{}",insert.getData().getIDs().getIntId().getData(0));
        }
    }
}
