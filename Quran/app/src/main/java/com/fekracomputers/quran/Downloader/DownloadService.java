package com.fekracomputers.quran.Downloader;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.support.annotation.Nullable;

import com.fekracomputers.quran.Database.AppPreference;
import com.fekracomputers.quran.Utilities.AppConstants;

import java.util.List;

/**
 * Service class to download file
 */
public class DownloadService extends Service {
    private DownloadManager downloadManager ;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Function to start service
     *
     * @param intent  Activity intent
     * @param flags   to start sticky or not
     * @param startId Service id
     * @return usually start or start once
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AppPreference.Downloading(true);
        Bundle extras = intent.getExtras();
        String downloadURL = extras.getString(AppConstants.Download.DOWNLOAD_URL);
        String downloadLocation = extras.getString(AppConstants.Download.DOWNLOAD_LOCATION);
        int type = extras.getInt(AppConstants.Download.TYPE , -1);
        List<String> downloadLinks = extras.getStringArrayList(AppConstants.Download.DOWNLOAD_LINKS);

        if(downloadLinks == null){
                downloadManager = new DownloadManager(this, true ,type);
                downloadManager.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, downloadURL, downloadLocation);
        } else{
                downloadManager = new DownloadManager(this, true, downloadLinks ,type);
                downloadManager.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "", downloadLocation);
        }

        return START_NOT_STICKY;
    }

    /**
     * Function on destroy service set downloading statues
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (downloadManager != null) {
            downloadManager.stopDownload = true;
        }
        AppPreference.Downloading(false);
    }


}
