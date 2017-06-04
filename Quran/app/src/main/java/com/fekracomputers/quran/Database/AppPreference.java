package com.fekracomputers.quran.Database;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fekracomputers.quran.Application.QuranApplication;
import com.fekracomputers.quran.Utilities.AppConstants;

/**
 * Run time configuration resources
 */
public class AppPreference {

    /**
     * Open configuration file
     *
     * @return SharedPreferences object
     */
    public static SharedPreferences OpenConfigPreferences() {
        try {
            return QuranApplication.getInstance().getSharedPreferences(AppConstants.Preferences.CONFIG, Context.MODE_PRIVATE);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Function to set download statue
     *
     * @param flag in download or not
     */
    public static void Downloading(boolean flag) {
        SharedPreferences.Editor editor = OpenConfigPreferences().edit();
        editor.putBoolean(AppConstants.Preferences.DOWNLOAD_STATUS, flag);
        editor.apply();
    }

    /**
     * Function to set after download statue
     *
     * @param statues Download statue
     */
    public static void DownloadStatues(int statues) {
        SharedPreferences.Editor editor = OpenConfigPreferences().edit();
        editor.putInt(AppConstants.Preferences.DOWNLOAD_STATUS_TEXT, statues);
        editor.apply();
    }

    /**
     * Function to set witch type of download you do
     *
     * @param type integer refer to download type
     */
    public static void setDownloadType(int type) {
        SharedPreferences.Editor editor = OpenConfigPreferences().edit();
        editor.putInt(AppConstants.Preferences.DOWNLOAD_TYPE, type);
        editor.apply();
    }

    /**
     * Function to set latest download id
     *
     * @param id ID of object you download
     */
    public static void setDownloadID(int id) {
        SharedPreferences.Editor editor = OpenConfigPreferences().edit();
        editor.putInt(AppConstants.Preferences.DOWNLOAD_ID, id);
        editor.apply();
    }

    /**
     * Function to set application language arabic
     *
     * @param isArabic flag of arabic or not
     */
    public static void setApplicationLanguage(boolean isArabic) {
        SharedPreferences.Editor editor = OpenConfigPreferences().edit();
        editor.putBoolean(AppConstants.Preferences.LANGUAGE, isArabic);
    }

    /**
     * Function to save last read page
     *
     * @param pageNumber Last Page read number
     */
    public static void setLastPageRead(int pageNumber) {
        SharedPreferences.Editor editor = OpenConfigPreferences().edit();
        editor.putInt(AppConstants.Preferences.LAST_PAGE_NUMBER, pageNumber);
        editor.apply();
    }

    /**
     * Function to save screen resolution
     *
     * @param resolution Screen max resolution
     */
    public static void setScreenResolution(int resolution) {
        SharedPreferences.Editor editor = OpenConfigPreferences().edit();
        editor.putInt(AppConstants.Preferences.SCREEN_RESOLUTION, resolution);
        editor.apply();
    }

    /**
     * Function to set default tafseer
     *
     * @param tafseerID tafseer book id
     */
    public static void setDefaultTafseer(int tafseerID) {
        SharedPreferences.Editor editor = OpenConfigPreferences().edit();
        editor.putInt(AppConstants.Preferences.DEFAULT_EXPLANATION, tafseerID);
        editor.commit();
    }

    /**
     * Function to set selection verse
     */
    public synchronized static void setSelectionVerse(String info) {
        SharedPreferences.Editor editor = OpenConfigPreferences().edit();
        editor.putString(AppConstants.Preferences.SELECT_VERSE, info);
        editor.commit();
    }

    /**
     * Function to get download states
     *
     * @return Download running or not
     */
    public static boolean isDownloading() {
        SharedPreferences preferences = OpenConfigPreferences();
        boolean isDownloading = preferences.getBoolean(AppConstants.Preferences.DOWNLOAD_STATUS, false);
        return isDownloading;
    }

    /**
     * Function get after download statues
     *
     * @return After download statue
     */
    public static int getDownloadStatues() {
        SharedPreferences preferences = OpenConfigPreferences();
        int downloadStatues = preferences.getInt(AppConstants.Preferences.DOWNLOAD_STATUS_TEXT, -1);
        return downloadStatues;
    }

    /**
     * Function to get last page read number
     *
     * @return Last page read number
     */
    public static int getLastPageRead() {
        SharedPreferences preferences = OpenConfigPreferences();
        int pageNumber = preferences.getInt(AppConstants.Preferences.LAST_PAGE_NUMBER, -1);
        return pageNumber;
    }


    /**
     * Function to get screen resolution
     *
     * @return
     */
    public static int getScreenResolution() {
        SharedPreferences preferences = OpenConfigPreferences();
        int resolution = preferences.getInt(AppConstants.Preferences.SCREEN_RESOLUTION, -1);
        return resolution;
    }

    /**
     * Function to get download type of download
     *
     * @return download type tafseer , voice , images or voiceDatabases
     */
    public static int getDownloadType() {
        SharedPreferences preferences = OpenConfigPreferences();
        int type = preferences.getInt(AppConstants.Preferences.DOWNLOAD_TYPE, -1);
        return type;
    }

    /**
     * Function to get download id
     *
     * @return download id
     */
    public static int getDownloadID() {
        SharedPreferences preferences = OpenConfigPreferences();
        int id = preferences.getInt(AppConstants.Preferences.DOWNLOAD_ID, -1);
        return id;
    }

    /**
     * Function to get default tafseer book id
     *
     * @return Tafseer book id
     */
    public static int getDefaultTafseer() {
        SharedPreferences preferences = OpenConfigPreferences();
        int type = preferences.getInt(AppConstants.Preferences.DEFAULT_EXPLANATION, -1);
        return type;
    }

    /**
     * Function to set translation text size
     */
    public static void setTranslationTextSize(String size) {
        SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(QuranApplication.getInstance()).edit();
        preferences.putString(AppConstants.Preferences.TRANSLATION_SIZE, size);
        preferences.commit();
    }

    /**
     * Function to get volume key navigation
     *
     * @return Statues of volume key navigation
     */
    public static boolean isVolumeKeyNavigation() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(QuranApplication.getInstance());
        return preferences.getBoolean(AppConstants.Preferences.VOLUME_NAVIGATION, false);
    }

    /**
     * Function to get if screen rotation allowed or not
     *
     * @return statue of rotation disable or enable
     */
    public static boolean isScreeRotationDisabled() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(QuranApplication.getInstance());
        return preferences.getBoolean(AppConstants.Preferences.ORIENTATION, false);
    }


