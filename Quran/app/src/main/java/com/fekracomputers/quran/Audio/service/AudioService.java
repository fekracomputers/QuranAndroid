package com.fekracomputers.quran.Audio.service;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import com.fekracomputers.quran.Audio.notification.SmallMediaPlayer;
import com.fekracomputers.quran.Database.DatabaseAccess;
import com.fekracomputers.quran.Models.Aya;
import com.fekracomputers.quran.UI.Activities.QuranPageReadActivity;
import com.fekracomputers.quran.Utilities.AppConstants;
import com.fekracomputers.quran.Utilities.FileManager;

import java.util.ArrayList;
import java.util.List;




public class AudioService extends Service {
    public static boolean mediaPaused, screenOff;
    public static int pageNumber;
    public int fromAya;
    private AudioManager audioManager;
    private int reader;
    private int AyaToPlay, suraOfAya;
    private String streamURL;
    private Window window;
    private Window mWindow;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //receive intents
        fromAya = intent.getIntExtra(AppConstants.MediaPlayer.VERSE, -1);
        pageNumber = intent.getIntExtra(AppConstants.MediaPlayer.PAGE, -1);
        reader = intent.getIntExtra(AppConstants.MediaPlayer.READER, -1);
        AyaToPlay = intent.getIntExtra(AppConstants.MediaPlayer.ONE_VERSE, -1);
        suraOfAya = intent.getIntExtra(AppConstants.MediaPlayer.SURA, -1);
        streamURL = intent.getStringExtra(AppConstants.MediaPlayer.STREAM_LINK);

        if (AyaToPlay == -1 && suraOfAya == -1) {
            prepareVersesToPlay();
        } else {
            prepareOneVerseToPlay();
        }

