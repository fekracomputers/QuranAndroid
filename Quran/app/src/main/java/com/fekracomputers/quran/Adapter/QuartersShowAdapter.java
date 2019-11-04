package com.fekracomputers.quran.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fekracomputers.quran.Database.DatabaseAccess;
import com.fekracomputers.quran.Models.Quarter;

import com.fekracomputers.quran.R;
import com.fekracomputers.quran.Utilities.Settingsss;

import java.util.List;
import java.util.Locale;

import com.fekracomputers.quran.UI.Activities.QuranPageReadActivity;
import com.fekracomputers.quran.Utilities.AppConstants;


/**
 * Adapter class for quarters show
 */
public class QuartersShowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //Variables.
    int x = 0;
    private Typeface customFont;
    // objects.
    private Context context;
    //List
    private List<Quarter> items;
    private boolean isTabletDevice;

    public QuartersShowAdapter(Context context, List<Quarter> items) {
        isTabletDevice = isTablet(context);
        this.context = context;
        this.items = items;
        customFont = Typeface.createFromAsset(context.getAssets(), "simple.otf");
        DatabaseAccess databaseAccess = new DatabaseAccess();
        if (items != null && items.size() > 0)
            for (Quarter quarter : items) {
                if (quarter.ayaFirstNumber == 0) {
                    quarter.firstVerseText = databaseAccess.getSoraFirstAya(quarter.soraid);
                }
            }
    }

    @Override
    public int getItemViewType(int position) {
        Quarter quarter = items.get(position);
        return quarter.soraid;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType != -1) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_quarter, parent, false); // inflater.inflate(R.layout.row_quarter, null, true);

            return new ViewHolder(view);

        } else {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sura_separator, parent, false);

            return new ViewHolderSplitter(view);
        }


    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        final Quarter quarter = items.get(position);
        if (getItemViewType(position) != -1) {
            // sora item.
            String soraNameText = Locale.getDefault().getDisplayLanguage().equals("العربية") ? quarter.soraName : quarter.soraNameEnglish.replace("$$$", "'");

            String ayaShortcut[] = quarter.firstVerseText.split(" ");

            String startVerse = "";
            int wordsNumber = (ayaShortcut.length > 4 ? 4 : ayaShortcut.length - 1);
            for (int i = 0; i <= wordsNumber; i++) {
                startVerse += ayaShortcut[i] + " ";
            }
            ViewHolder ayaViewHolder = (ViewHolder) holder;
            ayaViewHolder.page.setText(Settingsss.ChangeNumbers(context, quarter.startPageNumber + ""));
            ayaViewHolder.aya.setText(Settingsss.ChangeNumbers(context, startVerse));

            ayaViewHolder.info.setText(Settingsss.ChangeNumbers(context, context.getResources().getString(R.string.sora) + " " + soraNameText + ", " + context
                    .getResources().getString(R.string.aya) + " " + (quarter.ayaFirstNumber == 0 ? 1 : quarter.ayaFirstNumber)));
            ayaViewHolder.partNumber.setText(Settingsss.ChangeNumbers(context, quarter.counter != 0 ? String.valueOf(quarter.counter) : ""));
            imageNumber(quarter.partNumber, ayaViewHolder.partNumber);


            if (isTabletDevice) {
                ((ViewHolder) holder).aya.setTextSize(31);
                ((ViewHolder) holder).page.setTextSize(25);
                ((ViewHolder) holder).info.setTextSize(25);
            }
        } else {
            // TODO : debug this numbers in arabic lenovooo
            ((ViewHolderSplitter) holder).jozaNumber.setText(Settingsss.ChangeNumbers(context, context.getString(R.string.juza) + " " + quarter.joza));
            ((ViewHolderSplitter) holder).pageNumber.setText(Settingsss.ChangeNumbers(context, quarter.startPageNumber + ""));
            if (isTablet(context)) {
                ((ViewHolderSplitter) holder).jozaNumber.setTextSize(30);
                ((ViewHolderSplitter) holder).pageNumber.setTextSize(20);
            }
        }

        //open quran pages
        final int finalPosition = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (getItemViewType(finalPosition) != -1) {


                } else {

                }

                Intent QuranPage = new Intent(context, QuranPageReadActivity.class);
                QuranPage.putExtra(AppConstants.General.PAGE_NUMBER, (604 - quarter.startPageNumber));
                context.startActivity(QuranPage);
            }
        });
    }

    @Override
    public int getItemCount() {

        return items.size();

    }


    /**
     * View holder to set views to list view
     */
    private class ViewHolder extends RecyclerView.ViewHolder {

        TextView page, aya, info, partNumber;
        RelativeLayout eelativeLayout;

        ViewHolder(View layout) {
            super(layout);
            page = (TextView) layout.findViewById(R.id.textView7);
            aya = (TextView) layout.findViewById(R.id.textView5);
            info = (TextView) layout.findViewById(R.id.textView6);
            eelativeLayout = (RelativeLayout) layout.findViewById(R.id.eelativeLayout);
            partNumber = (TextView) layout.findViewById(R.id.imageView2);
            info.setTypeface(customFont);
            aya.setTypeface(customFont);
            page.setTypeface(customFont);
        }
    }

    /**
     * Adapter view holder for part
     */
    private class ViewHolderSplitter extends RecyclerView.ViewHolder {
        TextView jozaNumber, pageNumber;

        ViewHolderSplitter(android.view.View layout) {
            super(layout);
            jozaNumber = (TextView) layout.findViewById(R.id.textView8);

            pageNumber = (TextView) layout.findViewById(R.id.tv_page_number);
            jozaNumber.setTypeface(customFont);
            pageNumber.setTypeface(customFont);
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

    private static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

}
