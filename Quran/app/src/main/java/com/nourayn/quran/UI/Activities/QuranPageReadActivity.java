package com.nourayn.quran.UI.Activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nourayn.quran.Audio.service.AudioService;
import com.nourayn.quran.Database.AppPreference;
import com.nourayn.quran.Database.DatabaseAccess;
import com.nourayn.quran.Downloader.DownloadService;
import com.nourayn.quran.Models.Aya;
import com.nourayn.quran.Models.Page;
import com.nourayn.quran.Models.Reader;
import com.nourayn.quran.Models.TranslationBook;
import com.fekracomputers.quran.R;
import com.nourayn.quran.UI.Custom.HighlightImageView;
import com.nourayn.quran.UI.Fragments.QuranPageFragment;
import com.nourayn.quran.Utilities.AppConstants;
import com.nourayn.quran.Utilities.QuranValidateSources;
import com.nourayn.quran.Utilities.Settings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activity for quran page reader
 */
public class QuranPageReadActivity extends AppCompatActivity {
    public static int selectPage, readerID, repeatCounter, repeateValue;
    public static String lastSoraName, downloadLink, readerName;
    public static boolean startBeforeDownload, nextPage;
    private SectionsPagerAdapter SectionsPagerAdapter;
    private ViewPager mViewPager;
    private RelativeLayout myToolbarContainer, footerContainer;
    private boolean flagHideShowTool, tafseerMood, pausePlayFlag, repeatNotRepeat;
    private TextView sorahTitle, sorahInfo, partNumber, pageNumber, soraName;
    private ImageView back;
    private int bookmarkID;
    private Spinner translationBooks, readers;
    private LinearLayout normalFooter, downloadFooter, playerFooter;
    private List<String> bookNames;
    private List<Integer> bookIDs;
    private List<TranslationBook> booksInfo;
    private List<Reader> readersList;
    private Intent playForward, pause, playBack, play, stop, repeat, notRepeat;
    private AudioManager audioManager;
    private int screenHeight;
    private ProgressBar mediaPlayerDownloadProgress;
    private List<Aya> ayaList;
    ObjectAnimator toolbarAnimY;

