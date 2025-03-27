package com.example.genie.network;

import com.example.genie.model.ChatRequestBody;
import com.example.genie.model.ChatResponseObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface JsonApi {

    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    Call<ChatResponseObject> getDataChat(
            @Header("Authorization") String auth,
            @Body ChatRequestBody requestBody
    );
}
