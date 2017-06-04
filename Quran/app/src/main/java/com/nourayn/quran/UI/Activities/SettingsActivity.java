package com.nourayn.quran.UI.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.fekracomputers.quran.R;
import com.nourayn.quran.Utilities.AppConstants;

/**
 * Activity about settings
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//display back button
        getSupportActionBar().setTitle(getString(R.string.settings));//display title of activity
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainSettings()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Fragment preference of the settings
     */
    public static class MainSettings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private SharedPreferences sp;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

            //Preferences to open translations
            /*Preference openTranslations = (Preference) findPreference(AppConstants.Preferences.TRANSLATIONS);
            openTranslations.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    getActivity().startActivity(new Intent(getActivity(), TranslationsActivity.class));
                    getActivity().finish();
                    return true;
                }
            });*/

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
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    break;
            }

        }
    }

}
