package com.bulletcart.videorewards.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bulletcart.videorewards.Activities.ScanActivity;
import com.bulletcart.videorewards.Activities.StoresActivity;
import com.bulletcart.videorewards.Adapters.StoresListAdapter;
import com.bulletcart.videorewards.Adapters.StoresRecyclerAdapter;
import com.bulletcart.videorewards.ApiResult.StoreInfoResult;
import com.bulletcart.videorewards.Global.GlobalVariables;
import com.bulletcart.videorewards.R;
import com.bulletcart.videorewards.Utils.ApiUtil;
import com.bulletcart.videorewards.Utils.Notify;

public class StoresFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match

    TextView emptyText;
    ImageView emptyImage;
    RecyclerView stores;
    StoresListAdapter mAdapter;
    ProgressBar progressBar;
    Context context;

    View view;
    public StoresFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getActivity();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_stores, container, false);
        emptyText = view.findViewById(R.id.empty);
        emptyImage = view.findViewById(R.id.emptyImage);
        progressBar = view.findViewById(R.id.progressBar);
        stores = view.findViewById(R.id.stores);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        stores.setLayoutManager(layoutManager);
        stores.setItemAnimator(new DefaultItemAnimator());

        getStores();

        return view;
    }

    public void getStores() {
        ApiUtil.get_stores(new Notify() {
            @Override
            public void onSuccess(Object object) {
                final StoreInfoResult data = (StoreInfoResult) object;
                GlobalVariables.STORES.clear();
                GlobalVariables.STORES.addAll(data.stores);
                stores.setAdapter(new StoresRecyclerAdapter(context, GlobalVariables.STORES));
                progressBar.setVisibility(View.GONE);
                checkHaveStores();
            }

            @Override
            public void onAbort(Object object) {
                checkHaveStores();
            }

            @Override
            public void onFail() {
                checkHaveStores();
            }
        });
    }

    void checkHaveStores(){

        if(progressBar.getVisibility() == View.VISIBLE){
            emptyText.setVisibility(View.VISIBLE);
            emptyImage.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
