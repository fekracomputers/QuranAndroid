package com.fekracomputers.quran.Downloader;

import android.app.Notification;
import android.app.NotificationChannel;
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
import com.fekracomputers.quran.Database.AppPreference;
import com.fekracomputers.quran.R;
import com.fekracomputers.quran.UI.Activities.MainActivity;
import com.fekracomputers.quran.Utilities.AppConstants;
import com.fekracomputers.quran.Utilities.Settingsss;
import com.fekracomputers.quran.Utilities.UnZipping;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.util.Log;
/**
 * A Class Responsible For download management
 * Async task class can run in background as a service or normal thread
 */
public class DownloadManager extends AsyncTask<String, Long, Boolean> {


    final String CHANNEL_ID = "DOWNLOAD_SERVICE_CHANNEL_ID";
    final String CHANNEL_NAME = "DOWNLOAD_SERVICE_CHANNEL_NAME";
    final String CHANNEL_DESCRIPTION = "DOWNLOAD_SERVICE_CHANNEL_DESCRIPTION";



    public static final int DOWNLOAD_CHUNK_SIZE = 1024 * 3;
    private int downloadType = -1;
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
    private OkHttpClient mOkHttpClient;
    int flag=0;
    String TAG="StopDownloading";

    /**
     * Class constructor. for download manager
     *
     * @param context                    application context to show notification after finish
     * @param notificationDownloaderFlag flag to appear notification progressbar
     */
    public DownloadManager(Context context, boolean notificationDownloaderFlag , int downloadType) {

        Log.i("TAFSEER_DOWN_TAG" , "start download tafseer");
        this.downloadType = downloadType;
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
    public DownloadManager(ProgressBar downloadProgressBar, boolean notificationDownloaderFlag, int downloadType) {
        this.downloadType = downloadType;
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
    public DownloadManager(Context context, ProgressBar downloadProgressBar, boolean notificationDownloaderFlag, int downloadType) {
        this.downloadType = downloadType;
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
    public DownloadManager(Context context, ProgressBar downloadProgressBar, TextView downloadInfo, boolean notificationDownloaderFlag, int downloadType) {
        this.downloadType = downloadType;
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
    public DownloadManager(Context context, boolean notificationDownloaderFlag, List<String> downloadLinks, int downloadType) {

        Log.i("TAFSEER_DOWN_TAG" , "start download tafseer");
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
            Log.e(DownloadManager.class.getSimpleName(), "e : " + e.getLocalizedMessage());
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
        CacheControl cacheControl = null;
        fileName = link.substring(link.lastIndexOf('/') + 1, link.length());
        filePath = downloadLocation; //url[1];
        String splits[] = fileName.split("\\.");
        fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());

        //divided update notification
        notificationDivider = 0;

        //new http connection






        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(context.getCacheDir(), cacheSize);


        OkHttpClient httpClient = new OkHttpClient.Builder().cache(cache).connectTimeout(15, TimeUnit.MINUTES).
                readTimeout(15, TimeUnit.MINUTES).build();
        //private OkHttpClient mOkHttpClient;
        Request request = new Request.Builder()
                .url(link)
                .build();

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


                    int internetStatus = Settingsss.checkInternetStatus(context);

                    //flag to stop download
                    if (stopDownload){

                        Log.e(TAG, "cancel her: ");
                        flag=1;
                        break;
                    }
                    notificationDivider++;
                    //read buffer
                    int read = inputStream.read(buffer);
                    if (read == -1) break;
                    download += read;
                    if (notificationDivider == oneLoop) {
                        publishProgress(download, target);
                        notificationDivider = 0;
                    }
                    if (isCancelled()){
                        flag=1;

                        Log.e(TAG, "Stop: ");
//                        break;
                    }
                    output.write(buffer, 0, read);
                }
                Log.d(TAG, "Stop: "+download);
                return download == target;

            } catch (IOException e) {
                Log.d(TAG, "Stop: "+e.getMessage());

                e.printStackTrace();
                return false;
            } finally {
                if (inputStream != null) inputStream.close();
            }

        } else {
            return false;
        }
    }


//    /**
//     * Function to multi download
//     *
//     * @param links            List of links
//     * @param downloadLocation Download destination
//     * @return Flag of download success or not
//     * @throws IOException
//     */
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
            // if (response.code() == 200) {
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
                    if (stopDownload) {

                        Log.e(TAG, "cancel her: ");
                        flag=1;
                        break;
                    }

