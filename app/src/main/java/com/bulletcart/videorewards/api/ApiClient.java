package com.bulletcart.videorewards.api;

import com.bulletcart.videorewards.Global.GlobalConstants;

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by Administrator on 09/16/2017.
 */
public class ApiClient {
    public static final String CONTENT_TYPE         = "application/json";
    public static final String HEADERFIELD_ACCEPT   = "application/json";

    private static ApiInterface apiService;

    public static ApiInterface getApiClient() {
        if (apiService == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(300, TimeUnit.SECONDS)
                    .readTimeout(300, TimeUnit.SECONDS)
                    .writeTimeout(300, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .connectionPool(new ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
                    .build();
            Retrofit restAdapter = new Retrofit.Builder()
                    .baseUrl(GlobalConstants.POS_SERVER_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            apiService = restAdapter.create(ApiInterface.class);
        }
        return apiService;
    }
}
