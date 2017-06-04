package com.nourayn.quran.Utilities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;

import com.nourayn.quran.Database.AppPreference;

/**
 * Class to get mobile configurations
 */
public class QuranConfig {
    public static int maxResolution;
    private static final String DOWNLOAD_LINK = "http://www.mindtrack.net/data/quran/pages/quranpages_";
    private static final String File_TYPE = ".zip";

    /**
     * Function to get screen resolution
     *
     * @return width and height
     */
    public static String getResolutionURLLink(Context context) {
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        maxResolution = size.x > size.y ? size.x : size.y;

        if (maxResolution <= 320) {
            Log.d("resolution", "320");
            AppPreference.setScreenResolution(320);
            return DOWNLOAD_LINK + 320 + File_TYPE;
        } else if (maxResolution <= 512) {
            Log.d("resolution", "512");
            AppPreference.setScreenResolution(512);
            return DOWNLOAD_LINK + 512 + File_TYPE;
        } else if (maxResolution <= 480) {
            Log.d("resolution", "480");
            AppPreference.setScreenResolution(480);
            return DOWNLOAD_LINK + 480 + File_TYPE;
        } else if (maxResolution <= 800) {
            Log.d("resolution", "800");
            AppPreference.setScreenResolution(800);
            return DOWNLOAD_LINK + 800 + File_TYPE;
        } else if (maxResolution <= 1024) {
            Log.d("resolution", "1024");
            AppPreference.setScreenResolution(1024);
            return DOWNLOAD_LINK + 1024 + File_TYPE;
        } else if (maxResolution <= 1260) {
            Log.d("resolution", "1260");
            AppPreference.setScreenResolution(1260);
            return DOWNLOAD_LINK + 1260 + File_TYPE;
        } else if (maxResolution <= 1920) {
            Log.d("resolution", "1920");
            AppPreference.setScreenResolution(1920);
            return DOWNLOAD_LINK + 1920 + File_TYPE;
        } else {
            Log.d("resolution", "1920");
            AppPreference.setScreenResolution(1920);
            return DOWNLOAD_LINK + 1920 + File_TYPE;
        }
    }

}