                    notificationDivider++;
                    //read buffer
                    int read = inputStream.read(buffer);
                    if (read == -1) break;
                    if (isCancelled()){
                        flag=1;

//                            break;
                    }
                    output.write(buffer, 0, read);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (inputStream != null) inputStream.close();
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
            downloadInfo.setText(values[1] / 1000000 + "/" + values[0] / 1000000+" ("+(int)(( values[0] / 1000000*100)/(values[1] / 1000000))+"% )");
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
                i.putExtra(AppConstants.Download.TYPE , downloadType);
                i.putExtra(AppConstants.Download.DOWNLOAD, AppConstants.Download.IN_DOWNLOAD);
                LocalBroadcastManager.getInstance(context).sendBroadcast(i);
            }

        }

    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

    }

    /**
     * Action after download finished of not
     *
     * @param result flag download success or not
     */
    @Override
    protected void onPostExecute(Boolean result) {

        //notify download complete
        //notify download cancel
        if (flag == 1) {
            Toast.makeText(context,
                    context.getString(R.string.success_download_canceled)
                    , Toast.LENGTH_LONG).show();
            new File(filePath + "/" + fileName).delete();
            showcancelNotification();
//            downloadProgressBar.setProgress(0);
            notificationManager.cancel(0);
        } else {
            //notify download cancel
            if (context != null)
                if (flag == 1) {
                    Toast.makeText(context,
                            context.getString(R.string.success_download_canceled)
                            , Toast.LENGTH_LONG).show();
                    showcancelNotification();
                }else {
                    Toast.makeText(context, result ?
                                    context.getString(R.string.download_complete) :
                                    "Download failed due to the connection is lost"
                            , Toast.LENGTH_LONG).show();
                }
            //pass the download statue to notification
            if (notificationDownloaderFlag) {
                try {
                    notificationManager.cancel(0);
                }catch (Exception e){e.printStackTrace();}
                if (result) {
                    showCompleteNotification();
                    AppPreference.DownloadStatues(AppConstants.Preferences.DOWNLOAD_SUCCESS);
                } else {
                    showFailedNotification();

                    AppPreference.DownloadStatues(AppConstants.Preferences.DOWNLOAD_FAILED);
                }
            }
        }
        //run extraction service if zip file or stop service
        if (fileExtension.toLowerCase().equals("zip") && result) {
            Log.i("CHECK_INTERNET_LOST" , "in zipping");
            new UnZipping(context , downloadType).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filePath, fileName);
        } else {

            Log.i("CHECK_INTERNET_LOST" , "send broad cast result : "+result);
            //send broadcast of success or failed
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(AppConstants.Download.INTENT)
                    .putExtra(AppConstants.Download.DOWNLOAD, result  ?
                            AppConstants.Download.SUCCESS :
                            AppConstants.Download.FAILED)
                    .putExtra(AppConstants.Download.TYPE , downloadType));
            if (context instanceof Service) {
                ((Service) context).stopService(new Intent(context, DownloadService.class));
                context.stopService(new Intent(context , DownloadTafseerService.class));
            }
        }

    }


    /**
     * Init notification channels for android 8.0 and higher
     */
    private String createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        return CHANNEL_ID;
    }



    /**
     * Initialize and show notification of download statue
     */
    public void showNotificationDownloader() {


        remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_download_progress);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelID = createNotificationChannel(context);
            builder = new NotificationCompat.Builder(context, channelID);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        builder.setSmallIcon(aboveLollipopFlag ? R.drawable.ic_quran_trans : R.drawable.logo)
                .setColor(Color.parseColor("#3E686A"))
                .setProgress(100, 0, false)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.download)+"")
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelID = createNotificationChannel(context);
            builder = new NotificationCompat.Builder(context, channelID);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        builder.setSmallIcon(aboveLollipopFlag ? R.drawable.ic_quran_trans : R.drawable.logo)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelID = createNotificationChannel(context);
            builder = new NotificationCompat.Builder(context, channelID);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        builder.setSmallIcon(aboveLollipopFlag ? R.drawable.ic_quran_trans : R.drawable.logo)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.download_failed))
                .setColor(Color.parseColor("#3E686A"));
        notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
    /**
     * Initialize and show notification of download canceled
     */
    public void showcancelNotification() {
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_download_failed);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelID = createNotificationChannel(context);
            builder = new NotificationCompat.Builder(context, channelID);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        builder.setSmallIcon(aboveLollipopFlag ? R.drawable.ic_quran_trans : R.drawable.logo)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("canceled")
                .setColor(Color.parseColor("#3E686A"));
        notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
}
