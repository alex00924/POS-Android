package com.bulletcart.videorewards.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import com.bulletcart.videorewards.Adapters.BusinessRecyclerAdapter;
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

    private boolean isUserScrolling = false;
    private boolean isListGoingUp = true;
    private boolean isLoadingStores = false;
    private int m_nType = 0; // 0: store, 1: restaurant
    private int m_nCnt = 0;
    View view;
    public StoresFragment() {

    }

    public void setType(int nType)
    {
        m_nType = nType;
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
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        stores.setLayoutManager(layoutManager);
        stores.setItemAnimator(new DefaultItemAnimator());

        stores.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //detect is the topmost item visible and is user scrolling? if true then only execute
                if(newState ==  RecyclerView.SCROLL_STATE_DRAGGING){
                    isUserScrolling = true;
                    if(isListGoingUp){
                        //my recycler view is actually inverted so I have to write this condition instead
                        if(((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition() + 1 == m_nCnt){
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(isListGoingUp) {
                                        if (((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition() + 1 == m_nCnt) {
                                            getStores();
                                        }
                                    }
                                }
                            },50);
                            //waiting for 50ms because when scrolling down from top, the variable isListGoingUp is still true until the onScrolled method is executed
                        }
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(isUserScrolling){
                    if(dy > 0){
                        //means user finger is moving up but the list is going down
                        isListGoingUp = false;
                    }
                    else{
                        //means user finger is moving down but the list is going up
                        isListGoingUp = true;
                    }
                }
            }
        });
        getStores();

        return view;
    }

    public void getStores() {
        progressBar.setVisibility(View.VISIBLE);
        stores.setVisibility(View.GONE);
        if(isLoadingStores)
            return;
        isLoadingStores = true;
        ApiUtil.get_stores(m_nType, new Notify() {
            @Override
            public void onSuccess(Object object) {
                final StoreInfoResult data = (StoreInfoResult) object;
                if(m_nType == 0)
                {
                    GlobalVariables.BUSINESSES_STORES.clear();
                    GlobalVariables.BUSINESSES_STORES.addAll(data.stores);
                    stores.setAdapter(new BusinessRecyclerAdapter(context, GlobalVariables.BUSINESSES_STORES, m_nType));
                    m_nCnt = GlobalVariables.BUSINESSES_STORES.size();
                }
                else
                {
                    GlobalVariables.BUSINESSES_RESTAURANTS.clear();
                    GlobalVariables.BUSINESSES_RESTAURANTS.addAll(data.stores);
                    stores.setAdapter(new BusinessRecyclerAdapter(context, GlobalVariables.BUSINESSES_RESTAURANTS, m_nType));
                    m_nCnt = GlobalVariables.BUSINESSES_RESTAURANTS.size();
                }
                progressBar.setVisibility(View.GONE);
                stores.setVisibility(View.VISIBLE);
                checkHaveStores();
                isLoadingStores = false;
            }

            @Override
            public void onAbort(Object object) {
                checkHaveStores();
                stores.setVisibility(View.VISIBLE);
                isLoadingStores = false;
                m_nCnt = 0;
            }

            @Override
            public void onFail() {
                checkHaveStores();
                stores.setVisibility(View.VISIBLE);
                isLoadingStores = false;
                m_nCnt = 0;
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
