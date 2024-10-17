package com.sleepeasycenter.o2ring_app.api;

import com.sleepeasycenter.o2ring_app.BuildConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;

public class SleepEasyAPI{
    private static SleepEasyAPIService _service = null;

    public static SleepEasyAPIService getService() {
        if (_service == null){
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    String token = Base64.getEncoder().encodeToString((BuildConfig.API_USERNAME+":"+BuildConfig.API_PASSWORD).getBytes(StandardCharsets.UTF_8));
                    Request newRequest  = chain.request().newBuilder()
                            .addHeader("Authorization", "Basic " + token)
                            .build();
                    return chain.proceed(newRequest);
                }
            }).build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://sleepapnea-withr.sleepeasysingapore.com/")
                    .client(client)
                    .build();
            _service = retrofit.create(SleepEasyAPIService.class);
        }
        return _service;
    }
}
