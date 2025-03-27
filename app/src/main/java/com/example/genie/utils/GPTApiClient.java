package com.example.genie.utils;

import android.util.Log;

import com.example.genie.model.ChatRequestBody;
import com.example.genie.model.ChatResponseObject;
import com.example.genie.network.JsonApi;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;


import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GPTApiClient {
    private static final String TAG = "GPTApiClient";
    private static final String API_KEY = "";
    private static final String BASE_URL = "https://api.openai.com/";

    private final JsonApi jsonApi;

    public GPTApiClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)  // connection setup timeout
                .readTimeout(30, TimeUnit.SECONDS)     // waiting to read the response
                .writeTimeout(30, TimeUnit.SECONDS)    // sending the request timeout
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        jsonApi = retrofit.create(JsonApi.class);
    }

    public void callGPT4API(String prompt, GPTCallback callback) {
        ChatRequestBody requestBody = new ChatRequestBody(
                "gpt-4o",
                Arrays.asList(
                        new ChatRequestBody.Message("system", "You are an assistant for UI automation. Follow instructions carefully."),
                        new ChatRequestBody.Message("user", prompt)
                ),
                0.2
        );

        Call<ChatResponseObject> call = jsonApi.getDataChat("Bearer " + API_KEY, requestBody);

        call.enqueue(new Callback<ChatResponseObject>() {
            @Override
            public void onResponse(Call<ChatResponseObject> call, Response<ChatResponseObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String content = response.body().choices.get(0).message.content.trim();
                    Log.d(TAG, "GPT Success:\n" + content);
                    callback.onSuccess(content);
                } else {
                    Log.e(TAG, "GPT error: " + response.code());
                    callback.onError("Response error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ChatResponseObject> call, Throwable t) {
                Log.e(TAG, "GPT call failure", t);
                callback.onError("Failure: " + t.getMessage());
            }
        });
    }

    // Define a callback interface for success/failure handling
    public interface GPTCallback {
        void onSuccess(String response);
        void onError(String error);
    }
}