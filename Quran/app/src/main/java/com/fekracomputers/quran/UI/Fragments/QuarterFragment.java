package com.fekracomputers.quran.UI.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.fekracomputers.quran.Adapter.QuartersShowAdapter;
import com.fekracomputers.quran.Models.Quarter;
import com.fekracomputers.quran.R;
import com.fekracomputers.quran.UI.Activities.MainActivity;
import com.fekracomputers.quran.UI.Activities.QuranPageReadActivity;
import com.fekracomputers.quran.Utilities.AppConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Quarter fragment class
 */
public class QuarterFragment extends Fragment  {

    private RecyclerView quarterList;
    //private List<Quarter> quarters;
    private QuartersShowAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quarters, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    /**
     * Init view in Fragment
     *
     * @param rootView Fragment view
     */
    private void init(View rootView) {

        adapter = new QuartersShowAdapter(getActivity(), MainActivity.quarterListModified);
        quarterList = (RecyclerView) rootView.findViewById(R.id.recycler_view1);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        quarterList.setLayoutManager(mLayoutManager);
        quarterList.setItemAnimator(new DefaultItemAnimator());
        quarterList.setAdapter(adapter);

    }


}
