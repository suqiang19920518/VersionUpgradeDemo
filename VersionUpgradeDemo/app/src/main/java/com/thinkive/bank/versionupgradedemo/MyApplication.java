package com.thinkive.bank.versionupgradedemo;


import android.app.Application;

import com.pgyersdk.crash.PgyCrashManager;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PgyCrashManager.register(this);//注册Crash接口(蒲公英)
    }
}
