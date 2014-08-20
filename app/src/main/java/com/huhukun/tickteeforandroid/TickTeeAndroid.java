package com.huhukun.tickteeforandroid;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by kun on 19/08/2014.
 */
public class TickTeeAndroid extends Application {
    private static Context context;
    public static SharedPreferences appSetting;

    @Override
    public void onCreate(){
        super.onCreate();
        context = getApplicationContext();
        appSetting = getSharedPreferences(App_Constants.APP_TAG, 0);

    }

    public static Context getAppContext() {
        return context;
    }
}