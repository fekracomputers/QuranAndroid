package com.fekracomputers.quran.UI.Activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.fekracomputers.quran.Database.AppPreference;
import com.fekracomputers.quran.Database.DatabaseAccess;
import com.fekracomputers.quran.Models.Aya;
import com.fekracomputers.quran.Models.Bookmark;
import com.fekracomputers.quran.Models.TranslationBook;
import com.fekracomputers.quran.R;
import com.fekracomputers.quran.UI.Custom.HighlightImageView;
import com.fekracomputers.quran.UI.Custom.LockableViewPager;
import com.fekracomputers.quran.UI.Fragments.TafseerFragment;
import com.fekracomputers.quran.Utilities.AppConstants;
import com.fekracomputers.quran.Utilities.Contact;
import com.fekracomputers.quran.Utilities.DatabaseHandler;
import com.fekracomputers.quran.Utilities.QuranValidateSources;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity to show translation
 */
public class TranslationReadActivity extends AppCompatActivity implements SensorEventListener {
    private RelativeLayout header;
    private Spinner translationBooks;
    private LockableViewPager translationViewPager;
    private SelectionTranslationsAdapter selectionTranslationsAdapter;
    private ArrayList<String> bookNames;
    private List<Integer> bookIDs;
    private List<TranslationBook> booksInfo;
    private boolean flagHideShowTool, defaultBook;
    private int firstBook, defaultBookIndex;
    private ImageView back;
    int x;
    int k = 0;
    Sensor proxSensor;
    SensorManager sm;
    private SensorManager sensorManager,mSensorManager;
    private Sensor lightSensor;
    private float lightAmount;
    DatabaseHandler db;
    DatabaseHandler db1;
    Aya aya=null;
    MenuItem bookmark;

    public static int  imagesResource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_translation_read);
        db = new DatabaseHandler(this);
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        proxSensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        lightSensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
        sm.registerListener(this,proxSensor,SensorManager.SENSOR_DELAY_NORMAL);
        //toolbar object
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //init translation read activity
        init();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        aya = new DatabaseAccess().getAyaFromPosition(6235 - translationViewPager.getCurrentItem());
        startActivity(new Intent(TranslationReadActivity.this, QuranPageReadActivity.class)
                .putExtra(AppConstants.General.PAGE_NUMBER, 604 - aya.pageNumber)); //position where return
