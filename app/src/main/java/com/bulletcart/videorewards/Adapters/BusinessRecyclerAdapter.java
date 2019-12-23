package com.bulletcart.videorewards.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bulletcart.videorewards.Activities.ScanActivity;
import com.bulletcart.videorewards.Global.GlobalConstants;
import com.bulletcart.videorewards.Global.GlobalFunctions;
import com.bulletcart.videorewards.Global.GlobalVariables;
import com.bulletcart.videorewards.Model.Business;
import com.bulletcart.videorewards.Model.Store;
import com.bulletcart.videorewards.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class BusinessRecyclerAdapter extends RecyclerView.Adapter<BusinessRecyclerAdapter.ViewHolder>{

    private Context context;
    private List<Business> listItem;
    private int m_nType;
    public BusinessRecyclerAdapter(Context context, List<Business> listItem, int nType) {
        this.context = context;
        this.listItem = listItem;
        this.m_nType = nType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_store, parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final Business business = listItem.get(position);

        Glide.with(context)
                .load(GlobalConstants.PRODUCT_BUSINESS_LOGO_URL + business.logo)
                .apply(new RequestOptions().override(80,80))
                .apply(RequestOptions.placeholderOf(R.drawable.ic_place_holder))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .into(holder.iv_logo_wrapper);

        holder.tv_name.setText(business.name);
        holder.tv_business_name.setText(business.name);
        holder.tv_name.setSelected(true);

        holder.SingleItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                GlobalVariables.BUSINESS_TYPE = m_nType;
                final Dialog LocationDlg = new Dialog(context);
                LocationDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
                LocationDlg.setContentView(R.layout.dialog_location_layout);
                LocationDlg.getWindow().setLayout(((GlobalFunctions.getWidth(context) / 100) * 90), ((GlobalFunctions.getHeight(context) / 100) * 80));
                RecyclerView listLocation = LocationDlg.findViewById(R.id.list_location);
                listLocation.setHasFixedSize(true);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
                listLocation.setLayoutManager(layoutManager);
                listLocation.setItemAnimator(new DefaultItemAnimator());
                listLocation.setAdapter(new StoresRecyclerAdapter(context, business.locations));

                View btn_cancel = LocationDlg.findViewById(R.id.btn_cancel);
                btn_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LocationDlg.dismiss();
                    }
                });

                LocationDlg.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return listItem.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_logo_wrapper;
//        CircleImageView iv_logo;
        TextView tv_name;
        TextView tv_business_name;
        public RelativeLayout SingleItem;
        ViewHolder(View itemView) {
            super(itemView);

            iv_logo_wrapper = itemView.findViewById(R.id.shop_logo_blur);
//            iv_logo = itemView.findViewById(R.id.shop_logo);
            tv_name = itemView.findViewById(R.id.shop_name);
            tv_business_name = itemView.findViewById(R.id.business_name);

            SingleItem = itemView.findViewById(R.id.store_item);
        }
    }
}
