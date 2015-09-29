/*
 * Copyright (c) 2015 Sohu TV. All rights reserved.
 */
package com.brush.zhushou360;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * <P>
 * Description:linxiaobai@live.com
 * </p>
 *
 * @author Kevin
 * @version 1.0
 * @Date 2015年9月29日下午2:24:55
 */
public class TJPU {
    public static void main(String[] args) throws ClientProtocolException, IOException {
        final String loginUrl = "http://jwpt.tjpu.edu.cn:8081/loginAction.do";
        final String username = "";
        final String password = "";

        HttpClient httpClient = com.parsejson.HttpClientFactory.fetchHttpClient();
        HttpPost httpPost = new HttpPost(loginUrl);
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("zjh", username);
        params.put("mm", password);
        httpPost.setEntity(convertPostData(params));
        HttpResponse httpResponse = httpClient.execute(httpPost);
        String ret = getRetEntityStr(httpResponse);
        if (ret.contains("帐号")) {
            System.out.println("用户名或密码不正确!");
            return;
        }
        final String getAllScoresUrl = "http://jwpt.tjpu.edu.cn:8081/gradeLnAllAction.do?type=ln&oper=sxinfo&lnsxdm=001";
        HttpGet httpGet = new HttpGet(getAllScoresUrl);
        String html = getRetEntityStr(httpClient.execute(httpGet));
        Document doc = Jsoup.parse(html);
        HashMap<String, String> hashMap = new HashMap<String, String>();
        Elements elements = doc.select("tr.odd");
        for (Element element : elements) {
            hashMap.put(subStringData(element.child(2).toString()), subStringData2(element.child(6).toString()));
        }
        String keywords = ""; //查询的关键字，可以模糊匹配(例如‘程序设计’)，如果为“”则查询所有
        Iterator<Map.Entry<String, String>> iterEntry = hashMap.entrySet().iterator();
        Map.Entry<String, String> entry;
        while (iterEntry.hasNext()) {
            entry = iterEntry.next();
            String key = entry.getKey();
            if (key.contains(keywords) || keywords.equals("")) {
                String value = entry.getValue().toString();
                System.out.println(key + "-----" + value);
            }
        }
    }

    public static HttpEntity convertPostData(HashMap<String, Object> params) {
        List<BasicNameValuePair> formParams = new ArrayList<BasicNameValuePair>();
        if (params != null && !params.isEmpty()) {
            Iterator<Map.Entry<String, Object>> iterEntry = params.entrySet().iterator();
            Map.Entry<String, Object> entry;
            while (iterEntry.hasNext()) {
                entry = iterEntry.next();
                String key = entry.getKey();
                String value = entry.getValue().toString();
                formParams.add(new BasicNameValuePair(key, value));
            }
        }
        try {
            HttpEntity entity = new UrlEncodedFormEntity(formParams, "UTF-8");
            return entity;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getRetEntityStr(HttpResponse response) throws IOException {
        HttpEntity httpEntity = response.getEntity();
        return EntityUtils.toString(httpEntity, "UTF-8");
    }

    public static String subStringData(String oldString) {
        return oldString.substring(oldString.indexOf(">") + 1, oldString.lastIndexOf("<")).trim();
    }

    public static String subStringData2(String oldString) {
        return oldString.substring(oldString.indexOf("<p align=\"center\">") + "<p align=\"center\">".length(),
                oldString.indexOf("&"));
    }
}
