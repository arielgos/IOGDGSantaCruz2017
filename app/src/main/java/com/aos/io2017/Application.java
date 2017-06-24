package com.aos.io2017;

import android.util.Log;

/**
 * Created by aortuno on 11/24/2016.
 */
public class Application extends android.app.Application {

    public final static String tag = "IOExtended2017";

    private static Application instance = null;

    public static Application getInstance() {
        if (instance == null) {
            instance = new Application();
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(Application.tag, getString(R.string.app_name));
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                handleUncaughtException(thread, e);
            }
        });

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public void handleUncaughtException(Thread thread, Throwable e) {
        e.printStackTrace();
        Log.e(Application.tag, e.getMessage());
        System.exit(1);
    }

}
