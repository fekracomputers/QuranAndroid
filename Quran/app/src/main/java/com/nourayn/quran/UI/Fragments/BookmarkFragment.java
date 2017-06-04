package com.nourayn.quran.UI.Fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.nourayn.quran.Adapter.BookmarksShowAdapter;
import com.nourayn.quran.Database.AppPreference;
import com.nourayn.quran.Database.DatabaseAccess;
import com.nourayn.quran.Models.Page;
import com.fekracomputers.quran.R;
import com.nourayn.quran.Models.Bookmark;
import com.nourayn.quran.UI.Activities.QuranPageReadActivity;
import com.nourayn.quran.Utilities.AppConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Bookmark fragment class
 */
public class BookmarkFragment extends Fragment implements AdapterView.OnItemClickListener {
    private List<Bookmark> bookmarks ;
    private BookmarksShowAdapter adapter ;
    private ListView bookmarkList ;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bookmark , container , false);
        init(rootView);
        return rootView ;
    }

    @Override
    public void onResume() {
        super.onResume();
        new loadBookmarks().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Init views in the fragment
     * @param rootView Fragment view
     */
    private void init(View rootView) {
        bookmarks = new ArrayList<Bookmark>();
        adapter = new BookmarksShowAdapter(getActivity() , bookmarks);
        bookmarkList = (ListView) rootView.findViewById(R.id.listView);
        bookmarkList.setEmptyView(rootView.findViewById(R.id.listView1));
        bookmarkList.setAdapter(adapter);
        bookmarkList.setOnItemClickListener(this);
        new loadBookmarks().execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Bookmark bookmark = adapter.getItem(position);
        Intent QuranPage = new Intent(getContext() , QuranPageReadActivity.class);
        QuranPage.putExtra(AppConstants.General.PAGE_NUMBER , (604-bookmark.page));
        QuranPage.putExtra(AppConstants.General.BOOK_MARK, bookmark.bookmarkID);
        startActivity(QuranPage);
    }

    /**
     * Async task to load bookmarks
     */
    private class loadBookmarks extends AsyncTask<String,String,List<Bookmark>>{

        @Override
        protected List<Bookmark> doInBackground(String... params) {
            return new DatabaseAccess().getAllBookmarks();
        }

        @Override
        protected void onPostExecute(List<Bookmark> bookmarks) {
            int lastPageRead = AppPreference.getLastPageRead();
            Page page = new DatabaseAccess().getPageInfo(604-lastPageRead);

            if(AppPreference.getLastPageRead() != -1) {
                bookmarks.add(0, new Bookmark(-1, 0, getString(R.string.current_page), null));
                bookmarks.add(1, new Bookmark(-2, (604 - lastPageRead), "", page));
                bookmarks.add(2, new Bookmark(-1, 0, getString(R.string.sura_bookmark), null));
            }
            adapter.clear();
            adapter.addAll(bookmarks);
            adapter.notifyDataSetChanged();
        }
    }

}
