package com.nourayn.quran.Utilities;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;

import com.nourayn.quran.Downloader.DownloadService;
import com.nourayn.quran.Application.QuranApplication;
import com.fekracomputers.quran.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Class to unzip files in background
 */
public class UnZipping extends AsyncTask<String, Integer, Void> {
    private Context context;
    private InputStream is;
    private ZipInputStream zis;
    private ZipEntry ze;
    private String filename;
    private int fileCount;

    /**
     * Unzip Constructor
     *
     * @param context Application context
     */
    public UnZipping(Context context) {
        this.context = context;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (context instanceof Service)
            ((Service) context).stopService(new Intent(context, DownloadService.class));
        Intent zipedFiles = new Intent(AppConstants.Download.INTENT);
        zipedFiles.putExtra(AppConstants.Download.DOWNLOAD, AppConstants.Download.UNZIP);
        LocalBroadcastManager.getInstance(context).sendBroadcast(zipedFiles);
    }

    @Override
    protected Void doInBackground(String... params) {
        try {

            is = new FileInputStream(params[0] + "/" + params[1]);
            zis = new ZipInputStream(new BufferedInputStream(is));
            byte[] buffer = new byte[1024 * 3];
            int count;
            while ((ze = zis.getNextEntry()) != null) {
                filename = ze.getName();
                if (ze.isDirectory()) {
                    File fmd = new File(params[0] + "/" + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(params[0] + "/" + filename);
                int total = zis.available();
                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                    fileCount++;
                    Intent zipedFiles = new Intent(AppConstants.Download.INTENT);
                    zipedFiles.putExtra(AppConstants.Download.FILES, context.getString(R.string.extract) + " " + fileCount);
                    zipedFiles.putExtra(AppConstants.Download.DOWNLOAD, AppConstants.Download.IN_EXTRACT);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(zipedFiles);
                }

                File zipFile = new File(params[0] + "/" + params[1]);
                zipFile.delete();

                if (zipFile.getAbsolutePath().contains("tafseer")) {
                    copyFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + QuranApplication.getInstance()
                                    .getString(R.string.app_folder_path) + "/tafaseer/" + params[1].replace(AppConstants.Extensions.ZIP, AppConstants.Extensions.SQLITE)),
                            new File(params[0] + "/" + params[1].replace(AppConstants.Extensions.ZIP, AppConstants.Extensions.SQLITE)));
                }

                fout.close();
                zis.closeEntry();
            }

            //send broadcast of success or failed
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(AppConstants.Download.INTENT)
                    .putExtra(AppConstants.Download.DOWNLOAD, AppConstants.Download.SUCCESS ));

            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * Copy File after extract
     *
     * @param destination Destination of extraction
     * @param path        File path
     */
    public static void copyFile(File destination, File path) {
        try {
            FileInputStream inStream = new FileInputStream(path);
            FileOutputStream outStream = new FileOutputStream(destination);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }
            inStream.close();
            outStream.close();
            path.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
