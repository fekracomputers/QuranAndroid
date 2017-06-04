package com.nourayn.quran.UI.Activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import com.nourayn.quran.Database.AppPreference;
import com.nourayn.quran.Database.DatabaseAccess;
import com.nourayn.quran.Models.Aya;
import com.nourayn.quran.Models.TranslationBook;
import com.fekracomputers.quran.R;
import com.nourayn.quran.UI.Custom.HighlightImageView;
import com.nourayn.quran.UI.Fragments.TafseerFragment;
import com.nourayn.quran.Utilities.AppConstants;
import com.nourayn.quran.Utilities.QuranValidateSources;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity to show translation
 */
public class TranslationReadActivity extends AppCompatActivity {
    private RelativeLayout header;
    private Spinner translationBooks;
    private ViewPager translationViewPager;
    private SelectionTranslationsAdapter selectionTranslationsAdapter;
    private ArrayList<String> bookNames;
    private List<Integer> bookIDs;
    private List<TranslationBook> booksInfo;
    private boolean flagHideShowTool, defaultBook;
    private int firstBook , defaultBookIndex;
    private ImageView back ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_translation_read);

        //toolbar object
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //init translation read activity
        init();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Aya aya = new DatabaseAccess().getAyaFromPosition(6235 - translationViewPager.getCurrentItem());
        startActivity(new Intent(TranslationReadActivity.this, QuranPageReadActivity.class)
                .putExtra(AppConstants.General.PAGE_NUMBER, 604 - aya.pageNumber)); //position where return
        AppPreference.setTranslationTextSize("large");
        finish();
    }

    /**
     * Function to init activity views
     */
    private void init() {

        //get activity send intent
        final int TRANSLATION_PGE = getIntent().getIntExtra(AppConstants.General.PAGE_NUMBER, 1),
                BOOK_ID = AppPreference.getDefaultTafseer() ,
                AYA_NUMBER = getIntent().getIntExtra(AppConstants.Tafseer.AYA, -1),
                SORA_NUMBER = getIntent().getIntExtra(AppConstants.Tafseer.SORA , -1);

        //init views
        flagHideShowTool = false;
        defaultBook = true;
        back = (ImageView) findViewById(R.id.back);
        translationBooks = (Spinner) findViewById(R.id.s_tafaseer);
        translationViewPager = (ViewPager) findViewById(R.id.vp_container);
        header = (RelativeLayout) findViewById(R.id.rl_toolbar_container);
        DatabaseAccess db = new DatabaseAccess();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Aya aya = new DatabaseAccess().getAyaFromPosition(6235 - translationViewPager.getCurrentItem());
                startActivity(new Intent(TranslationReadActivity.this, QuranPageReadActivity.class)
                        .putExtra(AppConstants.General.PAGE_NUMBER, 604 - aya.pageNumber)); //position where return
                AppPreference.setTranslationTextSize("large");
                finish();
            }
        });

        //Load books names
        bookNames = new ArrayList<String>();
        bookIDs = QuranValidateSources.getDownloadedTransaltions();
        if(bookIDs.size() == 0 ){
            startActivity(new Intent(this , TranslationsActivity.class));
            finish();
            return ;
        }
        booksInfo = new DatabaseAccess().getAllTranslations();
        int count = 0 ;
        for (int bookID : bookIDs) {
            if (firstBook == 0) firstBook = bookID;
            for (TranslationBook bookInfo : booksInfo) {
                if (bookID == bookInfo.bookID){
                    count++;
                    bookNames.add(bookInfo.bookName);
                    //default book to start display
                    if(AppPreference.getDefaultTafseer() == bookInfo.bookID) defaultBookIndex = count ;
                }
            }
        }

        //init and add viewpager adapter
        selectionTranslationsAdapter = new SelectionTranslationsAdapter(getSupportFragmentManager(), BOOK_ID == -1 ? firstBook : BOOK_ID);
        new WeakReference<SelectionTranslationsAdapter>(selectionTranslationsAdapter);
        translationViewPager.setAdapter(selectionTranslationsAdapter);

        //check if you send to activity page or aya info
        int position = 0 ;
        if(AYA_NUMBER != -1){
            position = db.getAyaPosition(SORA_NUMBER, AYA_NUMBER);
        }else{
            //get page first aya
            Aya aya = db.getPageStartAyaID(604 - TRANSLATION_PGE);
            position = db.getAyaPosition(aya.suraID, aya.ayaID);
        }


        //Custom spinner adapter to readers spinner
        ArrayAdapter<String> spinnerBooksAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_layout,
                R.id.spinnerText,
                bookNames);
        translationBooks.setAdapter(spinnerBooksAdapter);
        translationViewPager.setCurrentItem(6235 - position);

        //set default book is the selected book
        translationBooks.setSelection(defaultBookIndex-1 , true);

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


    }

    /**
     * Function with flag to hide and show toolbar
     */
    public void showHideToolBar() {
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
        return super.onCreateOptionsMenu(menu);
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

                Aya aya = new DatabaseAccess().getAyaFromPosition(6235 - translationViewPager.getCurrentItem());
                startActivity(new Intent(this, QuranPageReadActivity.class)
                        .putExtra(AppConstants.General.PAGE_NUMBER, 604 - aya.pageNumber)); //position where return
                AppPreference.setTranslationTextSize("large");
                finish();

                break;

            //zoom in web_view
            case R.id.action_zoom_in:
                String size = AppPreference.getTranslationTextSize();
                switch (size){
                    case "large":
                        AppPreference.setTranslationTextSize("x-large");
                        break ;

                    case "x-large":
                        AppPreference.setTranslationTextSize("xx-large");
                        item.setIcon(R.drawable.ic_zoom_r);
                        break ;

                    case "xx-large":
                        AppPreference.setTranslationTextSize("large");
                        item.setIcon(R.drawable.ic_zoom_in);
                        break ;
                }

                int previousPage = translationViewPager.getCurrentItem() ;
                selectionTranslationsAdapter = null ;
                selectionTranslationsAdapter = new SelectionTranslationsAdapter(getSupportFragmentManager(),
                        AppPreference.getDefaultTafseer() == -1 ? firstBook : AppPreference.getDefaultTafseer());
                translationViewPager.setAdapter(selectionTranslationsAdapter);
                translationViewPager.setCurrentItem(previousPage);

                break ;

        }

        return super.onOptionsItemSelected(item);
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
            return new TafseerFragment().newInstance(6235 - position, bookID);
        }

        @Override
        public int getCount() {
            return 6236;
        }
    }


}
