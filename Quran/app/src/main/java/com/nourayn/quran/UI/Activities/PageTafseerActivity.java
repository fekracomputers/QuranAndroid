package com.nourayn.quran.UI.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.nourayn.quran.Adapter.TafseerShowAdapter;
import com.nourayn.quran.Database.DatabaseAccess;
import com.nourayn.quran.Models.AyaTafseer;
import com.nourayn.quran.Models.Page;
import com.nourayn.quran.Models.PageTafseer;
import com.fekracomputers.quran.R;
import com.nourayn.quran.Utilities.AppConstants;

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
