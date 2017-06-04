package com.nourayn.quran.Utilities;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.nourayn.quran.Application.QuranApplication;
import com.fekracomputers.quran.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Class for all operation can do for application files
 */
public class FileManager {

    /**
     * Function to  unzip file
     *
     * @param path    path of zip file
     * @param zipname zip file name
     * @return Extract success or Failed
     */
    public static boolean unpackZip(String path, String zipname) {
        Log.d("Path", path + zipname);
        InputStream is;
        ZipInputStream zis;
        try {
            String filename;
            is = new FileInputStream(path + "/" + zipname);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {
                filename = ze.getName();
                if (ze.isDirectory()) {
                    File fmd = new File(path + "/" + filename);
                    fmd.mkdirs();
                    continue;
                }


                FileOutputStream fout = new FileOutputStream(path + "/" + filename);

                // cteni zipu a zapis
                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }

                File zipFile = new File(path + "/" + zipname);
                zipFile.delete();

                if (zipFile.getAbsolutePath().contains("tafseer")) {
                    copyFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                                    QuranApplication.getInstance().getString(R.string.app_folder_path) + "/tafaseer/" + zipname.replace(".zip", ".sqlite")),
                            new File(path + "/" + zipname.replace(".zip", ".sqlite")));
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Function to format size
     *
     * @param size Size to format
     * @return Formatted size
     */
    public static String formatSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    /**
     * Function to get available memory size
     *
     * @return Memory size
     */
    public static String getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return formatSize(availableBlocks * blockSize);
    }

    /**
     * Function To get download link length
     *
     * @param downloadURL String download url
     * @return Length of file
     */
    public static String getDownloadFileLength(String downloadURL) {
        URL url = null;
        try {
            url = new URL(downloadURL);
            URLConnection conection = url.openConnection();
            conection.connect();
            int lenghtOfFile = conection.getContentLength();
            return formatSize(lenghtOfFile);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * Function to copy database from assets to internal memory
     */
    public static void copyDatabase(Context context) {
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + context.getString(R.string.app_folder_path) + "/quran.sqlite");
            if (!file.exists()) {
                InputStream inputStream = context.getApplicationContext().getAssets().open("quran.sqlite");
                BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
                int length = 0;
                while ((length = inputStream.read()) > -1) {
                    outputStream.write(length);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Function to copy certain file to folder
     *
     * @param destination Folder where you would copy
     * @param path        Path of file
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


    /**
     * Function to create aya Audio link in folders
     *
     * @param reader Reader ID
     * @param sura   Sura id
     * @param aya    Aya ID
     * @return URI of the file
     */
    public static String createAyaAudioLinkLocation(Context context , int reader, int aya, int sura) {
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
        return Environment
                .getExternalStorageDirectory()
                .getAbsolutePath()
                + context.getString(R.string.app_folder_path)
                + "/Audio/" + reader + "/" + suraID
                + ayaID + AppConstants.Extensions.MP3;
    }


}
