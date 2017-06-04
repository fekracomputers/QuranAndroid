package com.fekracomputers.quran.UI.Activities;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.os.StrictMode;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
//import java.awt.Font;
//import java.awt.FontFormatException;
import java.io.IOException;
import java.net.URISyntaxException;
import com.fekracomputers.quran.Adapter.QuartersShowAdapter;
import com.fekracomputers.quran.Database.AppPreference;
import com.fekracomputers.quran.R;
import com.fekracomputers.quran.UI.Fragments.BookmarkFragment;
import com.fekracomputers.quran.UI.Fragments.PartsFragment;
import com.fekracomputers.quran.UI.Fragments.QuarterFragment;
import com.fekracomputers.quran.UI.Popups.JumpToPopup;
import com.fekracomputers.quran.Utilities.AppConstants;
import com.fekracomputers.quran.Utilities.CloseApplication;
import com.fekracomputers.quran.Utilities.QuranConfig;

import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.List;

import static android.R.attr.x;
import static android.R.attr.y;

/**
 * Activity class Home page to choose Quran page read
 */
public class HomeActivity extends AppCompatActivity {
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    int k ;
    boolean doubleBackToExitPressedOnce = false;
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //check is tablet or not
    if(isTablet(getApplicationContext())){
    setContentView(R.layout.activity_home);
     }else{
    setContentView(R.layout.activity_home1);
    }

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


            //get and set screen resolution
            QuranConfig.getResolutionURLLink(this);

            //init application views
            init();


    }

    public static boolean isTablet(Context context) {
        boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (xlarge || large);
    }
    private void init() {
        k = 0;

        AppPreference.setSelectionVerse(null);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name_main));
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(mViewPager);
        if(getIntent() != null){
            String message=getIntent().getStringExtra("Setting");
            if (message!=null &&message.equalsIgnoreCase("x")) {
                Intent i = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(i);
            }
        }

        startService(new Intent(getBaseContext(), CloseApplication.class));
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }
        }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_activity_home, menu);

        //init search in the toolbar
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        //listener for click in search button
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startActivity(new Intent(HomeActivity.this, SearchActivity.class)
                        .putExtra(AppConstants.General.SEARCH_TEXT, query));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_translations) {
            //start translation activity
            startActivity(new Intent(this, TranslationsActivity.class));
            return true;
        } else if (id == R.id.last) {
            int lastPage = AppPreference.getLastPageRead();
            //check of there is no saved last page
            if (lastPage != -1) {
                startActivity(new Intent(this, QuranPageReadActivity.class)
                        .putExtra(AppConstants.General.PAGE_NUMBER, lastPage));
            } else {
                startActivity(new Intent(this, QuranPageReadActivity.class)
                        .putExtra(AppConstants.General.PAGE_NUMBER, 603));
            }

        } else if (id == R.id.search) {
            //Search view created in onCreateOptionMenu
            return false;
        } else if (id == R.id.action_jump) {
            //show popup
            new JumpToPopup(this);
        } else if (id == R.id.action_settings) {
            //settings activity
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.action_about) {
            //about activity
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.action_share) {
            //share intent for the application
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "#"+getString(R.string.app_name)+"\n https://play.google.com/store/apps/details?id="+getPackageName());
            startActivity(Intent.createChooser(sharingIntent, "Share using"));
//            Toast.makeText(this, "App not published", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.action_rate_app) {
            //market url of the application
            String url = "https://play.google.com/store/apps/details?id="+getPackageName();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
//            Toast.makeText(this, "App not published", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        startActivity(new Intent(this,HomeActivity.class).putExtra("Setting","x"));

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return new PartsFragment();
                case 1:
                    return new QuarterFragment();
                case 2:
                    return new BookmarkFragment();
            }

            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {

                switch (position) {

                    case 0:
                        return getResources().getString(R.string.fragment1);
                    case 1:
                        return getResources().getString(R.string.fragment2);
                    case 2:
                        return getResources().getString(R.string.fragment3);

                }

            return null;
        }
    }
    @Override
    public void onBackPressed() {
//        k++;
//        if(k == 1)
//            finish();
        super.onBackPressed();
    }

}
