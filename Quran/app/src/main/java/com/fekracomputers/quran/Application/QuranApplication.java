package com.fekracomputers.quran.Application;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.fekracomputers.quran.Database.AppPreference;

import java.util.Locale;

/**
 * Class to overwrite application class
 */
public class QuranApplication extends Application {
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();

        if (appContext == null) {
            //take instance of application context
            appContext = getApplicationContext();
        }
    }

    public static Context getInstance() {

        //Check application language
        Locale locale;
        if (AppPreference.isArabicMood(appContext))
            locale = new Locale("ar");
        else
            locale = new Locale("en");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        appContext.getResources().updateConfiguration(config, appContext.getResources().getDisplayMetrics());

        return appContext;
    }




}
