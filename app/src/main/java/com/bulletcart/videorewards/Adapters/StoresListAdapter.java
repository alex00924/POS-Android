package com.bulletcart.videorewards.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bulletcart.videorewards.Global.GlobalConstants;
import com.bulletcart.videorewards.Model.Store;
import com.bulletcart.videorewards.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoresListAdapter extends BaseAdapter {
    private Context context;
    private List<Store> mData;
    private LayoutInflater mInflater;

    public StoresListAdapter(Context context, List<Store> mListData) {
        super();
        this.context = context;
        this.mData = mListData;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return  mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        if(row == null) {
            row = mInflater.inflate(R.layout.list_item_store, parent, false);
        }

        Store store = mData.get(position);

        final ImageView iv_logo_wrapper = row.findViewById(R.id.shop_logo_blur);

        Glide.with(context)
                .load(GlobalConstants.PRODUCT_BUSINESS_LOGO_URL + store.logo)
                .apply(new RequestOptions().override(80,80))
                .apply(RequestOptions.placeholderOf(R.drawable.ic_place_holder))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .into(iv_logo_wrapper);

        TextView tv_name = row.findViewById(R.id.shop_name);
        TextView tv_business_name = row.findViewById(R.id.business_name);

        tv_name.setText(store.name);
        tv_business_name.setText(store.business_name);
        tv_name.setSelected(true);
        return row;
    }
}
