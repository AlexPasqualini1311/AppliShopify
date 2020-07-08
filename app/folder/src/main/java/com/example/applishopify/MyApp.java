package com.example.applishopify;

import android.app.Application;
import android.content.Context;

public class MyApp extends Application {
    private static MyApp instance;
    protected static Context context;

    public static MyApp getInstance() {
        return instance;
    }

    public static Context getContext(){
        return instance.getApplicationContext();
        // or return instance.getApplicationContext();
    }

    public static void setContext(Context mContext) {
        context = mContext;
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }
}