    /**
     * Function to get app in public mood or not
     *
     * @return Statue of arabic mood
     */
    public static boolean isArabicMood(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(AppConstants.Preferences.ARABIC_MOOD, false);
    }


    /**
     * Function to get appear aya or not in translations
     *
     * @return statues of appear aya or not
     */
    public static boolean isAyaAppear() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(QuranApplication.getInstance());
        return preferences.getBoolean(AppConstants.Preferences.AYA_APPEAR, true);
    }


    /**
     * Function to get translation text size
     *
     * @return Translation text size
     */
    public static String getTranslationTextSize() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(QuranApplication.getInstance());
        return preferences.getString(AppConstants.Preferences.TRANSLATION_SIZE, "large");
    }

    /**
     * Function to check stream or download mood
     *
     * @return Flag of stream or not
     */
    public static Boolean isStreamMood() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(QuranApplication.getInstance());
        return preferences.getBoolean(AppConstants.Preferences.STREAM, true);
    }
    /**
     * Function to check NightMood
     *
     * @return Flag of NightMood
     */
    public static Boolean isNightMood(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(QuranApplication.getInstance());
        return preferences.getBoolean(AppConstants.Preferences.NIGHT_MOOD, false);
    }

    /**
     * Function to get selected aya
     *
     * @return Selected aya object
     */
    public synchronized static String getSelectionVerse() {
        SharedPreferences preferences = OpenConfigPreferences();
        return preferences.getString(AppConstants.Preferences.SELECT_VERSE, "");
    }


}