    /**
     * Function to create activity view
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_quran_page_read);
        init();
    }

    /**
     * Function do some thing in press back button
     */
    @Override
    public void onBackPressed() {
        ((ImageButton) findViewById(R.id.pause_play)).setImageResource(R.drawable.ic_pause);
        playerFooter.setVisibility(View.GONE);
        normalFooter.setVisibility(View.VISIBLE);
        sendBroadcast(stop);

        //clear saved selected aya
        AppPreference.setSelectionVerse(null);

        if (QuranPageFragment.SELECTION) {
            closeSelectionMode();
        } else {
            super.onBackPressed();
            AppPreference.setLastPageRead(mViewPager.getCurrentItem());
            if (bookmarkID != -1)
                new DatabaseAccess().updateBookmark(bookmarkID, (604 - mViewPager.getCurrentItem()));
            SectionsPagerAdapter = null;
            System.gc();
            finish();
        }
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //volume down and check volume key for navigation
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) && AppPreference.isVolumeKeyNavigation()) {
            if (selectPage > 604) selectPage = 604;
            mViewPager.setCurrentItem((604 - (++selectPage)), true);
            //generate silent tone to disable volume sound
            ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 0);
            toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
            //volume up and check volume key for navigation
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && AppPreference.isVolumeKeyNavigation()) {
            if (selectPage < 0) selectPage = 1;
            mViewPager.setCurrentItem((604 - (--selectPage)), true);
            //generate silent tone to disable volume sound
            ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 0);
            toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //call on back button
            onBackPressed();
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && !AppPreference.isVolumeKeyNavigation()) {
            //volume down
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 1);
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && !AppPreference.isVolumeKeyNavigation()) {
            //volume up
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 1);
        }
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();

        //register broadcast for media player
        LocalBroadcastManager.getInstance(this).registerReceiver(MediaPlayer, new IntentFilter(AppConstants.MediaPlayer.INTENT));
        //register broadcast for download ayat
        LocalBroadcastManager.getInstance(this).registerReceiver(downloadPageAya, new IntentFilter(AppConstants.Download.INTENT));

        //make footer change to normal if audio end in pause
        if (!Settings.isMyServiceRunning(this, AudioService.class)) {
            playerFooter.setVisibility(View.GONE);
            normalFooter.setVisibility(View.VISIBLE);
        } else {
            playerFooter.setVisibility(View.VISIBLE);
            normalFooter.setVisibility(View.GONE);
        }

        //check if the service paused in background
        if (Settings.isMyServiceRunning(this, AudioService.class) && AudioService.mediaPaused == true) {
            ((ImageButton) findViewById(R.id.pause_play)).setImageResource(R.drawable.ic_play);
            pausePlayFlag = true;
        }

        //check the selected page
        if (Settings.isMyServiceRunning(this, AudioService.class) && AudioService.pageNumber != 0) {
            mViewPager.setCurrentItem(604 - (AudioService.pageNumber));
        }

    }

    /**
     * Function before destroy activity
     */
    @Override
    protected void onPause() {
        super.onPause();
        //unregister broadcast for media player
        LocalBroadcastManager.getInstance(this).unregisterReceiver(MediaPlayer);
        //unregister broadcast for download ayat
        LocalBroadcastManager.getInstance(this).unregisterReceiver(downloadPageAya);
        //stop flag of auto start
        startBeforeDownload = false;

    }

    /**
     * Function to init Quran page read activity
     */
    private void init() {

        //check if user not allow screen rotation
        if (AppPreference.isScreeRotationDisabled())
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Media player remote view intents
        Intent extras = getIntent();
        playForward = new Intent(AppConstants.MediaPlayer.FORWARD);
        pause = new Intent(AppConstants.MediaPlayer.PAUSE);
        playBack = new Intent(AppConstants.MediaPlayer.BACK);
        play = new Intent(AppConstants.MediaPlayer.PLAY);
        stop = new Intent(AppConstants.MediaPlayer.STOP);
        repeat = new Intent(AppConstants.MediaPlayer.REPEAT_ON);
        notRepeat = new Intent(AppConstants.MediaPlayer.REPEAT_OFF);

        //init variables and views
        myToolbarContainer = (RelativeLayout) findViewById(R.id.appbar);
        footerContainer = (RelativeLayout) findViewById(R.id.footerbar);
        mViewPager = (ViewPager) findViewById(R.id.container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        sorahTitle = (TextView) findViewById(R.id.soraTitle);
        back = (ImageView) findViewById(R.id.back);
        soraName = (TextView) findViewById(R.id.textView15);
        partNumber = (TextView) findViewById(R.id.textView14);
        pageNumber = (TextView) findViewById(R.id.pageNumber);
        normalFooter = (LinearLayout) findViewById(R.id.normalfooter);
        downloadFooter = (LinearLayout) findViewById(R.id.footerdownload);
        playerFooter = (LinearLayout) findViewById(R.id.footerplayer);
        translationBooks = (Spinner) findViewById(R.id.tafaseer);
        readers = (Spinner) findViewById(R.id.selectReader);
        sorahInfo = (TextView) findViewById(R.id.soraInfo);
        mediaPlayerDownloadProgress = (ProgressBar) findViewById(R.id.downloadProgress);
        Typeface type = Typeface.createFromAsset(getAssets(), "simple.otf");
        flagHideShowTool = false;
        tafseerMood = false;

        //change icons of media player in arabic mood
        if (AppPreference.isArabicMood(this)) {
            ((ImageButton) findViewById(R.id.before)).setImageResource(R.drawable.ic_next);
            ((ImageButton) findViewById(R.id.forward)).setImageResource(R.drawable.ic_previouse);
        }

        //init page adapters and type faces
        bookmarkID = extras.getIntExtra(AppConstants.General.BOOK_MARK, -1);
        sorahTitle.setTypeface(type);
        sorahInfo.setTypeface(type);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        SectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(SectionsPagerAdapter);
        setToolbarData(extras.getIntExtra(AppConstants.General.PAGE_NUMBER, 1));
        mViewPager.setCurrentItem(extras.getIntExtra(AppConstants.General.PAGE_NUMBER, 1));
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //switch between footer view
        if (Settings.isMyServiceRunning(this, AudioService.class)) {
            normalFooter.setVisibility(View.GONE);
            playerFooter.setVisibility(View.VISIBLE);
        }

        //tool bar back button
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppPreference.setLastPageRead(mViewPager.getCurrentItem());
                if (bookmarkID != -1)
                    new DatabaseAccess().updateBookmark(bookmarkID, (604 - mViewPager.getCurrentItem()));
                QuranPageReadActivity.this.finish();
            }
        });

        //on change page listener to show page information in toolbar
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                closeSelectionMode();
            }

            @Override
            public void onPageSelected(final int position) {
                //change the toolbar informations
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (tafseerMood == false) setToolbarData(position);
                    }
                });
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                closeSelectionMode();
            }
        });


        //spinner on click listener for readers
        readers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                readerName = readers.getSelectedItem().toString();
                getReaderAudioLink(readerName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //thread to load readers names and book titles
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //check language to load readers arabic or english
                List<String> readersNames = new ArrayList<String>();
                readersList = new DatabaseAccess().getAllReaders();
                for (Reader reader : readersList) {
                    if (reader.audioType == 0) {
                        if (Locale.getDefault().getDisplayLanguage().equals("العربية"))
                            readersNames.add(reader.readerName);
                        else
                            readersNames.add(reader.readerNameEnglish);
                    }
                }

                //add custom spinner adapter for readers
                ArrayAdapter<String> spinnerReaderAdapter = new ArrayAdapter<String>(QuranPageReadActivity
                        .this, R.layout.spinner_layout_larg, R.id.spinnerText, readersNames);
                readers.setAdapter(spinnerReaderAdapter);

                //load books names
                bookNames = new ArrayList<String>();
                bookIDs = QuranValidateSources.getDownloadedTransaltions();
                booksInfo = new DatabaseAccess().getAllTranslations();
                for (int bookID : bookIDs) {
                    for (TranslationBook bookInfo : booksInfo) {
                        if (bookID == bookInfo.bookID) bookNames.add(bookInfo.bookName);
                    }
                }

                //add custom spinner adapter for books
                ArrayAdapter<String> spinnerBooksAdapter = new ArrayAdapter<String>(QuranPageReadActivity
                        .this, R.layout.spinner_layout, R.id.spinnerText, bookNames);
                translationBooks.setAdapter(spinnerBooksAdapter);
            }
        });

        //hide toolbar and footer after time
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideFooter();
                hideToolbar();
            }
        }, 2000);


    }

    /**
     * Function to get reader link from by name
     *
     * @param readerName Reader name
     */
    public void getReaderAudioLink(String readerName) {
        for (Reader reader : readersList) {

            if (reader.readerName == readerName && Locale.getDefault().getDisplayLanguage().equals("العربية")) {
                downloadLink = reader.downloadUrl;
                readerID = reader.readerID;
                break;
            } else if (reader.readerNameEnglish == readerName) {
                downloadLink = reader.downloadUrl;
                readerID = reader.readerID;
                break;
            }

        }
    }

    /**
     * Function with flag to hide and show toolbar
     */
    public synchronized void showHideToolBar() {
        if (!flagHideShowTool) {
            hideToolbar();
            hideFooter();
            HighlightImageView.inAnimation = false;
            flagHideShowTool = true;
        } else {
            showToolbar();
            showFooter();
            HighlightImageView.inAnimation = false;
            flagHideShowTool = false;
        }
    }

    /**
     * Function to create menu items
     *
     * @param menu Menu object
     * @return Menu created of not
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_quran_page_read, menu);
        MenuItem bookmark = menu.findItem(R.id.bookmark);
        if (bookmarkID != -1) {
            bookmark.setIcon(R.drawable.ic_favo);
        }

        return true;
    }

    /**
     * Function to set actions to menu items
     *
     * @param item Menu item selected
     * @return flag return menu selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.tafser) {
            startActivity(new Intent(this, TranslationReadActivity.class)
                    .putExtra(AppConstants.General.PAGE_NUMBER, mViewPager.getCurrentItem()));
        } else if (id == R.id.bookmark) {
            //check if the page is bookmark or not
            if (bookmarkID == -1) {
                bookmarkID = new DatabaseAccess().bookmark(604 - mViewPager.getCurrentItem());
                if (bookmarkID != -1) item.setIcon(R.drawable.ic_favo);
            } else {
                if (new DatabaseAccess().removeBookmark(bookmarkID)) {
                    item.setIcon(R.drawable.ic_not_fav);
                    bookmarkID = -1;
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        /**
         * Function to get fragment by positiion
         *
         * @param position Position of view pager
         * @return Fragment in position
         */
        @Override
        public Fragment getItem(final int position) {
            return new QuranPageFragment().newInstance(position);
        }

        /**
         * Function to get fragments count
         *
         * @return
         */
        @Override
        public int getCount() {
            return 604;
        }

    }


    /**
     * Function to fill toolbar and page header footer with page info
     *
     * @param position Number of page
     */
    public void setToolbarData(int position) {

        Page page = new DatabaseAccess().getPageInfo((604 - position));

        //load sura information depend on application language
        if (AppPreference.isArabicMood(this) == true) {
            sorahTitle.setText(getResources().getString(R.string.sora) + " " + page.soraName);
            soraName.setText(getResources().getString(R.string.sora) + " " + page.soraName);
            lastSoraName = page.soraName;
        } else {
            sorahTitle.setText(getResources().getString(R.string.sora) + " " + page.soraNameEnglish.replace("$$$", "'"));
            soraName.setText(getResources().getString(R.string.sora) + " " + page.soraNameEnglish.replace("$$$", "'"));
            lastSoraName = page.soraNameEnglish;
        }

        //page number information
        sorahInfo.setText(Settings.ChangeNumbers(this, getResources().getString(R.string.page) + " " + page.pageNumber + ", " + getResources().getString(R.string.juza) + " " + page.jozaNumber));
        partNumber.setText(getResources().getString(R.string.juza) + " " + page.jozaNumber);
        pageNumber.setText(page.pageNumber + "");
        selectPage = 604 - position;
    }

    /**
     * Function to hide tool bar
     */
    public void hideToolbar() {

        ObjectAnimator toolbarAnimY = ObjectAnimator.ofFloat(myToolbarContainer, "y", -(myToolbarContainer.getHeight()));
        AnimatorSet toolbarHideAnimation = new AnimatorSet();
        toolbarHideAnimation.setInterpolator(new LinearInterpolator());
        toolbarHideAnimation.play(toolbarAnimY);
        toolbarHideAnimation.start();
    }

    /**
     * Function to show tool bar
     */
    public void showToolbar() {
        ObjectAnimator animY = ObjectAnimator.ofFloat(myToolbarContainer, "y", 0);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.setInterpolator(new LinearInterpolator());
        animSetXY.play(animY);
        animSetXY.start();
    }

    /**
     * Function to show footer
     */
    public void showFooter() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenHeight = metrics.heightPixels;
        ObjectAnimator animY = ObjectAnimator.ofFloat(footerContainer, "y", screenHeight
                - footerContainer.getHeight());
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.setInterpolator(new LinearInterpolator());
        animSetXY.playSequentially(animY);
        animSetXY.start();
    }

    /**
     * Function to hide footer
     */
    public void hideFooter() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenHeight = metrics.heightPixels;
        ObjectAnimator animY = ObjectAnimator.ofFloat(footerContainer, "y", screenHeight
                + footerContainer.getHeight());
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.setInterpolator(new LinearInterpolator());
        animSetXY.playSequentially(animY);
        animSetXY.start();
    }

    /**
     * Function to close selection mood
     */
    public void closeSelectionMode() {

        if (Settings.isMyServiceRunning(QuranPageReadActivity.this, AudioService.class))
            return;

        Intent resetImage = new Intent(AppConstants.Highlight.RESET_IMAGE);
        resetImage.putExtra(AppConstants.Highlight.RESET, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resetImage);

        QuranPageFragment.SELECTION = false;
        HighlightImageView.selectionFromTouch = false;
    }

    /**
     * Function to perform media player buttons actions
     *
     * @param v View clicked on
     */
    public void onMediaPlayerButtonClick(View v) {
        if (v == findViewById(R.id.play)) {
            footerPlay();
        } else if (v == findViewById(R.id.stop)) {
            //Stop media player
            ((ImageButton) findViewById(R.id.pause_play)).setImageResource(R.drawable.ic_pause);
            playerFooter.setVisibility(View.GONE);
            normalFooter.setVisibility(View.VISIBLE);
            sendBroadcast(stop);

        } else if (v == findViewById(R.id.pause_play)) {
            //Pause and play media
            if (pausePlayFlag != true) {
                ((ImageButton) findViewById(R.id.pause_play))
                        .setImageResource(R.drawable.ic_play);
                sendBroadcast(pause);
                pausePlayFlag = true;
            } else {
                ((ImageButton) findViewById(R.id.pause_play))
                        .setImageResource(R.drawable.ic_pause);
                sendBroadcast(play);
                pausePlayFlag = false;
            }
        } else if (v == findViewById(R.id.repeat)) {

            repeatCounter++;
            repeateValue = repeatCounter;
            switch (repeatCounter) {
                case 1:
                    repeatNotRepeat = true;
                    sendBroadcast(repeat);
                    ((ImageButton) findViewById(R.id.repeat)).setImageBitmap(drawNumbers("1"));
                    break;
                case 2:
                    repeatNotRepeat = true;
                    sendBroadcast(repeat);
                    ((ImageButton) findViewById(R.id.repeat)).setImageBitmap(drawNumbers("2"));
                    break;
                case 3:
                    repeatNotRepeat = true;
                    sendBroadcast(repeat);
                    ((ImageButton) findViewById(R.id.repeat)).setImageBitmap(drawNumbers("3"));
                    break;
                case 4:
                    repeatNotRepeat = true;
                    sendBroadcast(repeat);
                    ((ImageButton) findViewById(R.id.repeat)).setImageBitmap(drawNumbers("∞"));
                    repeatCounter = -1;
                    break;
                default:
                    repeatNotRepeat = false;
                    sendBroadcast(notRepeat);
                    ((ImageButton) findViewById(R.id.repeat)).setImageResource(R.drawable.ic_repeat);
                    repeatCounter = 0;
                    break;
            }

        } else if (v == findViewById(R.id.before)) {
            //play the before aya
            sendBroadcast(playBack);
        } else if (v == findViewById(R.id.forward)) {
            //play forward aya
            sendBroadcast(playForward);
        } else if (v == findViewById(R.id.canceldownload)) {
            downloadFooter.setVisibility(View.GONE);
            normalFooter.setVisibility(View.VISIBLE);
            //stop flag of auto start audio after download
            startBeforeDownload = false;
            //stop download service
            stopService(new Intent(this, DownloadService.class));
        }
    }

    /**
     * Function to play media player
     */
    private void footerPlay() {
        //check if other instance of Audio service run
        if (Settings.isMyServiceRunning(this, AudioService.class)) return;

        //get all ayat information of the page
        ayaList = new DatabaseAccess().getPageAyat((604 - mViewPager.getCurrentItem()));

        //check the internet statue
        int internetStatus = Settings.checkInternetStatus(this);
        if (AppPreference.isStreamMood()) {
            //check internet is opened or not to start stream
            if (internetStatus <= 0) {
                AlertDialog.Builder builder =
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
                //start stream mood
                normalFooter.setVisibility(View.GONE);
                playerFooter.setVisibility(View.VISIBLE);
                Intent player = new Intent(this, AudioService.class);
                player.putExtra("streamLink", downloadLink);
                player.putExtra(AppConstants.MediaPlayer.PAGE, selectPage);
                player.putExtra(AppConstants.MediaPlayer.READER, readerID);
                startService(player);

            }

        } else {

            //check if there is other download in progress
            if (!Settings.isMyServiceRunning(this, DownloadService.class)) {
                //internal media play
                List<String> Links = createDownloadLinks();
                if (Links.size() != 0) {
                    //check if the internet is opened
                    if (internetStatus <= 0) {
                        AlertDialog.Builder builder =
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
                        //change view of footer to media
                        normalFooter.setVisibility(View.GONE);
                        playerFooter.setVisibility(View.GONE);
                        downloadFooter.setVisibility(View.VISIBLE);

                        //check audio folders
                        String filePath = Environment
                                .getExternalStorageDirectory()
                                .getAbsolutePath()
                                + getString(R.string.AppPath)
                                + "/Audio/" + readerID;

                        //make dirs if not found
                        File file = new File(filePath);
                        if (!file.exists()) file.mkdirs();

                        //auto start audio after download
                        startBeforeDownload = true;

                        //start download service
                        startService(new Intent(QuranPageReadActivity.this, DownloadService.class)
                                .putStringArrayListExtra(AppConstants.Download.DOWNLOAD_LINKS, (ArrayList<String>) Links)
                                .putExtra(AppConstants.Download.DOWNLOAD_LOCATION, filePath));
                    }

                } else {
                    //Start media player service
                    startService(new Intent(QuranPageReadActivity.this, AudioService.class)
                            .putExtra(AppConstants.MediaPlayer.PAGE, selectPage)
                            .putExtra(AppConstants.MediaPlayer.READER, readerID));

                }
            } else {
                //Other thing in download
                Toast.makeText(this, getString(R.string.download_busy), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Function to create download link
     */
    public List<String> createDownloadLinks() {

        List<String> downloadLinks = new ArrayList<>();
        ayaList.add(0, new Aya(1, 1, 1));
        //loop for all page ayat
        for (Aya ayaItem : ayaList) {
            //validate if aya download or not
            if (!QuranValidateSources.validateAyaAudio(this, readerID, ayaItem.ayaID, ayaItem.suraID)) {

                //create aya link
                int suraLength = String.valueOf(ayaItem.suraID).trim().length();
                String suraID = ayaItem.suraID + "";
                int ayaLength = String.valueOf(ayaItem.ayaID).trim().length();
                String ayaID = ayaItem.ayaID + "";
                if (suraLength == 1)
                    suraID = "00" + ayaItem.suraID;
                else if (suraLength == 2)
                    suraID = "0" + ayaItem.suraID;

                if (ayaLength == 1)
                    ayaID = "00" + ayaItem.ayaID;
                else if (ayaLength == 2)
                    ayaID = "0" + ayaItem.ayaID;

                //add aya link to list
                downloadLinks.add(downloadLink + suraID + ayaID + AppConstants.Extensions.MP3);
                Log.d("DownloadLinks", downloadLink + suraID + ayaID + AppConstants.Extensions.MP3);
            }
        }
        ayaList.remove(0);
        return downloadLinks;
    }

    /**
     * Function to draw number in the bitmap
     *
     * @param number Number to draw
     * @return the new bitmap after draw
     */
    public Bitmap drawNumbers(String number) {
        float fontAndPadding = getResources().getDimension(R.dimen.draw_number);
        Paint paint = new Paint();
        paint.setTextSize(fontAndPadding);
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        paint.setFakeBoldText(true);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_repeat);
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        canvas.drawText(number, bitmap.getWidth() - fontAndPadding, fontAndPadding, paint);
        return mutableBitmap;
    }

    /**
     * BroadcastReceiver to change media player views
     */
    private BroadcastReceiver MediaPlayer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //media player intent receive
            if (intent.getBooleanExtra(AppConstants.MediaPlayer.STOP, false) == true) {
                ((ImageButton) findViewById(R.id.pause_play)).setImageResource(R.drawable.ic_pause);
                ((ImageButton) findViewById(R.id.repeat)).setImageResource(R.drawable.ic_repeat);
                playerFooter.setVisibility(View.GONE);
                normalFooter.setVisibility(View.VISIBLE);
            } else if (intent.getBooleanExtra(AppConstants.MediaPlayer.PLAY, true) == true) {
                normalFooter.setVisibility(View.GONE);
                playerFooter.setVisibility(View.VISIBLE);
            } else if (intent.getBooleanExtra(AppConstants.MediaPlayer.PAUSE, false) == true) {
                ((ImageButton) findViewById(R.id.pause_play)).setImageResource(R.drawable.ic_play);
            } else if (intent.getBooleanExtra(AppConstants.MediaPlayer.RESUME, false) == false) {
                ((ImageButton) findViewById(R.id.pause_play)).setImageResource(R.drawable.ic_pause);
            }

            if (intent.getIntExtra(AppConstants.MediaPlayer.OTHER_PAGE, -1) == 1) {
                int currentPage = mViewPager.getCurrentItem();
                mViewPager.setCurrentItem(--currentPage);
            } else if (intent.getIntExtra(AppConstants.MediaPlayer.OTHER_PAGE, -1) == 0) {
                int currentPage = mViewPager.getCurrentItem();
                mViewPager.setCurrentItem(++currentPage);
            } else if (intent.getIntExtra(AppConstants.MediaPlayer.OTHER_PAGE, -1) == 2) {
                mViewPager.setCurrentItem(604);
            } else if (intent.getIntExtra(AppConstants.MediaPlayer.OTHER_PAGE, -1) == 3) {
                mViewPager.setCurrentItem(0);
            }

        }
    };


    /**
     * Broadcast receiver to download page ayat
     */
    private BroadcastReceiver downloadPageAya = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int value = (int) intent.getLongExtra(AppConstants.Download.NUMBER, 0);
            int max = (int) intent.getLongExtra(AppConstants.Download.MAX, 0);
            String status = intent.getStringExtra(AppConstants.Download.DOWNLOAD);

            if (status != null) {
                if (status.equals(AppConstants.Download.IN_DOWNLOAD)) {
                    downloadFooter.setVisibility(View.VISIBLE);
                    normalFooter.setVisibility(View.GONE);
                    playerFooter.setVisibility(View.GONE);
                    mediaPlayerDownloadProgress.setMax(max);
                    mediaPlayerDownloadProgress.setProgress(value);
                } else if (status.equals(AppConstants.Download.FAILED)) {
                    mediaPlayerDownloadProgress.setMax(1);
                    mediaPlayerDownloadProgress.setProgress(1);
                } else if (status.equals(AppConstants.Download.SUCCESS)) {
                    mediaPlayerDownloadProgress.setMax(1);
                    mediaPlayerDownloadProgress.setProgress(1);
                    //check if you auto play after download
                    if (startBeforeDownload == true) {
                        //change views
                        downloadFooter.setVisibility(View.GONE);
                        normalFooter.setVisibility(View.GONE);
                        playerFooter.setVisibility(View.VISIBLE);

                        startService(new Intent(QuranPageReadActivity.this, AudioService.class)
                                .putExtra(AppConstants.MediaPlayer.PAGE, selectPage)
                                .putExtra(AppConstants.MediaPlayer.READER, readerID));
                    } else {
                        downloadFooter.setVisibility(View.GONE);
                        normalFooter.setVisibility(View.VISIBLE);
                        playerFooter.setVisibility(View.GONE);
                    }

                }

            }
        }
    };


}
