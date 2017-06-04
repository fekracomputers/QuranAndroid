package com.nourayn.quran.UI.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.nourayn.quran.Database.AppPreference;
import com.nourayn.quran.Database.DatabaseAccess;
import com.fekracomputers.quran.R;
import com.nourayn.quran.UI.Activities.QuranPageReadActivity;
import com.nourayn.quran.UI.Custom.HighlightImageView;
import com.nourayn.quran.Utilities.AppConstants;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Quran read fragment
 */
public class QuranPageFragment extends Fragment {
    public static boolean SELECTION = false, isPauseResume;
    public HighlightImageView QuranImageLandscape, QuranImagePortrait;
    private WeakReference<HighlightImageView> quranImageLandscapeWeak;
    private WeakReference<HighlightImageView> quranImagePortraitWeak;
    private ScrollView master;
    private int soraCurrentPage;


    /**
     * Function to create instance of fragment
     *
     * @param sectionNumber Number of fragment selected
     */
    public static QuranPageFragment newInstance(int sectionNumber) {
        QuranPageFragment fragment = new QuranPageFragment();
        Bundle args = new Bundle();
        args.putInt(AppConstants.Highlight.ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * Function to create view of fragment
     *
     * @param inflater           Layout inflater
     * @param container          Parent view
     * @param savedInstanceState Bundle
     * @return Fragment view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_quran_page_read, container, false);
        init(rootView);
        return rootView;
    }

