package com.wxy.chatgpt.rpc;

import com.alibaba.fastjson.JSONObject;
import com.wxy.chatgpt.config.HttpClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * rpc配置类
 * @author wxy
 */
public abstract class AbstractRpcInfo {
    private static final Logger log = LoggerFactory.getLogger(Class.class);

    protected static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded;charset=UTF-8";
    protected static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";
    protected static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";
    /** header配置**/
    protected static Map<String,String> header = new HashMap<>();
    /** requestBody数据**/
    protected static JSONObject jsonParam = new JSONObject();

    static {
        header.put("Connection", "keep-alive");
        header.put("Accept", "*/*");
        header.put("Content-Type", CONTENT_TYPE_JSON);
        header.put("X-Requested-With", "XMLHttpRequest");
        header.put("Cache-Control", "max-age=0");
        //header.put("User-Agent", DEFAULT_USER_AGENT);
    }



    /**
     * 请求封装
     * @param url   url
     * @param param 参数
     */
    public static JSONObject post(String url, String param){
        log.info("请求地址为:{},请求参数为:{}",url,param);
        JSONObject result = HttpClientUtils.doPostHttps(url, header, param);
        return result;
    }
}


