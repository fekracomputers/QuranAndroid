package com.nourayn.quran.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nourayn.quran.Models.Aya;
import com.fekracomputers.quran.R;

import java.util.List;
import java.util.Locale;

/**
 * Adapter class for show search results
 */
public class SearchShowAdapter extends ArrayAdapter<Aya> {
    private Context context;
    private String searchText;

    /**
     * Constructor for show adapter
     *
     * @param context application context
     * @param items   List of ayat
     */
    public SearchShowAdapter(Context context, String searchText, List<Aya> items) {
        super(context, R.layout.row_search, items);
        this.context = context;
        this.searchText = searchText;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_search, null, true);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Aya aya = getItem(position);

        //to highlight search text
        int startIndex = aya.text.indexOf(searchText);
        int stopIndex = startIndex + searchText.length();
        Spannable WordToSpan = new SpannableString(aya.text);
        WordToSpan.setSpan(new ForegroundColorSpan(Color.argb(255, 151, 177, 251)), startIndex, stopIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        viewHolder.ayaText.setText(WordToSpan);

        //check app language to set sura name
        if (Locale.getDefault().getDisplayLanguage().equals("العربية")) {
            viewHolder.ayaInfo.setText(context.getResources().getString(R.string.sora)
                    + " " + aya.name + ", " + context.getResources().getString(R.string.aya) + " " + aya.ayaID);
        } else {
            viewHolder.ayaInfo.setText(context.getResources().getString(R.string.sora)
                    + " " + aya.nameEnglish.replace("$$$", "'") + ", " + context.getResources().getString(R.string.aya) + " " + aya.ayaID);
        }

        return convertView;
    }

    /**
     * Model class for search row
     */
    private class ViewHolder {
        private TextView ayaText, ayaInfo;

        /**
         * Constructor fo view holder class
         *
         * @param layout content view
         */
        public ViewHolder(View layout) {
            Typeface type = Typeface.createFromAsset(context.getAssets(), "simple.otf");
            ayaText = (TextView) layout.findViewById(R.id.textView3);
            ayaInfo = (TextView) layout.findViewById(R.id.textView12);
            ayaInfo.setTypeface(type);
            ayaText.setTypeface(type);
        }
    }

}
