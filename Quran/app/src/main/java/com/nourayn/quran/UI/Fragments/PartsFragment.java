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

import com.nourayn.quran.Adapter.PartShowAdapter;
import com.nourayn.quran.Models.Sora;
import com.fekracomputers.quran.R;
import com.nourayn.quran.UI.Activities.MainActivity;
import com.nourayn.quran.UI.Activities.QuranPageReadActivity;
import com.nourayn.quran.Utilities.AppConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Part fragment class
 */
public class PartsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ListView partsList;
    private PartShowAdapter adapter;
    List<Sora> Soras;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_parts, container, false);
        init(rootView);
        return rootView;
    }

    /**
     * Init views in the fragment
     *
     * @param rootView Fragment view
     */
    private void init(View rootView) {

        Soras = new ArrayList<Sora>();
        adapter = new PartShowAdapter(getActivity(), Soras);
        partsList = (ListView) rootView.findViewById(R.id.listView);
        partsList.setEmptyView(rootView.findViewById(R.id.progressBar3));
        partsList.setAdapter(adapter);
        partsList.setActivated(true);
        partsList.setOnItemClickListener(this);

        adapter.addAll(MainActivity.soraListModified);
        adapter.notifyDataSetChanged();


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Sora sora = adapter.getItem(position);
        Intent QuranPage = new Intent(getContext(), QuranPageReadActivity.class);
        QuranPage.putExtra(AppConstants.General.PAGE_NUMBER, (604 - sora.startPageNumber));
        startActivity(QuranPage);

    }

}
