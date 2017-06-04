package com.fekracomputers.quran.UI.Activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fekracomputers.quran.Adapter.SearchShowAdapter;
import com.fekracomputers.quran.Database.AppPreference;
import com.fekracomputers.quran.Database.DatabaseAccess;
import com.fekracomputers.quran.Models.Aya;
import com.fekracomputers.quran.R;
import com.fekracomputers.quran.UI.Fragments.QuranPageFragment;
import com.fekracomputers.quran.Utilities.AppConstants;

import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Activity class for show search results
 */
public class SearchActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private SearchShowAdapter adapter;
    private List<Aya> ayas;
    private ListView searchResults;
    private TextView resultsInfo;
    private String searchText;
    int k = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        init();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Function to init views
     */
    private void init() {
           Intent intent = getIntent();
           searchText = intent.getStringExtra(AppConstants.General.SEARCH_TEXT);
           Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
           toolbar.setTitle(getString(R.string.search));
           setSupportActionBar(toolbar);
           getSupportActionBar().setDisplayHomeAsUpEnabled(true);
           getSupportActionBar().setDisplayShowHomeEnabled(true);

           resultsInfo = (TextView) findViewById(R.id.textView13);
           ayas = new ArrayList<Aya>();

           searchResults = (ListView) findViewById(R.id.listView3);
           searchResults.setOnItemClickListener(this);
           searchResults.setEmptyView(findViewById(R.id.progressBar3));
        if(searchText.contains("%")||searchText.contains("%")||searchText.contains("%")||searchText.contains("_")||searchText.contains("@")||searchText.contains("$")||searchText.contains(" % ")||searchText.contains("^")||searchText.contains("&")||searchText.contains("*")||searchText.contains("(")||searchText.contains(")")||searchText.contains("-")||searchText.contains("?")||searchText.contains(">")||searchText.contains("<")||searchText.contains("'")||searchText.contains(":")||searchText.contains(";")||searchText.contains("+")||searchText.contains("=")||searchText.contains("/")||searchText.contains("_")||searchText.contains(".")||searchText.contains(",")||searchText.contains("`")){
            resultsInfo.setText("  0 results for : "+searchText);
                findViewById(R.id.progressBar3).setVisibility(View.GONE);
                searchResults.setEmptyView(findViewById(R.id.empty));
        }else{
            adapter = new SearchShowAdapter(this, searchText, ayas);
           searchResults.setAdapter(adapter);

            new SearchResults().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //search aya result
        Aya aya = adapter.getItem(position);

        //set aya to select in start
        AppPreference.setSelectionVerse(aya.pageNumber+"-"+ aya.ayaID+"-"+ aya.suraID);

        //intent to open read activity
        Intent QuranPage = new Intent(SearchActivity.this, QuranPageReadActivity.class);
        QuranPage.putExtra(AppConstants.General.PAGE_NUMBER, (604 - aya.pageNumber));
        QuranPage.setFlags(FLAG_ACTIVITY_NEW_TASK);
        startActivity(QuranPage);

    }

    /**
     * Async thread class for load database results
     */
    private class SearchResults extends AsyncTask<Void, Void, List<Aya>> {

        @Override
        protected List<Aya> doInBackground(Void... params) {

        return new DatabaseAccess().quranSearch(searchText);

        }

        @Override
        protected void onPostExecute(List<Aya> ayas) {

            if(!searchText.contains("_")||!searchText.contains("@")||!searchText.contains("$")||!searchText.contains("%")||!searchText.contains("^")||!searchText.contains("&")||!searchText.contains("*")||!searchText.contains("(")||!searchText.contains(")")||!searchText.contains("-")||!searchText.contains("?")||!searchText.contains(">")||!searchText.contains("<")||!searchText.contains("'")||!searchText.contains(":")||!searchText.contains(";")||!searchText.contains("!")||!searchText.contains("+")||!searchText.contains("=")||!searchText.contains("/")||!searchText.contains("_")||!searchText.contains(".")||!searchText.contains(",")||!searchText.contains("`")){

                adapter.clear();
                adapter.addAll(ayas);
                adapter.notifyDataSetChanged();
                resultsInfo.setText("  " + adapter.getCount() + " " + getResources().
                        getString(R.string.searchReslt)  + searchText);
                findViewById(R.id.progressBar3).setVisibility(View.GONE);
                if (adapter.getCount() == 0) {
                    searchResults.setEmptyView(findViewById(R.id.empty));
                }
            }else{
                adapter.clear();
                resultsInfo.setText("  0 results for : "+searchText);
                findViewById(R.id.progressBar3).setVisibility(View.GONE);
                searchResults.setEmptyView(findViewById(R.id.empty));
            }

        }
    }

    public void onBackPressed() {
        Intent i=new Intent(SearchActivity.this,HomeActivity.class);
        startActivity(i);
    }

}
