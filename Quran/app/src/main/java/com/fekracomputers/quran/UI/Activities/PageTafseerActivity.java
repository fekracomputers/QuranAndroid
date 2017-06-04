package com.fekracomputers.quran.UI.Activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.fekracomputers.quran.Adapter.TafseerShowAdapter;
import com.fekracomputers.quran.Database.DatabaseAccess;
import com.fekracomputers.quran.Models.AyaTafseer;
import com.fekracomputers.quran.Models.Page;
import com.fekracomputers.quran.Models.PageTafseer;
import com.fekracomputers.quran.R;
import com.fekracomputers.quran.Utilities.AppConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity class for page tafseer
 */
public class PageTafseerActivity extends Activity {
    private ListView tafserList;
    private List<AyaTafseer> ayaTafseers;
    private TafseerShowAdapter adapter;
    private PageTafseer pageTafseer;
    private TextView soraInfo;
    int k = 0;
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
        setContentView(R.layout.activity_tafseer);
        init();
    }

    /**
     * Function to Init activity objects
     */
    private void init() {
        Intent comingData = getIntent();
        Page page = new DatabaseAccess().getPageInfo(comingData.getIntExtra(AppConstants.General.PAGE, 1));
        pageTafseer = new DatabaseAccess().getPageTafseer(page.pageNumber, 4);
        ayaTafseers = new ArrayList<>();
        tafserList = (ListView) findViewById(R.id.listView2);
        soraInfo = (TextView) findViewById(R.id.soraInfo);
        adapter = new TafseerShowAdapter(this, ayaTafseers, pageTafseer.soraName, pageTafseer.soraNameEnglish);
        ayaTafseers.addAll(pageTafseer.ayaTafseers);
        soraInfo.setText(getString(R.string.page) + " " + page.pageNumber + ", " + getString(R.string.juza) + " " + page.jozaNumber);
        tafserList.setAdapter(adapter);
    }

}
