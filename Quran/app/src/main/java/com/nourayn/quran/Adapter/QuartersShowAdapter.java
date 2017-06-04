package com.nourayn.quran.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nourayn.quran.Models.Quarter;
import com.fekracomputers.quran.R;
import com.nourayn.quran.Utilities.Settings;

import java.util.List;
import java.util.Locale;


/**
 * Adapter class for quarters show
 */
public class QuartersShowAdapter extends ArrayAdapter<Quarter> {

    private Context context;

    public QuartersShowAdapter(Context context, List<Quarter> items) {
        super(context, R.layout.row_quarter, items);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        ViewHolderSplitter viewHolderSplitter;
        Quarter quarter = getItem(position);

        if (quarter.soraid != -1) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_quarter, null, true);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
            String soraNameText = Locale.getDefault().getDisplayLanguage().equals("العربية") ? quarter.soraName : quarter.soraNameEnglish.replace("$$$", "'");
            String ayaShortcut[] = quarter.firstVerseText.split(" ");
            String startVerse = "";
            int wordsNumber = (ayaShortcut.length > 4 ? 4 : ayaShortcut.length - 1);
            for (int i = 0; i <= wordsNumber; i++) startVerse += ayaShortcut[i] + " ";
            viewHolder.page.setText(Settings.ChangeNumbers(context, quarter.startPageNumber + ""));
            viewHolder.aya.setText(Settings.ChangeNumbers(context, startVerse));
            viewHolder.info.setText(Settings.ChangeNumbers(context, context.getResources().getString(R.string.sora) + " " + soraNameText + ", " + context
                    .getResources().getString(R.string.aya) + " " + (quarter.ayaFirstNumber == 0 ? 1 : quarter.ayaFirstNumber) ));
            viewHolder.partNumber.setText(Settings.ChangeNumbers(context, quarter.counter != 0 ? String.valueOf(quarter.counter) : ""));
            imageNumber(quarter.partNumber, viewHolder.partNumber);
        } else {
            LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflate.inflate(R.layout.row_sura_separator, null, true);
            viewHolderSplitter = new ViewHolderSplitter(convertView);
            convertView.setTag(viewHolderSplitter);
            viewHolderSplitter.jozaNumber.setText(Settings.ChangeNumbers(context, context.getString(R.string.juza) + " " + quarter.joza));
            viewHolderSplitter.pageNumber.setText(Settings.ChangeNumbers(context , quarter.startPageNumber+""));
        }


        return convertView;
    }


    /**
     * View holder to set views to list view
     */
    private class ViewHolder {

        public TextView page, aya, info, partNumber;

        public ViewHolder(View layout) {
            Typeface type = Typeface.createFromAsset(context.getAssets(), "simple.otf");
            page = (TextView) layout.findViewById(R.id.textView7);
            aya = (TextView) layout.findViewById(R.id.textView5);
            info = (TextView) layout.findViewById(R.id.textView6);
            partNumber = (TextView) layout.findViewById(R.id.imageView2);
            info.setTypeface(type);
            aya.setTypeface(type);
            page.setTypeface(type);
        }
    }

    /**
     * Adapter view holder for part
     */
    private class ViewHolderSplitter {
        public TextView jozaNumber, pageNumber;

        public ViewHolderSplitter(android.view.View layout) {
            Typeface type = Typeface.createFromAsset(context.getAssets(), "simple.otf");
            jozaNumber = (TextView) layout.findViewById(R.id.textView8);
            pageNumber = (TextView) layout.findViewById(R.id.tv_page_number);
            jozaNumber.setTypeface(type);
            pageNumber.setTypeface(type);
        }
    }

    /**
     * Function to set image
     *
     * @param number     number of part
     * @param partNumber Text view to set background image
     */
    private void imageNumber(int number, TextView partNumber) {
        switch (number) {
            case 1:
                partNumber.setBackgroundResource(R.drawable.qur4);
                break;
            case 2:
                partNumber.setBackgroundResource(R.drawable.qur3);
                break;
            case 3:
                partNumber.setBackgroundResource(R.drawable.qur2);
                break;
            case 4:
                partNumber.setBackgroundResource(R.drawable.qur1);
                break;
        }
    }

}
