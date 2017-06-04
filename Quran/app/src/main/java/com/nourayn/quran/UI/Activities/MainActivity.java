package com.nourayn.quran.UI.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.widget.Toast;

import com.nourayn.quran.Database.AppPreference;
import com.nourayn.quran.Database.DatabaseAccess;
import com.nourayn.quran.Downloader.DownloadService;
import com.nourayn.quran.Models.Quarter;
import com.nourayn.quran.Models.Sora;
import com.fekracomputers.quran.R;
import com.nourayn.quran.Utilities.AppConstants;
import com.nourayn.quran.Utilities.QuranConfig;
import com.nourayn.quran.Utilities.QuranValidateSources;
import com.nourayn.quran.Utilities.Settings;

import java.util.ArrayList;
import java.util.List;

/**
 * Splash screen to validate application files and redirect to download open application
 */
public class MainActivity extends AppCompatActivity {
    public static List<Sora> soraListModified ;
    public static List<Quarter> quarterListModified ;
    private static final int REQUEST_WRITE_STORAGE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen_main);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        //marshmallow check permission permissions
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        } else {
            validateFilesAndDownload();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            //check if permission had taken or not
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //valid to download or not
                    validateFilesAndDownload();
                } else {
                    Toast.makeText(this, getString(R.string.permission), Toast.LENGTH_LONG).show();
                    MainActivity.this.finish();
                }
            }
        }
    }

    /**
     * Function to validate application folders and files and download
     */
    public void validateFilesAndDownload() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                //set screen resolution in prefrence
                QuranConfig.getResolutionURLLink(MainActivity.this);

                //check if files is valid
                if (!QuranValidateSources.validatAppMainFoldersAndFiles(MainActivity.this) ||
                        (Settings.isMyServiceRunning(MainActivity.this, DownloadService.class)
                                && AppPreference.getDownloadType() == AppConstants.Preferences.IMAGES)) {

                    //set default preference font int web_view to the meduim font
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("size" , "large");
                    editor.commit();

                    Intent downloadActivity = new Intent(MainActivity.this, QuranDataActivity.class);
                    startActivity(downloadActivity);
                    MainActivity.this.finish();

                } else {

                    loadMainApplicationData();

                    Intent Home = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(Home);
                    MainActivity.this.finish();
                }

            }
        }).start();
    }

    public static void loadMainApplicationData(){
        List<Quarter> quarters = new DatabaseAccess().getAllQuarters();
        int lastPart = 0;
        quarterListModified = new ArrayList<>();
        for (Quarter quarter : quarters) {
            if (lastPart != quarter.joza) {
                lastPart = quarter.joza;
                quarterListModified.add(new Quarter("", "", -1, new DatabaseAccess()
                        .getPartStartPage(lastPart), -1, -1, "", -1, -1, lastPart));
            }
            quarterListModified.add(quarter);
        }

        List<Sora> parts = new DatabaseAccess().getAllSora() ;

        int counter = 0;
        lastPart = 0;
        soraListModified = new ArrayList<>();
        for (Sora sora_item : parts) {
            counter++;
            if (lastPart != sora_item.jozaNumber) {
                if ((sora_item.jozaNumber - lastPart) == 2) {
                    soraListModified.add(new Sora("", "", -1, new DatabaseAccess()
                            .getPartStartPage(lastPart + 1), lastPart + 1, -1));
                }
                lastPart = sora_item.jozaNumber;
                soraListModified.add(new Sora("", "", -1, new DatabaseAccess()
                        .getPartStartPage(lastPart), lastPart, -1));
            }
            sora_item.setSoraTag(counter + "");
            soraListModified.add(sora_item);
        }

    }

}