package com.nourayn.quran.Audio.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.fekracomputers.quran.R;
import com.nourayn.quran.UI.Activities.QuranPageReadActivity;
import com.nourayn.quran.Utilities.AppConstants;

/**
 * Class to show big notification Media Player
 */
public class LargeMediaPlayer {
    private static LargeMediaPlayer largeMediaPlayer;
    private static Notification bigMediaPlayer;
    private static NotificationManager notificationManager;
    private RemoteViews notificationBigView;
    private Context context;
    private Intent displayActivity;
    private NotificationCompat.Builder builder;


    /**
     * Private constructor for large media player
     *
     * @param context Application context
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private LargeMediaPlayer(Context context) {

        this.context = context;
        notificationBigView = new RemoteViews(context.getPackageName(),
                R.layout.notification_player_large);

        //to open activity again
        displayActivity = new Intent(context, QuranPageReadActivity.class);
        displayActivity.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, displayActivity, 0);

        //pause button in notification
        notificationBigView.setOnClickPendingIntent(R.id.ib_pause,
                PendingIntent.getBroadcast(context, 1, new Intent(AppConstants.MediaPlayer.PAUSE),
                        PendingIntent.FLAG_UPDATE_CURRENT));

        //previous button in notification
        notificationBigView.setOnClickPendingIntent(R.id.ib_previous,
                PendingIntent.getBroadcast(context, 1, new Intent(AppConstants.MediaPlayer.BACK),
                        PendingIntent.FLAG_UPDATE_CURRENT));

        //next button in notification
        notificationBigView.setOnClickPendingIntent(R.id.ib_next,
                PendingIntent.getBroadcast(context, 1, new Intent(AppConstants.MediaPlayer.FORWARD),
                        PendingIntent.FLAG_UPDATE_CURRENT));

        //play button in notification
        notificationBigView.setOnClickPendingIntent(R.id.ib_play,
                PendingIntent.getBroadcast(context, 1, new Intent(AppConstants.MediaPlayer.PLAY),
                        PendingIntent.FLAG_UPDATE_CURRENT));

        //stop button in notification
        notificationBigView.setOnClickPendingIntent(R.id.ib_stop,
                PendingIntent.getBroadcast(context, 1, new Intent(AppConstants.MediaPlayer.STOP),
                        PendingIntent.FLAG_UPDATE_CURRENT));


        //retrieve open application
        notificationBigView.setOnClickPendingIntent(R.id.im_logo, configPendingIntent);
        notificationBigView.setOnClickPendingIntent(R.id.rl_main, configPendingIntent);

        builder = new NotificationCompat.Builder(context);
        bigMediaPlayer = builder
                .setContent(notificationBigView).setOngoing(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        ? R.drawable.ic_quran_trans : R.drawable.logo)
                .setWhen(0).build();
        bigMediaPlayer.bigContentView = notificationBigView;

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, bigMediaPlayer);

        resume();
    }

    /**
     * static function to single-tone the notification media player
     *
     * @param context Application context
     * @return Object of big media player
     */
    public static LargeMediaPlayer getInstance(Context context) {
        if (largeMediaPlayer == null) {
            synchronized (LargeMediaPlayer.class) {
                largeMediaPlayer = new LargeMediaPlayer(context);
            }
        }
        notificationManager.notify(0, bigMediaPlayer);
        return largeMediaPlayer;
    }

    /**
     * Function display pause view in media player
     */
    public void pause() {
        notificationBigView.setViewVisibility(R.id.linearLayout, View.GONE);
        notificationBigView.setViewVisibility(R.id.linearLayout11, View.VISIBLE);
        notificationManager.notify(0, bigMediaPlayer);
    }

    /**
     * Function display resume view in media player
     */
    public void resume() {
        notificationBigView.setViewVisibility(R.id.linearLayout, View.VISIBLE);
        notificationBigView.setViewVisibility(R.id.linearLayout11, View.GONE);
        notificationManager.notify(0, bigMediaPlayer);
    }

    /**
     * Function to cancel media player notification
     */
    public void dismiss() {
        //dismiss the notification
        notificationManager.cancelAll();
    }

    /**
     * Function to show information in notification bar
     */
    public void showAudioInformation(String information) {
        notificationBigView.setTextViewText(R.id.tv_information, information);
        notificationBigView.setTextViewText(R.id.tv_title, context.getString(R.string.app_name_main));
        notificationManager.notify(0, bigMediaPlayer);
    }

}
