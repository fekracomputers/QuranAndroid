package com.fekracomputers.quran.Utilities;

import android.content.Context;
import android.os.Environment;

import com.fekracomputers.quran.Application.QuranApplication;
import com.fekracomputers.quran.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to validate app sources databases and images
 */
public class QuranValidateSources {


    /**
     * Fuction to validate app main folder exists
     *
     * @return folder found of not
     */
    public static boolean validatAppMainFoldersAndFiles(Context context) {

        boolean foundQuranPages = false;

        File main = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                context.getResources().getString(R.string.app_folder_path));

        File database = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                context.getResources().getString(R.string.app_folder_path) + "/" + "quran.sqlite");

        if (!main.exists()) {
            main.mkdirs();
            return false;
        }

        if (!database.exists()) {
            main.mkdirs();
            return false;
        }

        File[] files = main.listFiles();
        for (File file : files) {
            if (file.getName().contains("quranpages")) {
                if (!(file.getName().contains(".zip"))) foundQuranPages = true;
                break;
            }
        }
        if (foundQuranPages != true) return false;
        return true;
    }

    /**
     * function to validate all app main files found and databases
     *
     * @return
     */
    public boolean validateAppFilesAndDatabase() {
        return false;
    }

    /**
     * Function to get all downloaded tafaseer and translation
     *
     * @return List of tafaseer ids
     */
    public static List<Integer> getDownloadedTransaltions() {
        List<Integer> tafaseerIDs = new ArrayList<Integer>();
        File tafaseer = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + QuranApplication.getInstance().getString(R.string.app_folder_path) + "/tafaseer");

        if (tafaseer.exists()) {
            String databaseName = null;
            File[] files = tafaseer.listFiles();
            for (File file : files) {
                if (!file.getName().contains("journal")) {
                    databaseName = file.getName().replace("tafseer", "").replace(".sqlite", "");
                    tafaseerIDs.add(Integer.parseInt(databaseName));
                }
            }
        }
        return tafaseerIDs;

    }

    /**
     * Function to validate aya found or not
     *
     * @param context Application context
     * @param reader  Reader id
     * @param aya     Aya id
     * @param sura    Sura id
     * @return Flag of found or not
     */
    public static boolean validateAyaAudio(Context context, int reader, int aya, int sura) {

        //create file name from aya id and sura id
        int suraLength = String.valueOf(sura).trim().length();
        String suraID = sura + "";
        int ayaLength = String.valueOf(aya).trim().length();
        String ayaID = aya + "";

        if (suraLength == 1)
            suraID = "00" + sura;
        else if (suraLength == 2)
            suraID = "0" + sura;

        if (ayaLength == 1)
            ayaID = "00" + aya;
        else if (ayaLength == 2)
            ayaID = "0" + aya;

        //Audio file path
        String filePath = Environment
                .getExternalStorageDirectory()
                .getAbsolutePath()
                + context.getString(R.string.app_folder_path)
                + "/Audio/" + reader+"/"+suraID
                + ayaID + AppConstants.Extensions.MP3;

        //check file found or not
        File file = new File(filePath);
        if (!file.exists()) return false;

        return true;
    }

}
