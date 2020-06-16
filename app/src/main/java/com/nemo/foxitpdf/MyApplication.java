package com.nemo.foxitpdf;

import android.app.Application;
import android.content.res.Configuration;

import com.foxit.sdk.Localization;

import androidx.multidex.MultiDex;

public class MyApplication extends Application {
    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        MultiDex.install(this);
        App.instance().setApplicationContext(this);
        Localization.setCurrentLanguage(this, Localization.getCurrentLanguage(this));
        if (!App.instance().checkLicense()) {
            return;
        }
    }

    public static MyApplication getInstance() {
        if (instance == null) {
            instance = new MyApplication();
        }
        return instance;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Localization.setCurrentLanguage(this, Localization.getCurrentLanguage(this));
    }

}
