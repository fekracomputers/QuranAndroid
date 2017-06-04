package com.nourayn.quran.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nourayn.quran.Models.Sora;
import com.fekracomputers.quran.R;
import com.nourayn.quran.Utilities.Settings;

import java.util.List;
import java.util.Locale;

/**
 * Adapter class for part show list view
 */
public class PartShowAdapter extends ArrayAdapter<Sora> {
    private Context context;

    /**
     * Constructor for part show adapter
     *
     * @param context Application context
     * @param items   list of sora
     */
    public PartShowAdapter(Context context, List<Sora> items) {
        super(context, R.layout.row_surah, items);
        this.context = context;
    }

    /**
     * Function to get the view of row in the list view
     *
     * @param position    Row position number
     * @param convertView view
     * @param parent      Parent view
     * @return return your custom row view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        ViewHolderSplitter viewHolderSplitter;
        Sora sora = getItem(position);

        if (sora.ayahCount == -1) {
            LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflate.inflate(R.layout.row_sura_separator, null, true);
            viewHolderSplitter = new ViewHolderSplitter(convertView);
            convertView.setTag(viewHolderSplitter);
            viewHolderSplitter.jozaNumber.setText(Settings.ChangeNumbers(context, context.getString(R.string.juza) + " " + sora.jozaNumber));
            viewHolderSplitter.pageNumber.setText(Settings.ChangeNumbers(context , sora.startPageNumber+""));
        } else {
            LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflate.inflate(R.layout.row_surah, null, true);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
            String soraNameText = Locale.getDefault().getDisplayLanguage().equals("العربية") ? sora.name : sora.name_english.replace("$$$", "'");
            viewHolder.number.setText(Settings.ChangeNumbers(context , sora.getSoraTag()));
            viewHolder.soraName.setText(Settings.ChangeNumbers(context, context.getResources().getString(R.string.sora) + " " + soraNameText));
            viewHolder.ayaCount.setText(Settings.ChangeNumbers(context, (sora.places == 1 ? context.getResources().getString(R.string.type1) : context.getResources().getString(R.string.type2)) + " - " + (sora.startPageNumber == 1 ? 7 : sora.ayahCount) + " " + context.getResources().getString(R.string.ayat)));
            viewHolder.pageNumbers.setText(Settings.ChangeNumbers(context, sora.startPageNumber + ""));
        }

        return convertView;
    }

    /**
     * Adapter view holder for Sura
     */
    private class ViewHolder {

        public TextView soraName, ayaCount, pageNumbers, number;

        public ViewHolder(android.view.View layout) {
            Typeface type = Typeface.createFromAsset(context.getAssets(), "simple.otf");
            soraName = (TextView) layout.findViewById(R.id.textView5);
            ayaCount = (TextView) layout.findViewById(R.id.textView6);
            pageNumbers = (TextView) layout.findViewById(R.id.textView7);
            number = (TextView) layout.findViewById(R.id.textView4);
            soraName.setTypeface(type);
            ayaCount.setTypeface(type);
            pageNumbers.setTypeface(type);

        }
    }

    /**
     * Adapter view holder for part
     */
    private class ViewHolderSplitter {
        public TextView jozaNumber , pageNumber;

        public ViewHolderSplitter(android.view.View layout) {
            Typeface type = Typeface.createFromAsset(context.getAssets(), "simple.otf");
            jozaNumber = (TextView) layout.findViewById(R.id.textView8);
            pageNumber = (TextView) layout.findViewById(R.id.tv_page_number);
            jozaNumber.setTypeface(type);
            pageNumber.setTypeface(type);
        }
    }

}
