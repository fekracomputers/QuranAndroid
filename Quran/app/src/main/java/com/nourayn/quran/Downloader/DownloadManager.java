package com.nourayn.quran.Downloader;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.nourayn.quran.Database.AppPreference;
import com.fekracomputers.quran.R;
import com.nourayn.quran.UI.Activities.MainActivity;
import com.nourayn.quran.Utilities.AppConstants;
import com.nourayn.quran.Utilities.UnZipping;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A Class Responsible For download management
 * Async task class can run in background as a service or normal thread
 */
public class DownloadManager extends AsyncTask<String, Long, Boolean> {
    public static final int DOWNLOAD_CHUNK_SIZE = 1024 * 3;
    private ProgressBar downloadProgressBar;
    private List<String> downloadLinks;
    private Context context;
    private TextView downloadInfo;
    private RemoteViews remoteViews;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private boolean notificationDownloaderFlag;
    private int notificationDivider;
    private String fileExtension, fileName, filePath;
    private PendingIntent notificationPending;
    private Intent openApplication;
    public boolean stopDownload;
    private boolean aboveLollipopFlag;

    /**
     * Class constructor. for download manager
     *
     * @param context                    application context to show notification after finish
     * @param notificationDownloaderFlag flag to appear notification progressbar
     */
    public DownloadManager(Context context, boolean notificationDownloaderFlag) {

        this.context = context;
        this.notificationDownloaderFlag = notificationDownloaderFlag;
        init();
    }

    /**
     * Class constructor. for download manager
     *
     * @param downloadProgressBar        gui progressbar
     * @param notificationDownloaderFlag flag to appear notification progressbar
     */
    public DownloadManager(ProgressBar downloadProgressBar, boolean notificationDownloaderFlag) {
        this.downloadProgressBar = downloadProgressBar;
        this.notificationDownloaderFlag = notificationDownloaderFlag;
        init();
    }

    /**
     * Class constructor. for download manager
     *
     * @param context                    application context to show notification after finish
     * @param downloadProgressBar        gui progressbar
     * @param notificationDownloaderFlag flag to appear notification progressbar
     */
    public DownloadManager(Context context, ProgressBar downloadProgressBar, boolean notificationDownloaderFlag) {
        this.downloadProgressBar = downloadProgressBar;
        this.context = context;
        this.notificationDownloaderFlag = notificationDownloaderFlag;
        init();
    }

    /**
     * Class constructor. for download manager
     *
     * @param context                    application context to show notification after finish
     * @param downloadProgressBar        gui progressbar
     * @param downloadInfo               textview to show download information
     * @param notificationDownloaderFlag flag to appear notification progressbar
     */
    public DownloadManager(Context context, ProgressBar downloadProgressBar, TextView downloadInfo, boolean notificationDownloaderFlag) {
        this.context = context;
        this.downloadProgressBar = downloadProgressBar;
        this.downloadInfo = downloadInfo;
        this.notificationDownloaderFlag = notificationDownloaderFlag;
        init();
    }


    /**
     * Public constructor for download links
     *
     * @param context                    Application Context
     * @param notificationDownloaderFlag Flag to show notification
     * @param downloadLinks              List of download links
     */
    public DownloadManager(Context context, boolean notificationDownloaderFlag, List<String> downloadLinks) {

        this.context = context;
        this.downloadLinks = downloadLinks;
        this.notificationDownloaderFlag = notificationDownloaderFlag;
        init();
    }

    /**
     * Function to init download objects
     */
    private void init() {
        openApplication = new Intent(context, MainActivity.class);
        notificationPending = PendingIntent.getActivity(context, 0,
                openApplication, 0);
        aboveLollipopFlag = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    }

