package com.wxy.chatgpt.config;

import com.alibaba.fastjson.JSONObject;
import okhttp3.OkHttpClient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;

/**
 * 依赖的jar包有：commons-lang-2.6.jar、httpclient-4.3.2.jar、httpcore-4.3.1.jar、commons-io-2.4.jar
 * @author wangxingyu
 */
public class HttpClientUtils {
	private static final Logger log = LoggerFactory.getLogger(HttpClientUtils.class);
	public static final int connTimeout=10000;
	public static final int socketTimeout=180000;
	private static HttpClient client;
	public static final PoolingHttpClientConnectionManager cm;
	static {
		cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(128);
		cm.setDefaultMaxPerRoute(128);
	}

	/**
	 * 发送一个 Post 请求, 使用指定的字符集编码.
	 * @param url
	 * @param body RequestBody
	 * @param mimeType 例如 application/xml "application/x-www-form-urlencoded" a=1&b=2&c=3
	 * @param charset 编码
	 * @param connTimeout 建立链接超时时间,毫秒.
	 * @param readTimeout 响应超时时间,毫秒.
	 * @return ResponseBody, 使用指定的字符集编码.
	 * @throws ConnectTimeoutException 建立链接超时异常
	 * @throws SocketTimeoutException  响应超时
	 * @throws Exception
	 */
	public static String post(String url, String body, String mimeType, String charset, Integer connTimeout, Integer readTimeout)
			throws Exception {
		BasicCookieStore cookieStore = new BasicCookieStore();
		HttpPost post = new HttpPost(url);
		String result = "";
		try {
			if (StringUtils.isNotBlank(body)) {
				HttpEntity entity = new StringEntity(body, ContentType.create(mimeType, charset));
				post.setEntity(entity);
			}
			// 设置参数
			Builder customReqConf = RequestConfig.custom();
			if (connTimeout != null) {
				customReqConf.setConnectTimeout(connTimeout);
			}
			if (readTimeout != null) {
				customReqConf.setSocketTimeout(readTimeout);
			}
			post.setConfig(customReqConf.build());

			HttpResponse res;
			if (url.startsWith("https")) {
				// 执行 Https 请求.
				client = createSSLInsecureClient(cookieStore);
				res = client.execute(post);
			} else {
				client = HttpClients.custom().setConnectionManager(cm).setDefaultCookieStore(cookieStore).build();
				res = client.execute(post);
			}
			result = IOUtils.toString(res.getEntity().getContent(), charset);
		} finally {
			post.releaseConnection();
			if (url.startsWith("https") && client != null&& client instanceof CloseableHttpClient) {
				((CloseableHttpClient) client).close();
			}
		}
		return result;
	}


