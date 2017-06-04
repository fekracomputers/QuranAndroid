package com.nourayn.quran.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fekracomputers.quran.R;
import com.nourayn.quran.Models.AyaTafseer;

import java.util.List;
import java.util.Locale;

/**
 * Adapter class for tafseer show adapter
 */
public class TafseerShowAdapter extends ArrayAdapter<AyaTafseer> {
    private Context context ;
    private String soraName ;
    private String soraNameEnglish ;

    public TafseerShowAdapter(Context context, List<AyaTafseer> items, String soraName, String soraNameEnglish) {
        super(context , R.layout.row_tafseer, items);
        this.context = context ;
        this.soraName = soraName;
        this.soraNameEnglish = soraNameEnglish ;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_tafseer, null , true);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        AyaTafseer ayaTafseer = getItem(position);

        if(ayaTafseer != null)
        {
            if(Locale.getDefault().getDisplayLanguage().equals("العربية"))
            {
                viewHolder.titleAndInfo.setText(context.getResources().getString(R.string.sora)+" "+
                        ""+soraName+"  : "+context.getResources().
                        getString(R.string.aya)+" "+ayaTafseer.ayaID);
            }else{
                viewHolder.titleAndInfo.setText(context.getResources().getString(R.string.sora)+" "+
                        soraNameEnglish+"  : "+context
                        .getResources().getString(R.string.aya)+" "+ayaTafseer.ayaID);
            }

            viewHolder.ayaguide.setText(ayaTafseer.soraID+":"+ayaTafseer.ayaID);
            viewHolder.aya.setText(ayaTafseer.ayaText);
            String myAyaTafseer = ayaTafseer.tafseer;
            if(ayaTafseer.tafseer.contains(":"))
            {
                myAyaTafseer = "تم التفسير سابقا";
            }
            viewHolder.tafseer.loadDataWithBaseURL("",String.format("<body align='justify'> %s </body>",myAyaTafseer),"text/html","utf8","");

        }

        return convertView;
    }

    private class ViewHolder
    {
        private TextView titleAndInfo , aya , ayaguide ;
        private WebView tafseer ;

        public ViewHolder(View layout)
        {
            titleAndInfo = (TextView) layout.findViewById(R.id.titleAndInfo);
            aya = (TextView) layout.findViewById(R.id.aya);
            tafseer = (WebView) layout.findViewById(R.id.tafseer);
            tafseer.setBackgroundColor(Color.TRANSPARENT);
            ayaguide = (TextView) layout.findViewById(R.id.ayaguide);
        }
    }


}
