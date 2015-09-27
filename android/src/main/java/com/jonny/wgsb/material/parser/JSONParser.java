package com.jonny.wgsb.material.parser;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;

public class JSONParser {
    private static JSONObject jObj = null;

    public JSONObject makeHttpRequest(String url) throws IOException {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();
            jObj = new JSONObject(response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jObj;
    }
}