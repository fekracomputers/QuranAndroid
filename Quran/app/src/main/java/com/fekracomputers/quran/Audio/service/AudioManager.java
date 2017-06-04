package com.fekracomputers.quran.Audio.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.fekracomputers.quran.Audio.helper.AudioHelper;
import com.fekracomputers.quran.Audio.notification.LargeMediaPlayer;
import com.fekracomputers.quran.Audio.notification.SmallMediaPlayer;
import com.fekracomputers.quran.Database.AppPreference;
import com.fekracomputers.quran.Database.DatabaseAccess;
import com.fekracomputers.quran.Downloader.DownloadService;
import com.fekracomputers.quran.Models.Aya;
import com.fekracomputers.quran.Models.Sora;
import com.fekracomputers.quran.R;
import com.fekracomputers.quran.UI.Activities.QuranDataActivity;
import com.fekracomputers.quran.UI.Activities.QuranPageReadActivity;
import com.fekracomputers.quran.UI.Custom.HighlightImageView;
import com.fekracomputers.quran.UI.Fragments.QuranPageFragment;
import com.fekracomputers.quran.Utilities.AppConstants;
import com.fekracomputers.quran.Utilities.Settingsss;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import java.io.IOException;
import java.util.List;

import okhttp3.internal.Util;

/**
 * Class for Audio can play audio from path or streaming
 */
public class AudioManager extends AsyncTask<String, Long, Boolean> {
    public int AudioPosition, pageNumber;
    private Context context;
    private String streamURL;
    public MediaPlayer mediaPlayer;
    public boolean isLooping, finished, bigNotification, previousFlag;
    private BroadcastReceiver OutgoingBroadcastReceiver;
    private TelephonyManager telephoneManger;
    private PhoneStateListener phoneStateListener;
    private boolean isInCall, isFirstStart;
    private List<Aya> ayat;
    private List<String> paths;
    public SmallMediaPlayer smallMediaPlayer;
    public LargeMediaPlayer largeMediaPlayer;
    public boolean stopFlag, oneVersePlay;
    private boolean isToastShowing = true;


    /**
     * Constructor for audio manger
     *
     * @param context Application context
     */
    public AudioManager(Context context, List<Aya> ayat, String streamURL, int pageNumber, boolean oneVersePlay) {
        this.context = context;
        this.ayat = ayat;
        this.streamURL = streamURL;
        this.pageNumber = pageNumber;
        this.oneVersePlay = oneVersePlay;
    }

    /**
     * Constructor for audio from path
     *
     * @param context Application context
     * @param paths   Paths list
     */
    public AudioManager(Context context, List<String> paths, List<Aya> ayat, int pageNumber, boolean oneVersePlay) {
        this.context = context;
        this.paths = paths;
        this.ayat = ayat;
        this.pageNumber = pageNumber;
        this.oneVersePlay = oneVersePlay;
    }

    public AudioManager() {

    }

