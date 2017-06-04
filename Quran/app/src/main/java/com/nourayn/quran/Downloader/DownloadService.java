package com.nourayn.quran.Downloader;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.nourayn.quran.Database.AppPreference;
import com.nourayn.quran.Utilities.AppConstants;

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
        List<String> downloadLinks = extras.getStringArrayList(AppConstants.Download.DOWNLOAD_LINKS);

        if(downloadLinks == null){
            downloadManager = new DownloadManager(this, true);
            downloadManager.execute(downloadURL, downloadLocation);
        } else{
            downloadManager = new DownloadManager(this, true , downloadLinks);
            downloadManager.execute("", downloadLocation);
        }

        return START_NOT_STICKY;
    }

    /**
     * Function on destroy service set downloading statues
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        downloadManager.stopDownload = true ;
        AppPreference.Downloading(false);
    }
}