    /**
     * Start download file from url
     *
     * @param url the download url
     * @return flag download success or not
     */
    @Override
    protected Boolean doInBackground(String... url) {

        try {
            if (downloadLinks != null) {
                return multiDownload(downloadLinks, url[1]);
            } else {
                return singleDownload(url[0], url[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }


    /**
     * Function to single download
     *
     * @param link             Download link
     * @param downloadLocation Download downloadLocation
     * @return Flag or download success or not
     * @throws IOException
     */
    public boolean  singleDownload(String link, String downloadLocation) throws IOException {
        //file name
        fileName = link.substring(link.lastIndexOf('/') + 1, link.length());
        filePath = downloadLocation; //url[1];
        String splits[] = fileName.split("\\.");
        fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());

        //divided update notification
        notificationDivider = 0;

        //new http connection
        OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.MINUTES).
                readTimeout(15, TimeUnit.MINUTES).build();

        Call call = httpClient.newCall(new Request.Builder().url(link).get().build());
        Response response = call.execute();

        if (notificationDownloaderFlag) showNotificationDownloader();

        //susses request
        if (response.code() == 200) {
            InputStream inputStream = null;
            OutputStream output = null;

            try {
                //path response to input stream
                inputStream = response.body().byteStream();
                output = new FileOutputStream(filePath + "/" + fileName);
                byte[] buffer = new byte[DOWNLOAD_CHUNK_SIZE];
                long download = 0;
                long target = response.body().contentLength();
                int oneBlock = Math.round((target / 100));
                int oneLoop = (oneBlock / DOWNLOAD_CHUNK_SIZE);
                //set progress zero and response length
                publishProgress(0L, target);
                while (true) {
                    //flag to stop download
                    if (stopDownload) break;

                    notificationDivider++;
                    //read buffer
                    int read = inputStream.read(buffer);
                    if (read == -1) break;
                    download += read;
                    if (notificationDivider == oneLoop) {
                        publishProgress(download, target);
                        notificationDivider = 0;
                    }
                    if (isCancelled()) break;
                    output.write(buffer, 0, read);
                }

                return download == target;

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (inputStream != null) inputStream.close();
            }

        } else {
            return false;
        }
    }

    /**
     * Function to multi download
     *
     * @param links            List of links
     * @param downloadLocation Download destination
     * @return Flag of download success or not
     * @throws IOException
     */
    public synchronized boolean multiDownload(List<String> links, String downloadLocation) throws IOException {

        if (notificationDownloaderFlag) showNotificationDownloader();
        int counter = 0;
        publishProgress(0L, Long.valueOf(links.size()));
        //foreach for the all links
        for (String linkItem : links) {

            //flag to stop download
            if (stopDownload) break;

            //update progress
            publishProgress(Long.valueOf(counter++), Long.valueOf(links.size()));

            //file name
            fileName = linkItem.substring(linkItem.lastIndexOf('/') + 1, linkItem.length());
            filePath = downloadLocation;
            String splits[] = fileName.split("\\.");
            fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());

            //divided update notification
            notificationDivider = 0;

            //new http connection
            OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.MINUTES).
                    readTimeout(15, TimeUnit.MINUTES).build();

            Call call = httpClient.newCall(new Request.Builder().url(linkItem).get().build());
            Response response = call.execute();


            //susses request
            if (response.code() == 200) {
                InputStream inputStream = null;
                OutputStream output = null;

                try {
                    //path response to input stream
                    inputStream = response.body().byteStream();
                    output = new FileOutputStream(filePath + "/" + fileName);
                    byte[] buffer = new byte[DOWNLOAD_CHUNK_SIZE];
                    //set progress zero and response length

                    while (true) {

                        //flag to stop download
                        if (stopDownload) break;

                        notificationDivider++;
                        //read buffer
                        int read = inputStream.read(buffer);
                        if (read == -1) break;
                        if (isCancelled()) break;
                        output.write(buffer, 0, read);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    if (inputStream != null) inputStream.close();
                }

            } else {
                return false;
            }
        }
        return true;

    }

    /**
     * Function show download information
     *
     * @param values download information values
     */
    @Override
    protected void onProgressUpdate(Long... values) {

        if (downloadProgressBar != null) {
            downloadProgressBar.setMax(values[1].intValue());
            downloadProgressBar.setProgress(values[0].intValue());
        }

        if (downloadInfo != null) {
            downloadInfo.setText(values[1] / 1000000 + "/" + values[0] / 1000000);
        }

        if (notificationDownloaderFlag) {
            //remoteViews.setProgressBar(R.id.progressBar2, , , false);
            builder.setProgress(values[1].intValue(), values[0].intValue(), false);
            notificationManager.notify(0, builder.build());
        }

        if (context != null) {
            if (context instanceof Service) {
                Intent i = new Intent(AppConstants.Download.INTENT);
                i.putExtra(AppConstants.Download.NUMBER, values[0]);
                i.putExtra(AppConstants.Download.MAX, values[1]);
                i.putExtra(AppConstants.Download.DOWNLOAD, AppConstants.Download.IN_DOWNLOAD);
                LocalBroadcastManager.getInstance(context).sendBroadcast(i);
            }

        }

    }

    /**
     * Action after download finished of not
     *
     * @param result flag download success or not
     */
    @Override
    protected void onPostExecute(Boolean result) {

        //notify download complete
        if (context != null)
            Toast.makeText(context, result == true ?
                            context.getString(R.string.download_complete) :
                            context.getString(R.string.download_failed)
                    , Toast.LENGTH_LONG).show();

        //pass the download statue to notification
        if (notificationDownloaderFlag) {
            notificationManager.cancel(0);
            if (result) {
                showCompleteNotification();
                AppPreference.DownloadStatues(AppConstants.Preferences.DOWNLOAD_SUCCESS);
            } else {
                showFailedNotification();
                AppPreference.DownloadStatues(AppConstants.Preferences.DOWNLOAD_FAILED);
            }
        }

        //run extraction service if zip file or stop service
        if (fileExtension.toLowerCase().equals("zip") && result) {
            new UnZipping(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filePath, fileName);
        } else {
            //send broadcast of success or failed
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(AppConstants.Download.INTENT)
                    .putExtra(AppConstants.Download.DOWNLOAD, result == true ?
                            AppConstants.Download.SUCCESS :
                            AppConstants.Download.FAILED));
            if (context instanceof Service)
                ((Service) context).stopService(new Intent(context, DownloadService.class));
        }

    }

    /**
     * Initialize and show notification of download statue
     */
    public void showNotificationDownloader() {

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_download_progress);
        builder = new NotificationCompat.Builder(context)
                .setSmallIcon(aboveLollipopFlag ? R.drawable.ic_quran_trans : R.drawable.logo)
                .setColor(Color.parseColor("#3E686A"))
                .setProgress(100, 0, false)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.download))
                .setOngoing(true);
        builder.setContentIntent(notificationPending);
        notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    /**
     * Initialize and show notification of download completes
     */
    public void showCompleteNotification() {
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_download_finished);
        builder = new NotificationCompat
                .Builder(context)
                .setSmallIcon(aboveLollipopFlag ? R.drawable.ic_quran_trans : R.drawable.logo)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.download_complete))
                .setColor(Color.parseColor("#3E686A"));
        builder.setContentIntent(notificationPending);
        notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    /**
     * Initialize and show notification of download failed
     */
    public void showFailedNotification() {
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_download_failed);
        builder = new NotificationCompat
                .Builder(context)
                .setSmallIcon(aboveLollipopFlag ? R.drawable.ic_quran_trans : R.drawable.logo)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.download_failed))
                .setColor(Color.parseColor("#3E686A"));
        notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

}