//        AppPreference.setTranslationTextSize("large");
        String log="";
        List<Contact> tasks = db.getAllContacts();

        int result=tasks.size();
        if(result >0) {
            // delete one book
            for (Contact cn : tasks) {
                log = cn.getPhoneNumber();
                // Writing Contacts to log
                Log.e("Name: ", log);
            }
            AppPreference.setTranslationTextSize(log);
        }else{

            AppPreference.setTranslationTextSize("large");
        }
        finish();

    }

    /**
     * Function to init activity views
     */
    private void init() {
        Intent extras = getIntent();



        String log="";
        List<Contact> tasks = db.getAllContacts();

        int result=tasks.size();
        if(result >0) {
            // delete one book
            for (Contact cn : tasks) {
                log = cn.getPhoneNumber();
                // Writing Contacts to log
                Log.e("Name: ", log);
            }
            AppPreference.setTranslationTextSize(log);
        }else{

            if(isTablet(getApplicationContext())){
                AppPreference.setTranslationTextSize("x-large");
            }else{
                AppPreference.setTranslationTextSize("large");
            }
        }






        //get activity send intent
        final int TRANSLATION_PGE = getIntent().getIntExtra(AppConstants.General.PAGE_NUMBER, 1),
                BOOK_ID = AppPreference.getDefaultTafseer(),
                AYA_NUMBER = getIntent().getIntExtra(AppConstants.Tafseer.AYA, -1),
                SORA_NUMBER = getIntent().getIntExtra(AppConstants.Tafseer.SORA, -1);
        x = SORA_NUMBER;

        //init views
        flagHideShowTool = false;
        defaultBook = true;
        back = (ImageView) findViewById(R.id.back);
        translationBooks = (Spinner) findViewById(R.id.s_tafaseer);
        translationViewPager = (LockableViewPager) findViewById(R.id.vp_container);
        translationViewPager.setPagingEnabled(true);
        header = (RelativeLayout) findViewById(R.id.rl_toolbar_container);
        DatabaseAccess db = new DatabaseAccess();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Aya aya = new DatabaseAccess().getAyaFromPosition(6235 - translationViewPager.getCurrentItem());
                startActivity(new Intent(TranslationReadActivity.this, QuranPageReadActivity.class)
                        .putExtra(AppConstants.General.PAGE_NUMBER, 604 - aya.pageNumber)); //position where return
//                AppPreference.setTranslationTextSize("large");
                String log="";
                db1 = new DatabaseHandler(TranslationReadActivity.this);
                List<Contact> tasks = db1.getAllContacts();

                int result=tasks.size();
                if(result >0) {
                    // delete one book
                    for (Contact cn : tasks) {
                        log = cn.getPhoneNumber();
                        // Writing Contacts to log
                        Log.e("Name: ", log);
                    }
                    AppPreference.setTranslationTextSize(log);
                }else{

                    AppPreference.setTranslationTextSize("large");
                }
                finish();
            }
        });

        //Load books names
        bookNames = new ArrayList<String>();
        bookIDs = QuranValidateSources.getDownloadedTransaltions();
        if (bookIDs.size() == 0) {
            startActivity(new Intent(this, TranslationsActivity.class));
            finish();
            return;
        }
        booksInfo = new DatabaseAccess().getAllTranslations();
        int count = 0;
        for (int bookID : bookIDs) {
            if (firstBook == 0) firstBook = bookID;
            for (TranslationBook bookInfo : booksInfo) {
                if (bookID == bookInfo.bookID) {
                    count++;
                    bookNames.add(bookInfo.bookName);
                    //default book to start display
                    if (AppPreference.getDefaultTafseer() == bookInfo.bookID)
                        defaultBookIndex = count;
                }
            }
        }

        selectionTranslationsAdapter = new SelectionTranslationsAdapter(getSupportFragmentManager(), BOOK_ID == -1 ? firstBook : BOOK_ID);
        new WeakReference<SelectionTranslationsAdapter>(selectionTranslationsAdapter);
        translationViewPager.setAdapter(selectionTranslationsAdapter);

        //check if you send to activity page or aya info
        int position = 0;
        if (AYA_NUMBER != -1) {
            position = db.getAyaPosition(SORA_NUMBER, AYA_NUMBER);


        } else {
            //get page first aya
            aya = db.getPageStartAyaID(604 - TRANSLATION_PGE);
            position = db.getAyaPosition(aya.suraID, aya.ayaID);

        }


        //Custom spinner adapter to readers spinner
        ArrayAdapter<String> spinnerBooksAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_layout,
                R.id.spinnerText,
                bookNames);
        translationBooks.setAdapter(spinnerBooksAdapter);
        translationViewPager.setCurrentItem(6235 - position);
        Log.e("tag", "translationViewPager: "+translationViewPager );
        //set default book is the selected book
        translationBooks.setSelection(defaultBookIndex - 1, true);

        //Spinner on click listener for books
        translationBooks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (defaultBook != true) {
                    int bookID = 0;
                    String bookName = bookNames.get(position);
                    //loop to get book id
                    for (TranslationBook book : booksInfo) {
                        if (bookName == book.bookName) bookID = book.bookID;
                    }

                    //set default book tafseer
                    AppPreference.setDefaultTafseer(bookID);

                    //load new book translation her
                    int page = translationViewPager.getCurrentItem();
                    selectionTranslationsAdapter = new SelectionTranslationsAdapter(getSupportFragmentManager(),
                            AppPreference.getDefaultTafseer());
                    translationViewPager.setAdapter(selectionTranslationsAdapter);
                    translationViewPager.setCurrentItem(page);
                    Log.e("tag", "page: "+page);
                } else {
                    defaultBook = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        //hide toolbar after time
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideToolbar();
            }
        }, 2000);
        Aya aya = db.getAyaFromPosition(position);
        Log.e("position", "init: "+ aya.pageNumber );

    }

    /**
     * Function with flag to hide and show toolbar
     */
    public void showHideToolBar() {
//        Aya aya122 = new DatabaseAccess().getAyaFromPosition(6235 - translationViewPager.getCurrentItem());
//        hi(aya122.pageNumber);
        if (!flagHideShowTool) {
            hideToolbar();
            HighlightImageView.inAnimation = false;
            flagHideShowTool = true;
        } else {
            showToolbar();
            HighlightImageView.inAnimation = false;
            flagHideShowTool = false;
        }
    }

    /**
     * Function to show tool bar "animation"
     */
    public void showToolbar() {
        ObjectAnimator animY = ObjectAnimator.ofFloat(header, "y", 0);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.setInterpolator(new LinearInterpolator());
        animSetXY.play(animY);
        animSetXY.start();

    }

    /**
     * Function to hide tool bar "animation"
     */
    public void hideToolbar() {
        ObjectAnimator animY = ObjectAnimator.ofFloat(header, "y", -(header.getHeight()));
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.setInterpolator(new LinearInterpolator());
        animSetXY.play(animY);
        animSetXY.start();
    }

    /**
     * Override function to create or inflate menu
     *
     * @param menu Activity menu
     * @return Menu you create
     */






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_transaltion_read, menu);
        bookmark = menu.findItem(R.id.bookmark);
        changeBookmarkItemImage();
        return true;
    }

    private void changeBookmarkItemImage() {
        if(bookmark!=null){
            bookmark.setIcon(imagesResource);
        }
    }

    /**
     * Function on select menu item
     *
     * @param item Menu item you select
     * @return Flag or selection
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //switch id to do action
        switch (id) {

            //return to quran read
            case R.id.action_read_quran:

               Aya  aya11 = new DatabaseAccess().getAyaFromPosition(6235 - translationViewPager.getCurrentItem());
                startActivity(new Intent(this, QuranPageReadActivity.class)
                        .putExtra(AppConstants.General.PAGE_NUMBER, 604 - aya11.pageNumber)

                ); //position where return


                String log="";
                List<Contact> tasks = db.getAllContacts();

                int result=tasks.size();
                if(result >0) {
                    // delete one book
                    for (Contact cn : tasks) {
                        log = cn.getPhoneNumber();
                        // Writing Contacts to log
                        Log.e("Name: ", log);
                    }
                    AppPreference.setTranslationTextSize(log);
                }else{

                    AppPreference.setTranslationTextSize("large");
                }
                finish();

                break;

            //zoom in web_view
            case R.id.action_zoom_in:
                String size = AppPreference.getTranslationTextSize();
                switch (size) {
                    case "large":
                        AppPreference.setTranslationTextSize("x-large");
                        db.addContact(new Contact("x-large"));
                        break;

                    case "x-large":
                        AppPreference.setTranslationTextSize("xx-large");
                        item.setIcon(R.drawable.ic_zoom_r);
                        db.addContact(new Contact("xx-large"));
                        break;

                    case "xx-large":
                        AppPreference.setTranslationTextSize("large");
                        item.setIcon(R.drawable.ic_zoom_in);
                        db.addContact(new Contact("large"));
                        break;
                }

                int previousPage = translationViewPager.getCurrentItem();
                selectionTranslationsAdapter = null;
                selectionTranslationsAdapter = new SelectionTranslationsAdapter(getSupportFragmentManager(),
                        AppPreference.getDefaultTafseer() == -1 ? firstBook : AppPreference.getDefaultTafseer());
                translationViewPager.setAdapter(selectionTranslationsAdapter);
                translationViewPager.setCurrentItem(previousPage);

                break;
            case  R.id.bookmark:
                //check if the page is bookmark or not
                if (new DatabaseAccess().isPageBookmarked(this.aya.pageNumber)) {
                    boolean isRemoved = new DatabaseAccess().removeBookmark(this.aya.pageNumber );

                    if (isRemoved) {
                        imagesResource=R.drawable.ic_not_fav;
                        changeBookmarkItemImage();

                    }
                } else {
                    if (new DatabaseAccess().bookmark(this.aya.pageNumber)) {
                        imagesResource=R.drawable.ic_favo;
                        changeBookmarkItemImage();
                    }
                }
                break;

        }

        return super.onOptionsItemSelected(item);
    }

     @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        try {

            Log.i("Sensor Changed", "Accuracy :" + sensorEvent.values[0]);

            float s = sensorEvent.values[0];
            String ss = String.valueOf(s);
            if (ss.equals("0.0") ) { //even reduce

                //refreshes the screen
                int br = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.screenBrightness = (float) 10 / 255;
                getWindow().setAttributes(lp);


            } else{

                //refreshes the screen

                WindowManager.LayoutParams lp1 = getWindow().getAttributes();
                lp1.screenBrightness = (float) 255 / 255;
                getWindow().setAttributes(lp1);

            }


        } catch (Exception e)
        {
            //Throw an error case it couldn't be retrieved
            Log.e("Error", "Cannot access system brightness");
            e.printStackTrace();
        }
    }

    /**
     * class adapter to view pager load pages of tafaseer
     */
    public class SelectionTranslationsAdapter extends FragmentStatePagerAdapter {

        private int bookID;

        public SelectionTranslationsAdapter(FragmentManager fm, int bookID) {
            super(fm);
            this.bookID = bookID;
        }

        @Override
        public Fragment getItem(int position) {
            Log.e("tag", "getItem: "+ (6235 - position));
            aya = new DatabaseAccess().getAyaFromPosition(6235 - position);
            // check.
            if(new DatabaseAccess().isPageBookmarked(aya.pageNumber)){
                //
                imagesResource = R.drawable.ic_favo;
                changeBookmarkItemImage();
            }else{
                imagesResource = R.drawable.ic_not_fav;
                changeBookmarkItemImage();
            }
//            hi(aya.pageNumber);
            return new TafseerFragment().newInstance(6235 - position, bookID);
        }

        @Override
        public int getCount() {
            return 6236 ;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }


}
