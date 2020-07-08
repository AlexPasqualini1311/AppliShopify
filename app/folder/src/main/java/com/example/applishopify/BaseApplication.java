package com.example.applishopify;

import android.app.Application;


public abstract class BaseApplication extends Application {
    @Override
    public void onCreate(){
        super.onCreate();

        initialize();
    }

    protected abstract void initialize();
}
