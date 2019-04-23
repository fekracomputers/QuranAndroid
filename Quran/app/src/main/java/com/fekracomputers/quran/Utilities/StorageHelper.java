package com.fekracomputers.quran.Utilities;

import android.content.Context;
import android.os.Environment;

import com.fekracomputers.quran.R;

public class StorageHelper {

    private static boolean externalStorageReadable, externalStorageWritable;

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

//    public static String getDownloadPath(Context context){
//        if (isExternalStorageWritable()){
//            return  Environment
//                    .getExternalStorageDirectory()
//                    .getAbsolutePath() + context.getResources().getString(R.string.app_folder_path);
//        }
//        return Environment
//                .get()
//                .getAbsolutePath() + context.getResources().getString(R.string.app_folder_path);
//    }

}