        return START_STICKY;
    }

    private BroadcastReceiver phoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra("state",0);
            if(state==1){
                //receive incoming call
                if(audioManager.mediaPlayer!=null && audioManager.mediaPlayer.isPlaying()){
                    audioManager.pauseMedia();
                    mediaPaused = true;
                }
                //rejected_incoming_call
            }else if(state==2){
                if(audioManager.mediaPlayer!=null && ! audioManager.mediaPlayer.isPlaying()){
                    audioManager.resumeMedia();
                    mediaPaused = false;
                }
            }
            //EXTRA_STATE_OFFHOOK
        else if(state==3){
            if(audioManager.mediaPlayer!=null && ! audioManager.mediaPlayer.isPlaying()){
                audioManager.pauseMedia();
                mediaPaused = true;
            }
        }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(phoneReceiver,new IntentFilter("receive_incoming_call"));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(phoneReceiver,new IntentFilter("rejected_incoming_call"));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(phoneReceiver,new IntentFilter("EXTRA_STATE_OFFHOOK"));
        //media player intents
        IntentFilter mediaPlayerFilter = new IntentFilter();
        mediaPlayerFilter.addAction(AppConstants.MediaPlayer.PLAY);
        mediaPlayerFilter.addAction(AppConstants.MediaPlayer.PAUSE);
        mediaPlayerFilter.addAction(AppConstants.MediaPlayer.BACK);
        mediaPlayerFilter.addAction(AppConstants.MediaPlayer.FORWARD);
        mediaPlayerFilter.addAction(AppConstants.MediaPlayer.STOP);
        mediaPlayerFilter.addAction(AppConstants.MediaPlayer.REPEAT_ON);
        mediaPlayerFilter.addAction(AppConstants.MediaPlayer.REPEAT_OFF);
        mediaPlayerFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        registerReceiver(MediaPlayerBroadcast, mediaPlayerFilter);

        //screen on / of guard key unlock flag
        IntentFilter screenIntentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        screenIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenIntentFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(MediaPlayerNotificationShow, screenIntentFilter);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(phoneReceiver);
        audioManager.finished = false;
        unregisterReceiver(MediaPlayerBroadcast);
        unregisterReceiver(MediaPlayerNotificationShow);
    }


    /**
     * Function to prepare verses to play
     */
    public synchronized void prepareVersesToPlay() {

        //get page ayat
        List<Aya> verses = new DatabaseAccess().getPageAyat(pageNumber);

        //get current page user will play
        pageNumber = verses.get(0).pageNumber;
        int pageFirstAya = verses.get(0).ayaID;

        //check if you call media player in stream or download
        if (streamURL != null) {

            //modify aya list to add basmala between suras
            List<Aya> ayatWithBasmala = new ArrayList<>();
            ayatWithBasmala.addAll(verses);
            int previousSura = verses.get(0).suraID, counter = 0, positionCounter = -1;

            if (fromAya == -1) {
                //loop to add basmala between suras
                for (Aya ayaItem : verses) {
                    counter++;
                    if (ayaItem.suraID != previousSura) {
                        ayatWithBasmala.add(counter + positionCounter, new Aya(1, 1, 1));
                        previousSura = ayaItem.suraID;
                        positionCounter++;
                    }
                }

                //add bsmala in the beginning
                if (pageNumber != 1 && pageNumber != 187 && pageFirstAya == 1) {
                    ayatWithBasmala.add(0, new Aya(1, 1, 1));
                }
            }

            //stream mood
            audioManager = new AudioManager(AudioService.this, ayatWithBasmala, streamURL, pageNumber, false);
            audioManager.AudioPosition = fromAya;
            audioManager.execute();

        } else {

            List<String> ayaLocations = new ArrayList<>();
            //Create files locations for the all page ayas
            for (Aya ayaItem : verses) {
                ayaLocations.add(FileManager.createAyaAudioLinkLocation(AudioService.this, reader, ayaItem.ayaID, ayaItem.suraID));
            }

            //modify aya list to add basmala between suras
            List<Aya> ayatWithBasmala = new ArrayList<>();
            ayatWithBasmala.addAll(verses);
            int previousSura = verses.get(0).suraID, counter = 0, positionCounter = -1;

            if (fromAya == -1) {
                //loop to add basmala between suras
                for (Aya ayaItem : verses) {
                    counter++;
                    if (ayaItem.suraID != previousSura) {
                        ayatWithBasmala.add(counter + positionCounter, new Aya(1, 1, 1));
                        ayaLocations.add(counter + positionCounter, FileManager.createAyaAudioLinkLocation(AudioService.this, QuranPageReadActivity.readerID, 1, 1));
                        previousSura = ayaItem.suraID;
                        positionCounter++;
                    }
                }

                //add bsmala in the beginning
                if (pageNumber != 1 && pageNumber != 187 && pageFirstAya == 1) {
                    ayatWithBasmala.add(0, new Aya(1, 1, 1));
                    ayaLocations.add(0, FileManager.createAyaAudioLinkLocation(AudioService.this, reader, 1, 1));
                }
            }


            //local mood
            audioManager = new AudioManager(AudioService.this, ayaLocations, ayatWithBasmala, pageNumber, false);
            audioManager.AudioPosition = fromAya;
            audioManager.execute();
        }

    }

    /**
     * Function to prepare one verse to play
     */
    public synchronized void prepareOneVerseToPlay() {

        List<Aya> verses = new ArrayList<>();
        verses.add(new Aya(pageNumber, suraOfAya, AyaToPlay));

        //check if you call media player in stream or download
        if (streamURL != null) {
            //stream mood
            audioManager = new AudioManager(AudioService.this, verses, streamURL, pageNumber, true);
            audioManager.AudioPosition = fromAya;
            audioManager.execute();

        } else {

            List<String> ayaLocations = new ArrayList<>();
            //Create files locations for the all page ayas
            for (Aya ayaItem : verses) {
                ayaLocations.add(FileManager.createAyaAudioLinkLocation(AudioService.this, reader, ayaItem.ayaID, ayaItem.suraID));
            }

            //local mood
            audioManager = new AudioManager(AudioService.this, ayaLocations, verses, pageNumber, true);
            audioManager.AudioPosition = fromAya;
            audioManager.execute();
        }
    }


    /**
     * Function to play next page
     */
    public synchronized void nextPage() {
        pageNumber++;
        if (pageNumber == 605) {
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(new Intent(AppConstants.MediaPlayer.INTENT)
                            .putExtra(AppConstants.MediaPlayer.OTHER_PAGE, 2));
            pageNumber = 1;
        }
        prepareVersesToPlay();
    }

    /**
     * Function to play previous page
     */
    public synchronized void previousPage() {
        pageNumber--;
        if (pageNumber == 0) {
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(new Intent(AppConstants.MediaPlayer.INTENT)
                            .putExtra(AppConstants.MediaPlayer.OTHER_PAGE, 3));
            pageNumber = 604;
        }
        List<Aya> verses = new DatabaseAccess().getPageAyat(pageNumber);
        fromAya = verses.size() - 2;
        prepareVersesToPlay();
    }

    /**
     * Broadcast for the media player events
     */
    public BroadcastReceiver MediaPlayerBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i("ACTION_TAG", "tag : "+intent.getAction());
            if (intent.getAction().equals(AppConstants.MediaPlayer.PLAY)) {
                audioManager.resumeMedia();
                mediaPaused = false;
            } else if (intent.getAction().equals(AppConstants.MediaPlayer.PAUSE)) {
                audioManager.pauseMedia();
                mediaPaused = true;
            } else if (intent.getAction().equals(AppConstants.MediaPlayer.BACK)) {
                audioManager.previousAudio();
            } else if (intent.getAction().equals(AppConstants.MediaPlayer.FORWARD)) {
                audioManager.nextAudio();
            } else if (intent.getAction().equals(AppConstants.MediaPlayer.STOP)) {
                audioManager.stopFlag = true;
                audioManager.stopMedia();
            } else if (intent.getAction().equals(AppConstants.MediaPlayer.REPEAT_ON)) {
                audioManager.isLooping = true;
            } else if (intent.getAction().equals(AppConstants.MediaPlayer.REPEAT_OFF)) {
                audioManager.isLooping = false;
            } else if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                audioManager.pauseMedia();
                mediaPaused = true;
            }

        }
    };

    /**
     * Broadcast for the media player notification view
     */
    public BroadcastReceiver MediaPlayerNotificationShow = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                audioManager.showMediaPlayerNotification();
                audioManager.bigNotification = false;
                screenOff = false;
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                audioManager.smallMediaPlayer = SmallMediaPlayer.getInstance(context);
                audioManager.bigNotification = false;
                screenOff = false;
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                audioManager.smallMediaPlayer = SmallMediaPlayer.getInstance(context);
                audioManager.bigNotification = false;
                screenOff = true;
            }
        }
    };


    public void pause() {
        audioManager.pauseMedia();
        mediaPaused = true;
    }

}

