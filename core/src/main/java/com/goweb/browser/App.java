package com.goweb.browser;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import com.goweb.browser.utils.SettingsUtils;

import java.util.Locale;

public class App extends Application {

    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(wrapContext(base));
    }

    private Context wrapContext(Context base) {
        int lang = SettingsUtils.getLanguage(base);
        if (lang == SettingsUtils.LANG_AUTO) {
            return base;
        }
        Locale locale;
        if (lang == SettingsUtils.LANG_CHINESE) {
            locale = Locale.SIMPLIFIED_CHINESE;
        } else {
            locale = Locale.ENGLISH;
        }
        Resources res = base.getResources();
        Configuration config = res.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
            return base.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
            return base;
        }
    }

    public static App getInstance() {
        return instance;
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public void applyLanguage() {
        int lang = SettingsUtils.getLanguage();
        if (lang == SettingsUtils.LANG_AUTO) {
            return;
        }

        Locale locale;
        if (lang == SettingsUtils.LANG_CHINESE) {
            locale = Locale.SIMPLIFIED_CHINESE;
        } else {
            locale = Locale.ENGLISH;
        }

        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }

        resources.updateConfiguration(configuration, displayMetrics);
        
        // Also update application context resources
        Context appContext = getApplicationContext();
        Resources appResources = appContext.getResources();
        Configuration appConfig = appResources.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            appConfig.setLocale(locale);
        } else {
            appConfig.locale = locale;
        }
        appResources.updateConfiguration(appConfig, appResources.getDisplayMetrics());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyLanguage();
    }
}