    /**
     * Function to init Quran page fragment
     *
     * @param rootView
     */
    private void init(View rootView) {

        //Portrait image view weak reference
        QuranImagePortrait = (HighlightImageView) rootView.findViewById(R.id.QuranPortrail);
        quranImagePortraitWeak = new WeakReference<>(QuranImagePortrait);
        QuranImagePortrait = null;

        //Landscape image view weak reference
        QuranImageLandscape = (HighlightImageView) rootView.findViewById(R.id.QuranLandScape);
        quranImageLandscapeWeak = new WeakReference<>(QuranImageLandscape);
        QuranImageLandscape = null;

        master = (ScrollView) rootView.findViewById(R.id.landscape);
        soraCurrentPage = (604 - getArguments().getInt(AppConstants.Highlight.ARG_SECTION_NUMBER));

        new  LoadImage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    /**
     * Function fire when fragment created
     */
    @Override
    public void onResume() {
        super.onResume();
        isPauseResume = true;
        drawSavedHighlight();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(ResetImage , new IntentFilter(AppConstants.Highlight.RESET_IMAGE));
    }

    @Override
    public void onPause() {
        super.onPause();
        isPauseResume = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(ResetImage);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(ImageSelection);
    }

    /**
     * BroadcastReceiver object to reset image
     */
    private BroadcastReceiver ResetImage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            boolean reset = intent.getBooleanExtra(AppConstants.Highlight.RESET , false);
            if (reset == true) {
                quranImageLandscapeWeak.get().resetImage();
                quranImagePortraitWeak.get().resetImage();
            }

        }
    };


    /**
     * BroadcastReceiver object to select in aya while audio play
     */
    public BroadcastReceiver ImageSelection = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int pageNumber = intent.getIntExtra(AppConstants.Highlight.PAGE_NUMBER, -1);
            int ayaNumber = intent.getIntExtra(AppConstants.Highlight.VERSE_NUMBER, -1);
            int soraNumber = intent.getIntExtra(AppConstants.Highlight.SORA_NUMBER, -1);


            if (pageNumber != -1
                    && ayaNumber != -1
                    && soraNumber != -1
                    && getArguments().getInt(AppConstants.Highlight.ARG_SECTION_NUMBER) == 604 - pageNumber
                    && soraCurrentPage == QuranPageReadActivity.selectPage) {

                Log.d("Locations", soraCurrentPage + " : " + pageNumber + "---" + ayaNumber + "---" + soraNumber);

                if (QuranPageFragment.this.getResources().getConfiguration().
                        orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    quranImageLandscapeWeak.get().mRects.clear();
                    quranImageLandscapeWeak.get().mRects.addAll(new DatabaseAccess().getAyaRectsMap(pageNumber,
                            soraNumber,
                            ayaNumber));
                    quranImageLandscapeWeak.get().postInvalidate();
                } else {
                    quranImagePortraitWeak.get().mRects.clear();
                    quranImagePortraitWeak.get().mRects.addAll(new DatabaseAccess().getAyaRectsMap(pageNumber,
                            soraNumber,
                            ayaNumber));
                    quranImagePortraitWeak.get().postInvalidate();
                }

            }
        }
    };

    /**
     * Async task for load Quran page image
     */
    private class LoadImage extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... params) {
            File imageFile;

            if (soraCurrentPage > 9 && soraCurrentPage < 99) {
                imageFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/" + getResources().getString(R.string.app_folder_path) + "/quranpages_" +
                        AppPreference.getScreenResolution() + "/images/page0" + soraCurrentPage + ".png");
            } else if (soraCurrentPage > 99) {
                imageFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/" + getResources().getString(R.string.app_folder_path) + "/quranpages_" +
                        AppPreference.getScreenResolution() + "/images/page" + soraCurrentPage + ".png");
            } else {
                imageFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/" + getResources().getString(R.string.app_folder_path) + "/quranpages_" +
                        AppPreference.getScreenResolution() + "/images/page00" + soraCurrentPage + ".png");
            }


            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inDither = true;
            options.inMutable = true;
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            try {

                WeakReference<Bitmap> bitmapWeak = new WeakReference<>(bitmap);
                bitmap = null ;
                //check if the weak reference is equal null
                if (bitmapWeak.get() != null) {
                    //load image according to activity orientation
                    if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        quranImagePortraitWeak.get().setVisibility(View.GONE);
                        master.setVisibility(View.VISIBLE);
                        quranImageLandscapeWeak.get().InternalBitmapSize(bitmapWeak.get().getHeight(), bitmapWeak.get().getWidth());
                        quranImageLandscapeWeak.get().setImageBitmap(Bitmap.createScaledBitmap(bitmapWeak.get(), bitmapWeak.get().getWidth(), bitmapWeak.get().getHeight(), false));
                    } else {
                        quranImagePortraitWeak.get().setVisibility(View.VISIBLE);
                        master.setVisibility(View.GONE);
                        quranImagePortraitWeak.get().InternalBitmapSize(bitmapWeak.get().getHeight(), bitmapWeak.get().getWidth());
                        quranImagePortraitWeak.get().setImageBitmap(bitmapWeak.get());
                    }

                    //register selection broadcast
                    LocalBroadcastManager.getInstance(getActivity()).registerReceiver(ImageSelection, new IntentFilter(AppConstants.Highlight.INTENT_FILTER));
                    drawSavedHighlight();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Function to draw saved highlight
     */
    public void drawSavedHighlight() {
        try {
            //get ayaInfo to highlight
            String[] ayaInfo = AppPreference.getSelectionVerse().split("-");
            if (ayaInfo != null) {

                //split ayaInfo to extract information
                int suraID = Integer.parseInt(ayaInfo[2]);
                int ayaID = Integer.parseInt(ayaInfo[1]);
                int pageNumber = Integer.parseInt(ayaInfo[0]);

                Log.d("draw_image", suraID + "--" + ayaID + "--" + pageNumber);

                Intent highlightAya = new Intent(AppConstants.Highlight.INTENT_FILTER);
                highlightAya.putExtra(AppConstants.Highlight.VERSE_NUMBER, ayaID);
                highlightAya.putExtra(AppConstants.Highlight.SORA_NUMBER, suraID);
                highlightAya.putExtra(AppConstants.Highlight.PAGE_NUMBER, pageNumber);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(highlightAya);
            }
        } catch (Exception e) {
        }

    }


}
