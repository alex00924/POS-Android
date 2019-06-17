package com.bulletcart.videorewards.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bulletcart.videorewards.Activities.ScanActivity;
import com.bulletcart.videorewards.Global.GlobalConstants;
import com.bulletcart.videorewards.Global.GlobalVariables;
import com.bulletcart.videorewards.Model.Store;
import com.bulletcart.videorewards.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;


import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoresRecyclerAdapter extends RecyclerView.Adapter<StoresRecyclerAdapter.ViewHolder>{

    private Context context;
    private List<Store> listItem;

    public StoresRecyclerAdapter(Context context, List<Store> listItem) {
        this.context = context;
        this.listItem = listItem;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_store, parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final Store store = listItem.get(position);

        Glide.with(context)
                .load(GlobalConstants.PRODUCT_BUSINESS_LOGO_URL + store.logo)
                .apply(new RequestOptions().override(80,80))
                .apply(RequestOptions.placeholderOf(R.drawable.ic_place_holder))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .into(holder.iv_logo_wrapper);

        holder.tv_name.setText(store.name);
        holder.tv_business_name.setText(store.business_name);
        holder.tv_name.setSelected(true);

        holder.SingleItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GlobalVariables.BUSINESS_ID = store.getBusiness_id();
                GlobalVariables.LOCATION_ID = store.getId();
                GlobalVariables.SHOP_NAME = store.getName();
                GlobalVariables.BUSINESS_CURRENCY = store.getCurrency();
                GlobalVariables.BUSINESS_CURRENCY_STRING = store.getCurrency_string();
                GlobalVariables.BUSINESS_MERCHANT_CURRENCY = store.merchant_currency;
                GlobalVariables.BUSINESS_TAX_TYPE = store.getTax_type();
                GlobalVariables.BUSINESS_TAX = store.getTax();
                GlobalVariables.BUISINESS_DEFAULT_DISCOUNT = store.default_sales_discount;// / 100;
                GlobalVariables.BUSINESS_DEFAULT_PRICE_GROUPS = store.selling_group;
                Intent intent = new Intent(context, ScanActivity.class);
                context.startActivity(intent);

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
