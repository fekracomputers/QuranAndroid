package com.nourayn.quran.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fekracomputers.quran.R;
import com.nourayn.quran.Models.TranslationBook;

import java.util.Locale;

/**
 * Adapter class for show translation show
 */
public class TranslationAdapter extends ArrayAdapter<TranslationBook> {
    private Context context ;

    public TranslationAdapter(Context context) {
        super(context, R.layout.row_translate);
        this.context = context ;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TranslationBook translationBook = getItem(position);
        ViewHolder viewHolder ;

        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_translate , null , true) ;
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);

        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //switch between views
        if(translationBook.bookID == -1)
        {
            viewHolder.title.setText(translationBook.bookName);
            viewHolder.split.setVisibility(View.VISIBLE);
            viewHolder.normal.setVisibility(View.GONE);

        }else{

            viewHolder.split.setVisibility(View.GONE);
            viewHolder.normal.setVisibility(View.VISIBLE);
            if(Locale.getDefault().getDisplayLanguage().equals("العربية"))
            {
                viewHolder.translationName.setText(translationBook.bookName) ;
                viewHolder.translationInfo.setText(translationBook.info);
            }else{
                viewHolder.translationName.setText(translationBook.type == 1 ? translationBook.info : translationBook.bookName) ;
                viewHolder.translationInfo.setText(translationBook.type == 1 ? translationBook.bookName : translationBook.info );
            }

            viewHolder.transaltionStatus.setImageResource(translationBook.isDownloaded == true
                    ? R.drawable.ic_close : R.drawable.ic_download);

            if(translationBook.downloading == true)
            {
                viewHolder.transaltionStatus.setVisibility(View.GONE);
                viewHolder.downloading.setVisibility(View.VISIBLE);
            }else{
                viewHolder.transaltionStatus.setVisibility(View.VISIBLE);
                viewHolder.downloading.setVisibility(View.GONE);
            }

        }


        return convertView;
    }

    /**
     * Class to init list view rows view
     */
    private class ViewHolder{
        public LinearLayout split ;
        public RelativeLayout normal ;
        public TextView translationName , translationInfo , title ;
        public ImageView transaltionStatus ;
        private ProgressBar downloading ;

        /**
         * Constructor for listview
         * @param layout layout to get view objects
         */
        public ViewHolder(View layout)
        {
            Typeface type = Typeface.createFromAsset(context.getAssets(),"simple.otf");
            translationName = (TextView) layout.findViewById(R.id.textView5);
            translationInfo = (TextView) layout.findViewById(R.id.textView6);
            title = (TextView) layout.findViewById(R.id.title);
            transaltionStatus = (ImageView) layout.findViewById(R.id.Action);
            split = (LinearLayout) layout.findViewById(R.id.split);
            normal = (RelativeLayout)layout.findViewById(R.id.normal);
            downloading = (ProgressBar)layout.findViewById(R.id.downloading);
            translationName.setTypeface(type);
            translationInfo.setTypeface(type);
        }

    }

}
