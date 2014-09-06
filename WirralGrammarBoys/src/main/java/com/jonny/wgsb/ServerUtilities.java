package com.jonny.wgsb;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import static com.jonny.wgsb.CommonUtilities.SERVER_UNREGISTER_URL;
import static com.jonny.wgsb.CommonUtilities.SERVER_UPDATE_URL;
import static com.jonny.wgsb.CommonUtilities.SERVER_URL;
import static com.jonny.wgsb.CommonUtilities.TAG;

public final class ServerUtilities {
	private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();
	
    static void register(final Context context, final String regId, String name, String email, String year7, String year8, String year9,
    		String year10, String year11, String year12, String year13) {
        Log.i(TAG, "registering device (regId = " + regId + ")");
        String serverUrl = SERVER_URL;
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        params.put("name", name);
        params.put("email", email);
        params.put("year7", year7);
        params.put("year8", year8);
        params.put("year9", year9);
        params.put("year10", year10);
        params.put("year11", year11);
        params.put("year12", year12);
        params.put("year13", year13);
        long backOff = BACKOFF_MILLI_SECONDS + random.nextInt(1500);
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            Log.d(TAG, "Attempt #" + i + " to register");
            try {
                post(serverUrl, params);
                String message = context.getString(R.string.server_registered);
                CommonUtilities.displayMessage(context, message);
                return;
            } catch (IOException e) {
                Log.e(TAG, "Failed to register on attempt " + i + ": " + e);
                if (i == MAX_ATTEMPTS) {
                    break;
                }
                try {
                    Log.d(TAG, "Sleeping for " + backOff + " ms before retry");
                    Thread.sleep(backOff);
                } catch (InterruptedException e1) {
                    Log.d(TAG, "Thread interrupted: abort remaining retries!");
                    Thread.currentThread().interrupt();
                    return;
                }
                backOff *= 2;
            }
        }
        String message = context.getString(R.string.server_register_error, MAX_ATTEMPTS);
        CommonUtilities.displayMessage(context, message);
    }

    static void update(final String regId, String email, String year7, String year8, String year9,
                         String year10, String year11, String year12, String year13) {
        String serverUrl = SERVER_UPDATE_URL;
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        params.put("email", email);
        params.put("year7", year7);
        params.put("year8", year8);
        params.put("year9", year9);
        params.put("year10", year10);
        params.put("year11", year11);
        params.put("year12", year12);
        params.put("year13", year13);
        long backOff = BACKOFF_MILLI_SECONDS + random.nextInt(1500);
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            Log.d(TAG, "Attempt #" + i + " to update");
            try {
                post(serverUrl, params);
                return;
            } catch (IOException e) {
                Log.e(TAG, "Failed to update on attempt " + i + ": " + e);
                if (i == MAX_ATTEMPTS) {
                    break;
                }
                Log.d(TAG, "Sleeping for " + backOff + " ms before retry");
                backOff *= 2;
            }
        }
    }

    static void unregister(final String regId) {
        Log.i(TAG, "Un-Registering device (regId = " + regId + ")");
        String serverUrl = SERVER_UNREGISTER_URL;
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        long backOff = BACKOFF_MILLI_SECONDS + random.nextInt(1500);
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            Log.d(TAG, "Attempt #" + i + " to unregister");
            try {
                post(serverUrl, params);
                return;
            } catch (IOException e) {
                Log.e(TAG, "Failed to unregister on attempt " + i + ": " + e);
                if (i == MAX_ATTEMPTS) {
                    break;
                }
                Log.d(TAG, "Sleeping for " + backOff + " ms before retry");
                backOff *= 2;
            }
        }
    }

    private static void post(String endpoint, Map<String, String> params) throws IOException {  
        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=').append(param.getValue());
            if (iterator.hasNext()) bodyBuilder.append('&');
        }
        String body = bodyBuilder.toString();
        Log.v(TAG, "Posting '" + body + "' to " + url);
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            int status = conn.getResponseCode();
            if (status != 200) throw new IOException("Post failed with error code " + status);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}