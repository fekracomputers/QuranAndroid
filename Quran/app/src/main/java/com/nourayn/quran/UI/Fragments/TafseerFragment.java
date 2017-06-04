package com.nourayn.quran.UI.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.nourayn.quran.Database.AppPreference;
import com.nourayn.quran.Database.DatabaseAccess;
import com.nourayn.quran.Models.Aya;
import com.nourayn.quran.Models.AyaTafseer;
import com.fekracomputers.quran.R;
import com.nourayn.quran.UI.Activities.TranslationReadActivity;
import com.nourayn.quran.Utilities.Settings;


/**
 * Fragment to show Quran with tafseer
 */
public class TafseerFragment extends Fragment {
    private static final String AYA_POSITION = "aya_position" , BOOK_ID = "book_id";
    private int ayaPosition , bookID ;
    private String ayaTafseer ;
    private TextView number, text, soraName;
    private WebView tafseer;
    private double LastX, FirstX;


    /**
     * Function to take instance from tafseer fragment class
     *
     * @param position Aya position
     * @return Fragment instance
     */
    public static TafseerFragment newInstance(int position , int bookID) {
        TafseerFragment fragment = new TafseerFragment();
        Bundle args = new Bundle();
        args.putInt(AYA_POSITION, position);
        args.putInt(BOOK_ID , bookID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ayaPosition = getArguments().getInt(AYA_POSITION , 0);
            bookID = getArguments().getInt(BOOK_ID , 0);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tafseer, container, false);
        init(rootView);
        return rootView;
    }

    /**
     * Function to init tafseer fragment view
     *
     * @param rootView main View
     */
    private void init(final View rootView) {

        //get aya translation from database
        DatabaseAccess db = new DatabaseAccess();
        Aya aya = db.getAyaFromPosition(ayaPosition);
        AyaTafseer ayaTafseerObject = db.getAyaTafseer(aya.suraID , aya.ayaID ,bookID, aya.text);
        ayaTafseer = ayaTafseerObject.tafseer ;

        //init views of fragment
        soraName = (TextView) rootView.findViewById(R.id.soraName);
        number = (TextView) rootView.findViewById(R.id.ayaguide);
        text = (TextView) rootView.findViewById(R.id.aya);
        tafseer = (WebView) rootView.findViewById(R.id.tafseer);

        //show data of the fragment
        soraName.setText(getString(R.string.sora) + " " + (AppPreference.isArabicMood(getContext()) == true ?  aya.name : aya.nameEnglish));
        number.setText(Settings.ChangeNumbers(getContext() , aya.ayaID+"") );
        text.setText(aya.text);
        tafseer.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        tafseer.setBackgroundColor(Color.TRANSPARENT);
        tafseer.getSettings().setAllowFileAccess(true);

        //progress par while web-view load text
        tafseer.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                rootView.findViewById(R.id.loadTafseer).setVisibility(View.GONE);
            }


        });

        //check if no translation or translated before
        if (ayaTafseer.contains(":")) {
            String[] ayaSplits = ayaTafseer.split(":");
            try {
                Integer.valueOf(ayaSplits[0]);
                ayaTafseer = "تم التفسير سابقا";
            } catch (Exception e) {
            }
        } else if (ayaTafseer.equals("") || ayaTafseer == null) {
            ayaTafseer = "لا يوجد تفسير";
        }

        //Check if text is arabic to text alignment
        if (ayaTafseer.contains("ا") ||
                ayaTafseer.contains("ب") ||
                ayaTafseer.contains("أ") ||
                ayaTafseer.contains("ت") ||
                ayaTafseer.contains("ّ") ||
                ayaTafseer.contains("`"))
            tafseer.loadDataWithBaseURL("file:///android_asset/", String.format("<head> <style>@font-face {font-family: font ;src: url('simple.otf');}div { font-family: font;  word-spacing: 1px;}{ font-family: font; word-spacing: 1px;}</style></head><body align='justify' dir='rtl' style='line-height:1.4em ; font-size:%s'> <div> <span style='color:#3E686A'>%s</span>"+(AppPreference.isAyaAppear()?"<br><br>" : "")+"%s </div> </body>", AppPreference.getTranslationTextSize()== null ? "x-large" : AppPreference.getTranslationTextSize() , AppPreference.isAyaAppear()? aya.text : "" , ayaTafseer), "text/html", "utf8", "");
        else
            tafseer.loadDataWithBaseURL("file:///android_asset/", String.format("<head> <style>@font-face {font-family: font ;src: url('simple.otf');}div { font-family: font;  word-spacing: 1px;}{ font-family: font; word-spacing: 1px;}</style></head><body align='justify' dir='ltr' style='line-height:1.4m ; font-size:%s'><div> <span style='color:#3E686A'>%s</span>"+(AppPreference.isAyaAppear()?"<br><br>" : "")+" %s </div></body>", AppPreference.getTranslationTextSize()== null ? "x-large" : AppPreference.getTranslationTextSize() ,  AppPreference.isAyaAppear()? aya.text : "" , ayaTafseer), "text/html", "utf8", "");

        tafseer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        FirstX = event.getRawX();
                        break;
                    case MotionEvent.ACTION_UP:
                        LastX = event.getRawX();
                        double deltaX = LastX - FirstX;
                        if (Math.abs(deltaX) < 50) {
                            ((TranslationReadActivity) getActivity()).showHideToolBar();
                        }
                        break;
                }

                return false;
            }
        });




    }


}
