package com.fekracomputers.quran.UI.Activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.fekracomputers.quran.R;
import com.fekracomputers.quran.Utilities.AppConstants;

import java.util.List;

/**
 * Activity about settings
 */
public class SettingsActivity extends AppCompatActivity {
    int k = 0;
    ImageView back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//display back button
        getSupportActionBar().setTitle(getString(R.string.settings));//display title of activity
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainSettings()).commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        startActivity(new Intent(this, HomeActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
          return true;

    }

    /**
     * Fragment preference of the settings
     */
    public static class MainSettings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private SharedPreferences sp;



        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if(isTablet1(this.getActivity().getApplicationContext())){
                addPreferencesFromResource(R.xml.settings);
            }else{
                addPreferencesFromResource(R.xml.settings1);
            }

            sp = PreferenceManager.getDefaultSharedPreferences(getActivity());


        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }


        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            switch (key) {
                case AppConstants.Preferences.ARABIC_MOOD:
                    getActivity().startActivity(new Intent(getActivity(), HomeActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK)
                           .putExtra("Setting","x"));
                    getActivity().finish();

                    break;

            }

        }
    }

    @Override
    public void onBackPressed() {

        startActivity(new Intent(this, HomeActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main1, menu);
        return true;
    }


public static boolean isTablet1(Context context) {
    return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
}
}
