package com.nourayn.quran.UI.Popups;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.nourayn.quran.Database.DatabaseAccess;
import com.nourayn.quran.Models.Sora;
import com.fekracomputers.quran.R;
import com.nourayn.quran.UI.Activities.QuranPageReadActivity;
import com.nourayn.quran.Utilities.AppConstants;
import com.nourayn.quran.Utilities.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Class for the pop-up window
 */
public class JumpToPopup {
    private Context context;
    private Dialog jumpDialog;
    private Spinner suraNames, verses;
    private EditText page;
    private Button ok;
    private int verseNumber, suraNumber, pageNumber = 2;
    ;

    /**
     * Public constructor
     *
     * @param context Application context
     */
    public JumpToPopup(Context context) {
        this.context = context;
        jumpDialog = new Dialog(context);
        jumpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        jumpDialog.setContentView(R.layout.jumb_to_popup);
        initDialogComponents();
        jumpDialog.show();
    }


    /**
     * Function to init dialog components
     */
    public void initDialogComponents() {
        suraNames = (Spinner) jumpDialog.findViewById(R.id.suras);
        verses = (Spinner) jumpDialog.findViewById(R.id.verses);
        page = (EditText) jumpDialog.findViewById(R.id.page);
        ok = (Button) jumpDialog.findViewById(R.id.ok);

        final List<Sora> soraList = new DatabaseAccess().getAllSora();
        List<String> sorasShow = new ArrayList<>();
        int count = 0;
        for (Sora soraItem : soraList) {
            sorasShow.add(((++count) + " - " + (Locale.getDefault().getDisplayLanguage().equals("العربية") ? soraItem.name : soraItem.name_english).replace("$$$", "'")));
        }
        final String[] show = sorasShow.toArray(new String[sorasShow.size()]);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                R.layout.spinner_layout_larg, show);
        suraNames.setAdapter(adapter);

        suraNames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                suraNumber = position + 1;
                Sora sora = soraList.get(position);
                int versesNumber = suraNumber == 1 ? sora.ayahCount + 1 : sora.ayahCount;
                String[] numbers = new String[versesNumber];
                for (int i = 1; i <= versesNumber; i++) {
                    numbers[i - 1] = Settings.ChangeNumbers(context, i + "");
                }
                final ArrayAdapter<String> verseAdapter = new ArrayAdapter<String>(context,
                        R.layout.spinner_layout_larg, numbers);
                verses.setAdapter(verseAdapter);
                page.setHint((sora.startPageNumber - 1) + "");

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        verses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                verseNumber = position + 1;
                pageNumber = new DatabaseAccess().getAyaPage(suraNumber, verseNumber);
                page.setHint((pageNumber) + "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (page.getText().toString().trim().equals("") || page.getText() == null) {
                    context.startActivity(new Intent(context, QuranPageReadActivity.class)
                            .putExtra(AppConstants.General.PAGE_NUMBER, (604 - pageNumber)));
                    jumpDialog.dismiss();
                } else {
                    //validate the page you enter
                    if (Integer.parseInt(page.getText().toString().trim()) > 604) {
                        Toast.makeText(context, Settings.ChangeNumbers(context, page.getText().toString()) + " " + context.getString(R.string.error_page), Toast.LENGTH_SHORT).show();
                    } else {
                        context.startActivity(new Intent(context, QuranPageReadActivity.class)
                                .putExtra(AppConstants.General.PAGE_NUMBER, (604 - Integer.parseInt(page.getText().toString().trim()))));
                        jumpDialog.dismiss();
                    }
                }

            }
        });

    }

}
