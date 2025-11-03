package com.example.inktouch.api;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    private static final String BASE_URL = "https://qdeetwuxlqmijwkbolof.supabase.co/";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFkZWV0d3V4bHFtaWp3a2JvbG9mIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTk4OTI0ODgsImV4cCI6MjA3NTQ2ODQ4OH0.4MGt8YGs4kbg2HhhywuEjGqpeLMH3W5Iu3_Tk7ZkVtw";

    private static Retrofit retrofit = null;
    private static Retrofit authRetrofit = null;
    private static String accessToken = null;

    public static void setAccessToken(String token) {
        accessToken = token;
        retrofit = null; // Reset retrofit to rebuild with new token
    }

    public static String getAccessToken() {
        return accessToken;
    }

    private static OkHttpClient getOkHttpClient(boolean includeAuth) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(logging);

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request.Builder requestBuilder = chain.request().newBuilder()
                        .addHeader("apikey", API_KEY)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=representation");

                if (includeAuth && accessToken != null) {
                    requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
                }

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        return httpClient.build();
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(getOkHttpClient(true))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getAuthClient() {
        if (authRetrofit == null) {
            authRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(getOkHttpClient(false))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return authRetrofit;
    }

    public static SupabaseApi getApi() {
        return getClient().create(SupabaseApi.class);
    }

    public static SupabaseAuthApi getAuthApi() {
        return getAuthClient().create(SupabaseAuthApi.class);
    }
}
