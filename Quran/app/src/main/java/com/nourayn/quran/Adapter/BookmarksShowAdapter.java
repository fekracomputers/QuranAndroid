package com.nourayn.quran.Adapter;


import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nourayn.quran.Models.Bookmark;
import com.fekracomputers.quran.R;
import com.nourayn.quran.Utilities.Settings;

import java.util.List;
import java.util.Locale;

/**
 * Adapter class for the bookmarks list view
 */
public class BookmarksShowAdapter extends ArrayAdapter<Bookmark> {

    private Context context;
    private View rootview;

    /**
     * Constructor fo bookmark show
     *
     * @param context Application context
     * @param items   List of bookmarks
     */
    public BookmarksShowAdapter(Context context, List<Bookmark> items) {
        super(context, R.layout.row_bookmark, items);
        this.context = context;
    }

    /**
     * Function to get the view of every row in the list view
     *
     * @param position    Row position number
     * @param convertView view
     * @param parent      Parent view
     * @return return your custom row view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Bookmark bookmark = getItem(position);
        ViewHolder viewHolder;
        ViewHolderSeparator viewHolderSeparator;

        //switch between two views bookmark and separators
        if (bookmark.bookmarkID != -1) {
            LayoutInflater inflate = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = inflate.inflate(R.layout.row_bookmark, null, true);
            viewHolder = new ViewHolder(convertView);
            viewHolder.suraName.setText(Locale.getDefault().getDisplayLanguage().equals("العربية") ?
                    context.getResources().getString(R.string.sora) + " " + bookmark.pageInfo.soraName :
                    context.getResources().getString(R.string.sora) + " " + bookmark.pageInfo.soraNameEnglish.replace("$$$", "'"));
            viewHolder.pageNumber.setText(Settings.ChangeNumbers(context, bookmark.page + ""));
            viewHolder.bookmarkInfo.setText(Settings.ChangeNumbers(context, context.getResources().getString(R.string.page) + " " +
                    bookmark.page + ", " +
                    context.getResources().getString(R.string.juza) + " " +
                    bookmark.pageInfo.jozaNumber));
        } else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.bookmark_splite_row_item, null, true);
            viewHolderSeparator = new ViewHolderSeparator(convertView);
            //data and time where i put bookmark title
            viewHolderSeparator.bookmarkType.setText(bookmark.dateAndTime);
        }

        return convertView;
    }

    /**
     * View holder for bookmark adapter
     */
    private class ViewHolder {
        public TextView suraName, bookmarkInfo, pageNumber;

        public ViewHolder(android.view.View layout) {
            Typeface type = Typeface.createFromAsset(context.getAssets(), "simple.otf");
            suraName = (TextView) layout.findViewById(R.id.textView5);
            bookmarkInfo = (TextView) layout.findViewById(R.id.textView6);
            pageNumber = (TextView) layout.findViewById(R.id.textView7);
            suraName.setTypeface(type);
            bookmarkInfo.setTypeface(type);
            pageNumber.setTypeface(type);
        }
    }

    /**
     * View holder class for separator
     */
    private class ViewHolderSeparator {
        public TextView bookmarkType;

        public ViewHolderSeparator(View view) {
            Typeface type = Typeface.createFromAsset(context.getAssets(), "simple.otf");
            bookmarkType = (TextView) view.findViewById(R.id.tv_separator);
            bookmarkType.setTypeface(type);
        }
    }

}
