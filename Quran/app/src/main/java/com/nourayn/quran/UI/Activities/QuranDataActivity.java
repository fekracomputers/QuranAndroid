package com.nourayn.quran.UI.Activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nourayn.quran.Database.AppPreference;
import com.nourayn.quran.Downloader.DownloadService;
import com.fekracomputers.quran.R;
import com.nourayn.quran.Utilities.AppConstants;
import com.nourayn.quran.Utilities.FileManager;
import com.nourayn.quran.Utilities.QuranConfig;
import com.nourayn.quran.Utilities.Settings;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


/**
 * Activity class to download application resources
 */
public class QuranDataActivity extends Activity {
    public ProgressBar downloadProgress;
    public TextView downloadInformation;
    public Button StartQuran;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quran_data);
        init();

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(AppConstants.Download.INTENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
    }

    /**
     * Init Quran data activity
     */
    private void init() {

        downloadProgress = (ProgressBar) findViewById(R.id.progressBar);
        downloadInformation = (TextView) findViewById(R.id.textView);
        StartQuran = (Button) findViewById(R.id.button);

        StartQuran.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start home activity
                Intent Home = new Intent(QuranDataActivity.this, HomeActivity.class);
                startActivity(Home);
                finish();
            }
        });

        String AlertMsg;

        File mainDatabase = new File(Environment.getExternalStorageDirectory().
                getAbsolutePath() + getString(R.string.AppPath) + "/quran.sqlite");
        if (!mainDatabase.exists()) {
            //copy database
            new CopyDatabase().execute();
        } else {
            downloadDialog();
        }
    }

    /**
     * Download Dialog check internet and annotation
     */
    private void downloadDialog() {
        int internetStatus = Settings.checkInternetStatus(this);
        if (!Settings.isMyServiceRunning(QuranDataActivity.this, DownloadService.class)) {
            if (internetStatus > 0) {

                android.support.v7.app.AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
                builder.setTitle(getResources().getString(R.string.Alert));
                builder.setMessage(internetStatus == 1 ? getResources().
                        getString(R.string.normal_download_alert) : getResources().getString(R.string.mobile_data_alert));
                builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int id) {downloadInformation.setText("Connecting...");new Thread(downloadService).start();}});
                builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int id) {dialog.cancel();System.exit(0);}});
                builder.show();

            } else {
                android.support.v7.app.AlertDialog.Builder builder =
                        new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
                builder.setTitle(getResources().getString(R.string.Alert));
                builder.setMessage(getResources().getString(R.string.no_internet_alert));
                builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        System.exit(0);
                    }
                });
                builder.show();
            }
        }
    }

    /**
     * Thread to check file length and start download service
     */
    private Runnable downloadService = new Runnable() {
        @Override
        public void run() {

            String DownloadLink = QuranConfig.getResolutionURLLink(QuranDataActivity.this);

            //check file download length
            if (Integer.getInteger(FileManager.getDownloadFileLength(DownloadLink)) ==
                    Integer.getInteger(FileManager.getAvailableInternalMemorySize())) {

                //check if download service running
                if (!Settings.isMyServiceRunning(QuranDataActivity.this, DownloadService.class)) {

                    AppPreference.setDownloadType(AppConstants.Preferences.IMAGES);
                    Intent serviceIntent = new Intent(QuranDataActivity.this, DownloadService.class);
                    serviceIntent.putExtra(AppConstants.Download.DOWNLOAD_URL, DownloadLink);
                    serviceIntent.putExtra(AppConstants.Download.DOWNLOAD_LOCATION, Environment
                            .getExternalStorageDirectory()
                            .getAbsolutePath() + getResources().getString(R.string.app_folder_path));
                    startService(serviceIntent);
                }

            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downloadInformation.setText("No enough memory");
                    }
                });
            }
        }
    };

    /**
     * Broadcast receiver to take information of download
     */
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            int value = (int) intent.getLongExtra(AppConstants.Download.NUMBER, 0);
            int max = (int) intent.getLongExtra(AppConstants.Download.MAX, 0);
            String status = intent.getStringExtra(AppConstants.Download.DOWNLOAD);

            if (status != null) {
                if (status.equals(AppConstants.Download.IN_DOWNLOAD)) {
                    downloadProgress.setMax(max);
                    downloadProgress.setProgress(value);
                    downloadInformation.setText(max / 10000 + " / " + value / 10000);
                } else if (status.equals(AppConstants.Download.FAILED)) {
                    downloadProgress.setMax(1);
                    downloadProgress.setProgress(1);
                    downloadInformation.setText(getString(R.string.failed_download));
                } else if (status.equals(AppConstants.Download.SUCCESS)) {
                    downloadProgress.setMax(1);
                    downloadProgress.setProgress(1);
                    downloadInformation.setText(AppConstants.Download.SUCCESS);
                    downloadProgress.setVisibility(View.GONE);
                    MainActivity.loadMainApplicationData();
                } else if (status.equals(AppConstants.Download.IN_EXTRACT)) {
                    downloadProgress.setVisibility(View.GONE);
                    downloadInformation.setText(intent.getStringExtra(AppConstants.Download.FILES));
                } else if (status.equals(AppConstants.Download.UNZIP)) {
                    downloadInformation.setVisibility(View.GONE);
                    StartQuran.setVisibility(View.VISIBLE);
                }

            }
        }
    };


    /**
     * Class for copying main database from assets to internal memory
     */
    private class CopyDatabase extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                downloadInformation.setText(getString(R.string.Done));
                downloadDialog();
            } else
                downloadInformation.setText(R.string.failed);

        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.app_folder_path) + "/quran.sqlite");
                if (!file.exists()) {
                    InputStream inputStream = getApplicationContext().getAssets().open("quran.sqlite");
                    BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
                    int fileSize = inputStream.available();
                    publishProgress(0, fileSize);
                    int copylength = 0;
                    byte[] buffer = new byte[1024];
                    while (true) {
                        int read = inputStream.read(buffer);
                        if (read == -1) break;
                        copylength += read;
                        publishProgress(copylength, fileSize);
                        outputStream.write(buffer, 0, read);
                    }
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            downloadProgress.setMax(values[1]);
            downloadProgress.setProgress(values[0]);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            downloadInformation.setText(getString(R.string.copydata));
        }
    }


}
