package com.nourayn.quran.UI.Activities;

import android.app.Dialog;
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
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nourayn.quran.Adapter.TranslationAdapter;
import com.nourayn.quran.Database.AppPreference;
import com.nourayn.quran.Database.DatabaseAccess;
import com.nourayn.quran.Downloader.DownloadService;
import com.nourayn.quran.Models.TranslationBook;
import com.fekracomputers.quran.R;
import com.nourayn.quran.Utilities.AppConstants;
import com.nourayn.quran.Utilities.QuranValidateSources;
import com.nourayn.quran.Utilities.Settings;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for management download translations and explanation
 */
public class TranslationsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private ListView translationManagmentList;
    private TranslationAdapter adapter;
    private List<TranslationBook> listOfDownloaded, listToDownload;
    private Dialog progress;
    private ProgressBar downloadProgress;
    private TextView downloadInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //display back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//display back button
        getSupportActionBar().setTitle(getString(R.string.translations));//set action bar title
        setContentView(R.layout.activity_translation);
        init();
    }

    /**
     * Function to init activity view
     */
    private void init() {

        //init views
        adapter = new TranslationAdapter(this);
        translationManagmentList = (ListView) findViewById(R.id.download);
        translationManagmentList.setOnItemClickListener(this);
        translationManagmentList.setTextFilterEnabled(true);
        translationManagmentList.setEmptyView(findViewById(R.id.progressBar3));
        translationManagmentList.setAdapter(adapter);

        //async thread to load translations
        new TafaseerLists().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void showDownloadPop() {
        progress = new Dialog(this);
        progress.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progress.setContentView(R.layout.download_popup);
        progress.setCancelable(false);
        downloadProgress = (ProgressBar) progress.findViewById(R.id.pb_download);
        downloadInfo = (TextView) progress.findViewById(R.id.tv_download_info);
        Button cancelDownload = (Button) progress.findViewById(R.id.b_cancel_download);

        cancelDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TafaseerLists().execute();
                progress.cancel();
                stopService(new Intent(TranslationsActivity.this, DownloadService.class));
            }
        });

        progress.show();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(downloadBroadcast, new IntentFilter(AppConstants.Download.INTENT));

        //check if the download service run
        if (!Settings.isMyServiceRunning(TranslationsActivity.this, DownloadService.class) && progress != null) {
            progress.dismiss();
            new TafaseerLists().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else if (progress != null) {
            progress.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(downloadBroadcast);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        //check to remove or download translation
        final TranslationBook translationBook = adapter.getItem(position);
        final File tafseer = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.AppPath) + "/tafaseer");
        if (translationBook.isDownloaded) {
            //remove translation
            final AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            alert.setTitle(getString(R.string.Remove));
            alert.setMessage(getString(R.string.removeTranslationAlert));
            alert.setNegativeButton(getString(R.string.removeTranslationAlertNegative), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alert.setPositiveButton(getString(R.string.removeTranslationAlertPositive), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (new File(tafseer.getAbsolutePath() + "/tafseer" + translationBook.bookID + AppConstants.Extensions.SQLITE).delete()) {
                        Toast.makeText(TranslationsActivity.this, getString(R.string.translation_deleted), Toast.LENGTH_SHORT).show();
                    }
                    new TafaseerLists().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
            alert.show();
        } else if (!Settings.isMyServiceRunning(TranslationsActivity.this, DownloadService.class)) {
            int internetStatus = Settings.checkInternetStatus(this);
            if (internetStatus <= 0) {
                android.support.v7.app.AlertDialog.Builder builder =
                        new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
                builder.setTitle(getResources().getString(R.string.Alert));
                builder.setMessage(getResources().getString(R.string.no_internet_alert));
                builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                //add dirs and download
                if (!tafseer.exists()) tafseer.mkdirs();
                translationBook.downloading = true;
                AppPreference.setDownloadType(AppConstants.Preferences.TAFSEER);
                AppPreference.setDownloadID(position);
                Intent downloadService = new Intent(this, DownloadService.class);
                downloadService.putExtra(AppConstants.Download.DOWNLOAD_LOCATION, Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.AppPath));
                downloadService.putExtra(AppConstants.Download.DOWNLOAD_URL, AppConstants.Paths.TAFSEER_LINK + translationBook.bookID + AppConstants.Extensions.ZIP);
                startService(downloadService);
                showDownloadPop();
                //downloadView.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
            }


        }
    }

    /**
     * Class to get all downloaded tafaseer and tafaseer to download
     */
    private class TafaseerLists extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            listOfDownloaded = new ArrayList<>();
            listToDownload = new DatabaseAccess().getAllTranslations();
            listOfDownloaded.clear();
            List<Integer> downloadedBookIDs = new QuranValidateSources().getDownloadedTransaltions();
            for (TranslationBook translationBook : listToDownload) {
                for (Integer bookID : downloadedBookIDs) {
                    if (bookID == translationBook.bookID) {
                        translationBook.isDownloaded = true;
                        listOfDownloaded.add(translationBook);
                    }
                }
            }
            for (TranslationBook translationBook : listOfDownloaded) {
                listToDownload.remove(translationBook);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            adapter.clear();
            if (listOfDownloaded.size() != 0)
                adapter.add(new TranslationBook(-1, getString(R.string.downloaded), null, 0, false, false));
            adapter.addAll(listOfDownloaded);
            adapter.add(new TranslationBook(-1, getString(R.string.downloadavaliable), null, 0, false, false));
            adapter.addAll(listToDownload);
            adapter.notifyDataSetChanged();
        }

    }

    /**
     * Class receive Broadcast that download finished to refresh list
     */
    private BroadcastReceiver downloadBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //intent from sent broadcast
            float value = intent.getLongExtra(AppConstants.Download.NUMBER, 0);
            float max = intent.getLongExtra(AppConstants.Download.MAX, 0);
            String status = intent.getStringExtra(AppConstants.Download.DOWNLOAD);

            //cases of download
            if (status != null) {
                if (status.equals(AppConstants.Download.IN_DOWNLOAD)) {
                    downloadProgress.setMax((int) max);
                    downloadProgress.setProgress((int) value);

                    DecimalFormat df = new DecimalFormat("#.##");
                    String maxDownload = df.format((max / 1000000));
                    String currentDownload = df.format((value / 1000000));
                    downloadInfo.setText(maxDownload + " " + getString(R.string.mb) + " / " + currentDownload + " " + getString(R.string.mb));
                } else if (status.equals(AppConstants.Download.FAILED)) {
                    downloadProgress.setMax(1);
                    downloadProgress.setProgress(1);
                } else if (status.equals(AppConstants.Download.SUCCESS)) {
                    downloadProgress.setMax(1);
                    downloadProgress.setProgress(1);
                    progress.dismiss();
                    new TafaseerLists().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }else if(status.equals(AppConstants.Download.IN_EXTRACT)){
                    downloadProgress.setVisibility(View.GONE);
                    downloadInfo.setText(intent.getStringExtra(AppConstants.Download.FILES));
                }
            }
        }
    };

}
