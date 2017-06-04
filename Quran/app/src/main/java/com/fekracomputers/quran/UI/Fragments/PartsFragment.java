package com.fekracomputers.quran.UI.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.fekracomputers.quran.Adapter.PartShowAdapter;
import com.fekracomputers.quran.Models.Sora;
import com.fekracomputers.quran.R;
import com.fekracomputers.quran.UI.Activities.MainActivity;
import com.fekracomputers.quran.UI.Activities.QuranPageReadActivity;
import com.fekracomputers.quran.Utilities.AppConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Part fragment class
 */
public class PartsFragment extends Fragment  {

    private RecyclerView partsList;
    private PartShowAdapter adapter;

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

        adapter = new PartShowAdapter(getActivity(), MainActivity.soraListModified);
        partsList = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        partsList.setLayoutManager(mLayoutManager);
        partsList.setItemAnimator(new DefaultItemAnimator());
        partsList.setAdapter(adapter);


    }

}