	/**
	 * 提交form表单
	 *
	 * @param url
	 * @param params
	 * @param connTimeout
	 * @param readTimeout
	 * @return
	 * @throws ConnectTimeoutException
	 * @throws SocketTimeoutException
	 * @throws Exception
	 */
	public static String postForm(String url, Map<String, String> params, Map<String, String> headers, Integer connTimeout, Integer readTimeout) throws Exception {
		HttpPost post = new HttpPost(url);
		BasicCookieStore cookieStore = new BasicCookieStore();
		try {
			if (params != null && !params.isEmpty()) {
				List<NameValuePair> formParams = new ArrayList<NameValuePair>();
				Set<Entry<String, String>> entrySet = params.entrySet();
				for (Entry<String, String> entry : entrySet) {
					formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
				post.setEntity(entity);
			}

			if (headers != null && !headers.isEmpty()) {
				for (Entry<String, String> entry : headers.entrySet()) {
					post.addHeader(entry.getKey(), entry.getValue());
				}
			}
			// 设置参数
			Builder customReqConf = RequestConfig.custom();
			if (connTimeout != null) {
				customReqConf.setConnectTimeout(connTimeout);
			}
			if (readTimeout != null) {
				customReqConf.setSocketTimeout(readTimeout);
			}
			post.setConfig(customReqConf.build());
			HttpResponse res = null;
			if (url.startsWith("https")) {
				// 执行 Https 请求.
				client = createSSLInsecureClient(cookieStore);
				res = client.execute(post);
			} else {
				client = HttpClients.custom().setConnectionManager(cm).setDefaultCookieStore(cookieStore).build();
				res = client.execute(post);
			}
			return IOUtils.toString(res.getEntity().getContent(), "UTF-8");
		} finally {
			post.releaseConnection();
			if (url.startsWith("https") && client != null
					&& client instanceof CloseableHttpClient) {
				((CloseableHttpClient) client).close();
			}
		}
	}

	/**
	 * 发送一个 GET 请求
	 * @param url
	 * @param charset
	 * @param connTimeout  建立链接超时时间,毫秒.
	 * @param readTimeout  响应超时时间,毫秒.
	 * @return
	 * @throws Exception
	 */
	public static String get(String url, String charset, Integer connTimeout, Integer readTimeout)
			throws Exception {
		BasicCookieStore cookieStore = new BasicCookieStore();
		HttpGet get = new HttpGet(url);
		String result = "";
		try {
			// 设置参数
			Builder customReqConf = RequestConfig.custom();
			if (connTimeout != null) {
				customReqConf.setConnectTimeout(connTimeout);
			}
			if (readTimeout != null) {
				customReqConf.setSocketTimeout(readTimeout);
			}
			get.setConfig(customReqConf.build());

			HttpResponse res = null;

			if (url.startsWith("https")) {
				// 执行 Https 请求.
				client = createSSLInsecureClient(cookieStore);
				res = client.execute(get);
			} else {
				client = HttpClients.custom().setConnectionManager(cm).setDefaultCookieStore(cookieStore).build();
				res = client.execute(get);
			}
			result = IOUtils.toString(res.getEntity().getContent(), charset);
		} finally {
			get.releaseConnection();
			if (url.startsWith("https") && client != null && client instanceof CloseableHttpClient) {
				((CloseableHttpClient) client).close();
			}
		}
		return result;
	}

	/**
	 * 从 response 里获取 charset
	 * @param response response
	 */
	@SuppressWarnings("unused")
	private static String getCharsetFromResponse(HttpResponse response) {
		// Content-Type:text/html; charset=GBK
		if (response.getEntity() != null  && response.getEntity().getContentType() != null && response.getEntity().getContentType().getValue() != null) {
			String contentType = response.getEntity().getContentType().getValue();
			if (contentType.contains("charset=")) {
				return contentType.substring(contentType.indexOf("charset=") + 8);
			}
		}
		return null;
	}

	/**
	 * 创建 SSL连接
	 * @throws GeneralSecurityException
	 */
	public static CloseableHttpClient createSSLInsecureClient(BasicCookieStore cookieStore) throws GeneralSecurityException {
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] chain, String authType) {
					return true;
				}
			}).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new HostnameVerifier() {
				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}
			});
			return HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultCookieStore(cookieStore).build();
		} catch (GeneralSecurityException e) {
			throw e;
		}
	}

	/**
	 * post请求
	 * @param url 路径
	 * @param jsonObject object
	 * @return json object
	 */
	public static JSONObject doPostHttps(String url, Map<String, String> headers, JSONObject jsonObject) {
		BasicCookieStore cookieStore = new BasicCookieStore();
		HttpResponse res;
		HttpPost post = new HttpPost(url);
		JSONObject response = null;
		try {
			// 循环添加header
			Iterator headerIterator = headers.entrySet().iterator();
			while (headerIterator.hasNext()) {
				Entry<String, String> elem = (Entry<String, String>) headerIterator.next();
				post.addHeader(elem.getKey(), elem.getValue());
			}
			post.setEntity(new StringEntity(jsonObject.toString(), Charset.forName("UTF-8")));
			RequestConfig requestConfig = RequestConfig.custom()
					.setConnectTimeout(connTimeout).setConnectionRequestTimeout(connTimeout)
					.setSocketTimeout(socketTimeout).build();
			post.setConfig(requestConfig);
			if (url.startsWith("https")) {
				// 执行 Https 请求.
				HttpClient client = createSSLInsecureClient(cookieStore);
				res = client.execute(post);
			} else {
				client = HttpClients.custom().setConnectionManager(cm).setDefaultCookieStore(cookieStore).build();
				res = client.execute(post);
			}
			if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 获取cookie
				List<Cookie> cookies = cookieStore.getCookies();
				if (ObjectUtils.isNotEmpty(cookies)) {
					for (Cookie cookie : cookies) {
						log.info("cookie为:{}",cookie);
					}
				}
				// 获取返回结果
				HttpEntity entity = res.getEntity();
				String result = EntityUtils.toString(entity);
				response = JSONObject.parseObject(result);
				if (ObjectUtils.isEmpty(response)) {
					throw new RuntimeException("请求外部接口失败");
				}
				response.put("cookie",cookieStore.getCookies());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return response;
	}

	/**
	 * post请求
	 * @param url 路径
	 * @param  param 参数
	 * @return json object
	 */
	public static JSONObject doPostHttps(String url, Map<String, String> headers, String param) {
		BasicCookieStore cookieStore = new BasicCookieStore();
		HttpResponse res;
		HttpPost post = new HttpPost(url);
		JSONObject response = null;
		try {
			// 循环添加header
			Iterator headerIterator = headers.entrySet().iterator();
			while (headerIterator.hasNext()) {
				Entry<String, String> elem = (Entry<String, String>) headerIterator.next();
				post.addHeader(elem.getKey(), elem.getValue());
			}
			post.setEntity(new StringEntity(param, Charset.forName("UTF-8")));
			RequestConfig requestConfig = RequestConfig.custom()
					.setConnectTimeout(connTimeout).setConnectionRequestTimeout(connTimeout)
					.setSocketTimeout(socketTimeout).build();
			post.setConfig(requestConfig);
			if (url.startsWith("https")) {
				// 执行 Https 请求.
				HttpClient client = createSSLInsecureClient(cookieStore);
				res = client.execute(post);
			} else {
				client = HttpClients.custom().setConnectionManager(cm).setDefaultCookieStore(cookieStore).build();
				res = client.execute(post);
			}
			if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 获取返回结果
				HttpEntity entity = res.getEntity();
				String result = EntityUtils.toString(entity);
				response = JSONObject.parseObject(result);
				if (ObjectUtils.isEmpty(response)) {
					throw new RuntimeException("请求外部接口失败");
				}
				response.put("cookie",cookieStore.getCookies());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return response;
	}
}
