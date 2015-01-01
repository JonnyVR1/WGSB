package com.jonny.wgsb.material.parser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class JSONParser {
    private static JSONObject jObj = null;

    public JSONObject makeHttpRequest(String url, List<NameValuePair> params) {
        HttpParams httpParameters = new BasicHttpParams();
        int connectionTimeout = 1000 * 12;
        int socketTimeout = 1000 * 13;
        HttpConnectionParams.setConnectionTimeout(httpParameters, connectionTimeout);
        HttpConnectionParams.setSoTimeout(httpParameters, socketTimeout);
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            InputStream is = httpEntity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            is.close();
            String json = sb.toString();
            jObj = new JSONObject(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jObj;
    }
}