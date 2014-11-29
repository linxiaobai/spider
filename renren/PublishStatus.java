package com.i8da.kevin;

import com.google.gson.Gson;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kevin on 2014/11/29.
 */
public class PublishStatus {
    private static HttpClient httpclient;
    /**
     * 初始化httpClient
     */
    public static void init() {
        httpclient = HttpClients.createDefault();
    }

    /**
     * get the publish hidden post required params
     * @param pageContent
     * @return params map
     */
    public static Map<String,Object> getHiddenParams(String pageContent) {
        Map<String,Object> paramsMap = new HashMap<String, Object>();
        String keyWords = "hostid=";
        int startIndex = pageContent.indexOf(keyWords)+keyWords.length();
        int entIndex = startIndex + 9; //the length of hostid  is 9
        paramsMap.put("hostid",pageContent.substring(startIndex,entIndex));
        keyWords = "get_check:'-";
        startIndex = pageContent.indexOf(keyWords)+keyWords.length();
        entIndex = startIndex + 10; //the length of requestToken is 10
        paramsMap.put("requestToken",pageContent.substring(startIndex,entIndex));
        keyWords = "get_check_x:'";
        startIndex = pageContent.indexOf(keyWords)+keyWords.length();
        entIndex = startIndex + 8; //the length of _rtk is 8
        paramsMap.put("_rtk",pageContent.substring(startIndex,entIndex));
        return paramsMap;
    }

    /**
     * login
     * @param url
     * @param email
     * @param password
     * @return if login success then return homeUrl,
     *         else return ""
     */
    public static String login(String url, String email, String password) throws IOException {
        HttpPost httpPost = getHttpPost(url);
        Map<String,Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("email",email);
        paramsMap.put("password",password);
        UrlEncodedFormEntity entity = getParamsEntity(paramsMap);
        httpPost.setEntity(entity);
        HttpResponse response = httpclient.execute(httpPost);
        String loginRetStr = getRetEntityStr(response);
        JsonObj jsonObj = parseJson(loginRetStr);
        if (!jsonObj.isCode()) {
            return jsonObj.getFailDescription();
        }
        return jsonObj.getHomeUrl();
    }

    /**
     * sent a get request
     * @return the pageHtml that related the param 'getUrl'
     */
    public static String getRequest(String getUrl) throws IOException {
        HttpGet httpGet = getHttpGet(getUrl);
        HttpResponse getResponse =  httpclient.execute(httpGet);
        String pageContent = EntityUtils.toString(getResponse.getEntity(),"UTF-8");
        return pageContent;
    }


    /**
     * get a httpPost
     * @param postUrl
     * @return
     */
    public static HttpPost getHttpPost(String postUrl) {
        return new HttpPost(postUrl);
    }

    /**
     * packaging request params
     * @param params
     * @return
     */
    public static UrlEncodedFormEntity getParamsEntity(Map<String,Object> params) {
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        for (String s : params.keySet()) {
            formParams.add(new BasicNameValuePair(s, params.get(s).toString()));
        }
        return new UrlEncodedFormEntity(formParams, Consts.UTF_8);
    }

    /**
     * parse the string from login to the object
     * @param jsonStr
     * @return
     */
    public static JsonObj parseJson(String jsonStr) {
        Gson gson = new Gson();
        return gson.fromJson(jsonStr,JsonObj.class);
    }

    /**
     * get response return result string
     */
    public static String getRetEntityStr(HttpResponse response) throws IOException {
        HttpEntity httpEntity = response.getEntity();
        return EntityUtils.toString(httpEntity, "UTF-8");
    }

    /**
     * fetch a httpGet
     * @param getUrl
     * @return
     */
    public static HttpGet getHttpGet(String getUrl) {
        return new HttpGet(getUrl);
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        for (int i = 0; i <  10; i++) {
            init();
            String loginUrl = "http://www.renren.com/ajaxLogin/login";
            String email = "linxiaobai_music@foxmail.com";
            String password = "ljdxq165";
            String homeUrl = login(loginUrl,email,password);
            System.out.println("ajax login return url:"+homeUrl);
            String pageContent = getRequest(homeUrl);
            Map<String,Object> map = getHiddenParams(pageContent);
            String publishStatusUrl = "http://shell.renren.com/"+map.get("hostid").toString()+"/status";
            StringBuffer stringBuffer = new StringBuffer("test").append(i);
            map.put("content",stringBuffer.toString()); // input you want post content
            map.put("privacyParams","{\"sourceControl\":-1}"); //-1 is represent privacy ,99 is represent public
            map.put("channel","renren"); //maybe is different from the mobile or pad
            UrlEncodedFormEntity entity = getParamsEntity(map);
            HttpPost httpPost = new HttpPost(publishStatusUrl);
            httpPost.setEntity(entity);
            httpclient.execute(httpPost);
            Thread.sleep(1000);
        }

    }

}

class JsonObj {
    private boolean code;

    private String homeUrl;

    private String failDescription;

    public String getFailDescription() {
        return failDescription;
    }

    public void setFailDescription(String failDescription) {
        this.failDescription = failDescription;
    }

    public boolean isCode() {
        return code;
    }

    public void setCode(boolean code) {
        this.code = code;
    }

    public String getHomeUrl() {
        return homeUrl;
    }

    public void setHomeUrl(String homeUrl) {
        this.homeUrl = homeUrl;
    }
}
