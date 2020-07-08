package com.example.applishopify;

import android.app.Application;

import com.shopify.buy3.GraphClient;

import kotlin.Unit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class Client extends BaseApplication {
    private static final String SHOP_DOMAIN = "testngrok.myshopify.com";
    private static final String API_KEY = "f988d1db7cbb4fe008520d661238fb92";
    private static GraphClient client;

    public static GraphClient client(){
        return client;
    }

    protected void initialize(){

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(BuildConfig.OKHTTP_LOG_LEVEL))
                .build();

        client = GraphClient.Companion.build(this, SHOP_DOMAIN, API_KEY,
                builder -> {
                        builder.setHttpClient(httpClient);
                        return Unit.INSTANCE;
        });
    }
}
