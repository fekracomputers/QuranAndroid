package com.nourayn.quran.UI.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.nourayn.quran.Adapter.QuartersShowAdapter;
import com.nourayn.quran.Models.Quarter;
import com.fekracomputers.quran.R;
import com.nourayn.quran.UI.Activities.MainActivity;
import com.nourayn.quran.UI.Activities.QuranPageReadActivity;
import com.nourayn.quran.Utilities.AppConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Quarter fragment class
 */
public class QuarterFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ListView quarterList;
    private List<Quarter> quarters;
    private QuartersShowAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_quarters, container, false);
        init(rootView);
        return rootView;

    }

    /**
     * Init view in Fragment
     *
     * @param rootView Fragment view
     */
    private void init(View rootView) {

        quarters = new ArrayList<Quarter>();
        adapter = new QuartersShowAdapter(getActivity(), quarters);
        quarterList = (ListView) rootView.findViewById(R.id.listView);
        quarterList.setEmptyView(rootView.findViewById(R.id.progressBar));
        quarterList.setAdapter(adapter);
        quarterList.setOnItemClickListener(this);

        adapter.addAll(MainActivity.quarterListModified);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent QuranPage = new Intent(getContext(), QuranPageReadActivity.class);
        Quarter qurater = adapter.getItem(position);
        QuranPage.putExtra(AppConstants.General.PAGE_NUMBER, (604 - qurater.startPageNumber));
        startActivity(QuranPage);
    }

}