    /**
     * Before start playing sound
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mediaPlayer = new MediaPlayer();

        Intent playMedia = new Intent(AppConstants.MediaPlayer.INTENT);
        playMedia.putExtra(AppConstants.MediaPlayer.PLAY, true);
        LocalBroadcastManager.getInstance(context).sendBroadcast(playMedia);


        IntentFilter intentFilter = new IntentFilter(NetworkStateChangeReceiver.NETWORK_AVAILABLE_ACTION);
        LocalBroadcastManager.getInstance(context).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isNetworkAvailable = intent.getBooleanExtra(NetworkStateChangeReceiver.IS_NETWORK_AVAILABLE, false);
                String networkStatus = isNetworkAvailable ? "connected" : "disconnected";

                if (!isNetworkAvailable) {
                    pauseMedia();
                }
            }
        }, intentFilter);


        //show notification media player
        if (AudioService.screenOff != true)
            showMediaPlayerNotification();

        isInCall = false;
        isFirstStart = true;
        finished = true;
        //initPhoneListener();
        initOutgoingBroadcastReceiver();
    }

    /**
     * Start play sound
     *
     * @param params Media Path
     * @return media play true
     */
    @Override
    protected Boolean doInBackground(String... params) {
        try {
            if (!oneVersePlay)
                nextAudio();
            else
                versePlayAudio();

            while (finished) {

                //Broadcast to announce playing
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(AppConstants.MediaPlayer.INTENT)
                        .putExtra(AppConstants.MediaPlayer.PLAYING, true));

            }

        } catch (Exception e) {
            e.printStackTrace();
            if (bigNotification) {
                largeMediaPlayer.dismiss();
            } else {
                smallMediaPlayer.dismiss();
            }
            return false;
        }
        return true;
    }

    /**
     * Function fire when do in background finished
     *
     * @param aBoolean return of do in background
     */
    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (stopFlag != true && oneVersePlay == false) {
            ((AudioService) context).fromAya = -1;

            //to call next page or previous page
            if (!previousFlag) {
                ((AudioService) context).nextPage();
            } else {
                ((AudioService) context).previousPage();
            }

        } else {

            //unregister broadcasts and release media player
            AudioService.screenOff = false;
            context.unregisterReceiver(OutgoingBroadcastReceiver);
            stopMedia();
            mediaPlayer.release();

            //dismiss the media players in notification
            if (bigNotification) {
                largeMediaPlayer.dismiss();
            } else {
                smallMediaPlayer.dismiss();
            }

            //delete all selection in the image
            Intent resetImage = new Intent(AppConstants.Highlight.RESET_IMAGE);
            resetImage.putExtra(AppConstants.Highlight.RESET , true);
            LocalBroadcastManager.getInstance(context).sendBroadcast(resetImage);

            //stop service
            context.stopService(new Intent(context, AudioService.class));
        }


    }


    public synchronized void versePlayAudio() {
        try {

                    //reset media player
                    mediaPlayer.reset();

                    HighlightImageView.selectionFromTouch = false;

                    //send broadcast to highlight image view
                    final Aya aya = ayat.get(0);
                    Intent highlightAya = new Intent(AppConstants.Highlight.INTENT_FILTER);
                    highlightAya.putExtra(AppConstants.Highlight.VERSE_NUMBER, aya.ayaID);
                    highlightAya.putExtra(AppConstants.Highlight.SORA_NUMBER, aya.suraID);
                    highlightAya.putExtra(AppConstants.Highlight.PAGE_NUMBER, aya.pageNumber);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(highlightAya);
                    AppPreference.setSelectionVerse(aya.pageNumber + "-" + aya.ayaID + "-" + aya.suraID);


                    //check if stream or from path
                    if (AppPreference.isStreamMood()) {
                        mediaPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
                        mediaPlayer.setDataSource(AudioHelper.createStreamLink(aya, streamURL));

                    } else {

                        mediaPlayer.setDataSource(paths.get(0));

                    }

                    mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            if (!isLooping) {
                                stopMedia();
                            } else if (QuranPageReadActivity.repeatCounter != 0) {
                                mediaPlayer.start();
                                QuranPageReadActivity.repeatCounter--;
                            } else {
                                stopMedia();
                                QuranPageReadActivity.repeatCounter = QuranPageReadActivity.repeateValue;
                            }

                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
            //stop the media service and dismiss the notification
            stopFlag = true;
            stopMedia();
            if (smallMediaPlayer != null) smallMediaPlayer.dismiss();
            if (largeMediaPlayer != null) largeMediaPlayer.dismiss();
        }
    }

    /**
     * Function to play next Audio
     */
    public synchronized void nextAudio() {
        try {
            ConnectivityManager connec;
            connec = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);

            // Check for network connections
            if ( connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                    connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                    connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                    connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {

                // if connected with internet

                //reset media player
                mediaPlayer.reset();

                HighlightImageView.selectionFromTouch = false;
                AudioPosition++;
                if (ayat.size() <= AudioPosition) {

                    //stop thread
                    finished = false;

                    if(ayat.size() != 1){
                        //send broadcast to the next page
                        previousFlag = false;
                        LocalBroadcastManager.getInstance(context)
                                .sendBroadcast(new Intent(AppConstants.MediaPlayer.INTENT)
                                        .putExtra(AppConstants.MediaPlayer.OTHER_PAGE, 1));

                        //set the flag of next page false
                        QuranPageReadActivity.nextPage = false;
                    }


                    return;

                }

                Log.i("AUDIO_TAG" , "size next: "+ayat.size());
                Log.i("AUDIO_TAG" , "pos next: "+AudioPosition);
                //send broadcast to highlight image view
                final Aya aya = ayat.get(AudioPosition);
                Intent highlightAya = new Intent(AppConstants.Highlight.INTENT_FILTER);
                highlightAya.putExtra(AppConstants.Highlight.VERSE_NUMBER, aya.ayaID);
                highlightAya.putExtra(AppConstants.Highlight.SORA_NUMBER, aya.suraID);
                highlightAya.putExtra(AppConstants.Highlight.PAGE_NUMBER, aya.pageNumber);
                LocalBroadcastManager.getInstance(context).sendBroadcast(highlightAya);
                AppPreference.setSelectionVerse(aya.pageNumber + "-" + aya.ayaID + "-" + aya.suraID);

                //show information in large notification
                if(largeMediaPlayer != null){

                    if(aya.ayaID == 1 && aya.suraID == 1 && pageNumber != aya.pageNumber){
                        Sora sora = new DatabaseAccess().getSuraNameByID(aya.suraID);
                        largeMediaPlayer.showAudioInformation(context.getString(R.string.basmala));
                    }else{
                        Sora sora = new DatabaseAccess().getSuraNameByID(aya.suraID);
                        largeMediaPlayer.showAudioInformation(context.getString(R.string.sora)+" "
                                +(AppPreference.isArabicMood(context) ? sora.name : sora.name_english )
                                +" "+context.getString(R.string.aya)+" "+ Settingsss.ChangeNumbers(context , aya.ayaID+"") );
                    }

                }

                //check if stream or from path
                if (AppPreference.isStreamMood()) {
                    mediaPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(AudioHelper.createStreamLink(aya, streamURL));

                } else {
                    mediaPlayer.setDataSource(paths.get(AudioPosition));
                }

                //play mediaPlayer in other thread
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                    }
                });
                mediaPlayer.prepareAsync();

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (!isLooping) {
                            nextAudio();
                        } else if (QuranPageReadActivity.repeatCounter != 0) {
                            mediaPlayer.start();
                            QuranPageReadActivity.repeatCounter--;
                        } else {
                            nextAudio();
                            QuranPageReadActivity.repeatCounter = QuranPageReadActivity.repeateValue;
                        }

                    }
                });
                isToastShowing = false;
            } else if (
                    connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                            connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED  ) {

                if (!isToastShowing) {
                    Toast.makeText(context, " Not Connected ", Toast.LENGTH_LONG).show();
                    isToastShowing = true;
                }


pauseMedia();

            }


        } catch (IOException e) {
            e.printStackTrace();
            //stop the media service and dismiss the notification
            stopFlag = true;
            stopMedia();
            if (smallMediaPlayer != null) smallMediaPlayer.dismiss();
            if (largeMediaPlayer != null) largeMediaPlayer.dismiss();
        }
    }

    /**
     * Function to play previous order
     */
    public synchronized void previousAudio() {
        try {

            //reset media player
            mediaPlayer.reset();

            if(ayat.size() == 1) {
                finished = false;
                return;
            }

            HighlightImageView.selectionFromTouch = false;
            AudioPosition--;
            if (AudioPosition < 0) {

                //stop thread
                finished = false;
                if(ayat.size() != 1) {
                    //send broad cast to go to previous flag
                    previousFlag = true;
                    LocalBroadcastManager.getInstance(context)
                            .sendBroadcast(new Intent(AppConstants.MediaPlayer.INTENT)
                                    .putExtra(AppConstants.MediaPlayer.OTHER_PAGE, 0));
                }
                return;
            }

            Log.i("AUDIO_TAG" , "size prev: "+ayat.size());
            Log.i("AUDIO_TAG" , "pos prev: "+AudioPosition);
            //send broadcast to highlight image view
            Aya aya = ayat.get(AudioPosition);
            Intent highlightAya = new Intent(AppConstants.Highlight.INTENT_FILTER);
            highlightAya.putExtra(AppConstants.Highlight.VERSE_NUMBER, aya.ayaID);
            highlightAya.putExtra(AppConstants.Highlight.SORA_NUMBER, aya.suraID);
            highlightAya.putExtra(AppConstants.Highlight.PAGE_NUMBER, aya.pageNumber);
            LocalBroadcastManager.getInstance(context).sendBroadcast(highlightAya);
            AppPreference.setSelectionVerse(aya.pageNumber + "-" + aya.ayaID + "-" + aya.suraID);

            //show information in large notification
            if(largeMediaPlayer != null){

                if(aya.ayaID == 1 && aya.suraID == 1 && pageNumber != aya.pageNumber){
                    Sora sora = new DatabaseAccess().getSuraNameByID(aya.suraID);
                    largeMediaPlayer.showAudioInformation(context.getString(R.string.basmala));
                }else{
                    Sora sora = new DatabaseAccess().getSuraNameByID(aya.suraID);
                    largeMediaPlayer.showAudioInformation(context.getString(R.string.sora)+" "
                            +(AppPreference.isArabicMood(context) ? sora.name : sora.name_english )
                            +" "+context.getString(R.string.aya)+" "+ Settingsss.ChangeNumbers(context , aya.ayaID+"") );
                }

            }

            //check if stream or from path
            if (AppPreference.isStreamMood()) {
                mediaPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(AudioHelper.createStreamLink(aya, streamURL));
            } else {
                mediaPlayer.setDataSource(paths.get(AudioPosition));
            }

            //play mediaPlayer in other thread
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });

            mediaPlayer.prepareAsync();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (!isLooping) {
                        nextAudio();
                    } else if (QuranPageReadActivity.repeatCounter != 0) {
                        mediaPlayer.start();
                        QuranPageReadActivity.repeatCounter--;
                    } else {
                        nextAudio();
                        QuranPageReadActivity.repeatCounter = QuranPageReadActivity.repeateValue;
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            //stop the media service and dismiss the notification
            stopFlag = true;
            stopMedia();
            if (smallMediaPlayer != null) smallMediaPlayer.dismiss();
            if (largeMediaPlayer != null) largeMediaPlayer.dismiss();

        }
    }

    /**
     * Pause media
     */
    public synchronized void pauseMedia() {

        Intent pauseMedia = new Intent(AppConstants.MediaPlayer.INTENT);
        pauseMedia.putExtra(AppConstants.MediaPlayer.PAUSE, true);
        pauseMedia.putExtra(AppConstants.MediaPlayer.PLAY, false);
        LocalBroadcastManager.getInstance(context).sendBroadcast(pauseMedia);

        mediaPlayer.pause();
        if (bigNotification != true || isInCall == true) {
            if (smallMediaPlayer != null)
            smallMediaPlayer.pause();
            Log.d("MEDIA_PLAYER", "pause small");
        } else {
            if (largeMediaPlayer != null)
            largeMediaPlayer.pause();
            Log.d("MEDIA_PLAYER", "pause large");
        }

    }


    /**
     * Stop media
     */
    public synchronized void stopMedia() {

        Intent stopMedia = new Intent(AppConstants.MediaPlayer.INTENT);
        stopMedia.putExtra(AppConstants.MediaPlayer.STOP, true);
        LocalBroadcastManager.getInstance(context).sendBroadcast(stopMedia);
        cancelMediaPlayerNotification();

        AppPreference.setSelectionVerse(null);
        finished = false;
        if (telephoneManger != null)
            telephoneManger.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);



    }

    /**
     * Resume media
     */
    public synchronized void resumeMedia() {

        Intent resumeMedia = new Intent(AppConstants.MediaPlayer.INTENT);
        resumeMedia.putExtra(AppConstants.MediaPlayer.RESUME, false);
        LocalBroadcastManager.getInstance(context).sendBroadcast(resumeMedia);

        mediaPlayer.start();
        if (bigNotification != true || isInCall == true) {
            if (smallMediaPlayer != null)
            smallMediaPlayer.resume();
            Log.d("MEDIA_PLAYER", "resume small");
        } else {
            if (largeMediaPlayer != null)
            largeMediaPlayer.resume();
            Log.d("MEDIA_PLAYER", "resume large");
        }

    }



    /* You can write such method somewhere in utility class and call it NetworkChangeReceiver like below */
    public class NetworkStateChangeReceiver extends BroadcastReceiver {
        public static final String NETWORK_AVAILABLE_ACTION = "com.ajit.singh.NetworkAvailable";
        public static final String IS_NETWORK_AVAILABLE = "isNetworkAvailable";

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent networkStateIntent = new Intent(NETWORK_AVAILABLE_ACTION);
            networkStateIntent.putExtra(IS_NETWORK_AVAILABLE,  isConnectedToInternet(context));
            LocalBroadcastManager.getInstance(context).sendBroadcast(networkStateIntent);
        }

        private boolean isConnectedToInternet(Context context) {
            try {
                if (context != null) {
                    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                    return networkInfo != null && networkInfo.isConnected();
                }
                return false;
            } catch (Exception e) {
                Log.e(NetworkStateChangeReceiver.class.getName(), e.getMessage());
                return false;
            }
        }
    }



    /**
     * Listener to check incoming call
     */
    private void initPhoneListener() {

        final PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    pauseMedia();
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {

                    isInCall = false;

                    if (isFirstStart == false) {
                        if (Build.VERSION.SDK_INT >= 17.0) {
                            bigNotification = true;
                            largeMediaPlayer = LargeMediaPlayer.getInstance(context);
                        } else {
                            bigNotification = false;
                            smallMediaPlayer = SmallMediaPlayer.getInstance(context);
                        }
                        resumeMedia();
                    }

                    isFirstStart = false;
                }
                super.onCallStateChanged(state, incomingNumber);
            }

        };

        telephoneManger = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephoneManger != null) {
            telephoneManger.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    /**
     * Broadcast receiver to check outgoing call
     */
    private void initOutgoingBroadcastReceiver() {
        OutgoingBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {

                    isInCall = true;

                    if (isInCall == true) {
                        smallMediaPlayer = SmallMediaPlayer.getInstance(context);
                        bigNotification = false;
                        pauseMedia();
                    }

                }
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
        context.registerReceiver(OutgoingBroadcastReceiver, filter);
    }


    /**
     * Function to show media player  notification according to the mobile version
     */
    public synchronized void showMediaPlayerNotification() {

            bigNotification = false;
            smallMediaPlayer = SmallMediaPlayer.getInstance(context);
            smallMediaPlayer.resume();
    }


    public synchronized void cancelMediaPlayerNotification() {

            smallMediaPlayer.dismiss();

    }

}
