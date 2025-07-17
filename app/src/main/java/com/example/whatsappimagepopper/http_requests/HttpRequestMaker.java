package com.example.whatsappimagepopper.http_requests;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

abstract public class HttpRequestMaker {
    private final String url;
    private static OkHttpClient httpClientInstance = null;

    public static OkHttpClient getHttpClientInstance(){
        if (httpClientInstance == null){
            httpClientInstance = new OkHttpClient();
            return httpClientInstance;
        }
        return httpClientInstance;
    }

    public HttpRequestMaker(String url) {
        super();
        if (getHttpClientInstance() == null){
            throw new RuntimeException("Failed to get HTTP Client Instance.");
        }
        this.url = url;
    }

    public abstract void onResp(String data);
    public abstract void onFail(IOException err);
    public abstract void onCompletion();

    public void post(String data, String content_type){
        RequestBody rb = RequestBody.create(data, MediaType.get(content_type));
        Request r = new Request.Builder().url(this.url).post(rb).build();
        httpClientInstance.newCall(r).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                onCompletion();
                onFail(e);
                call.cancel();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                onCompletion();
                ResponseBody resp_ = response.body();
                if (resp_ == null){
                    onResp("");
                    return;
                }
                onResp(resp_.string());
            }
        });
    }

    public void postText(String data){
        this.post(data, "text/plain");
    }

    public void postJSON(JSONObject data){
        this.post(data.toString(), "application/json");
    }
}
