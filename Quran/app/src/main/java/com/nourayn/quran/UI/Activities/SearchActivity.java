package com.nourayn.quran.UI.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.fekracomputers.quran.R;
import com.nourayn.quran.Adapter.SearchShowAdapter;
import com.nourayn.quran.Database.AppPreference;
import com.nourayn.quran.Database.DatabaseAccess;
import com.nourayn.quran.Models.Aya;
import com.nourayn.quran.Utilities.AppConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity class for show search results
 */
public class SearchActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private SearchShowAdapter adapter;
    private List<Aya> ayas;
    private ListView searchResults;
    private TextView resultsInfo;
    private String searchText;


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
        adapter = new SearchShowAdapter(this, searchText, ayas);
        searchResults = (ListView) findViewById(R.id.listView3);
        searchResults.setOnItemClickListener(this);
        searchResults.setEmptyView(findViewById(R.id.progressBar3));
        searchResults.setAdapter(adapter);
        new SearchResults().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
            adapter.clear();
            adapter.addAll(ayas);
            adapter.notifyDataSetChanged();
            resultsInfo.setText("  " + adapter.getCount() + " " + getResources().
                    getString(R.string.searchReslt) + " " + searchText);
            findViewById(R.id.progressBar3).setVisibility(View.GONE);
            if (adapter.getCount() == 0) {
                searchResults.setEmptyView(findViewById(R.id.empty));
            }
        }
    }


}
