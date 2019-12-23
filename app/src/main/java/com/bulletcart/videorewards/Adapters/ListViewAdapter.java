package com.bulletcart.videorewards.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bulletcart.videorewards.Activities.ScanActivity;
import com.bulletcart.videorewards.Global.GlobalConstants;
import com.bulletcart.videorewards.Global.GlobalVariables;
import com.bulletcart.videorewards.Model.ProductInfo;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.bulletcart.videorewards.R;
import java.util.List;


public class ListViewAdapter extends ArrayAdapter<ProductInfo> {

    private Context mContext;
    private ListView mListView;
    private List<ProductInfo> listData;
    private ProductListItemCallback mProductListItemCallback;

    public ListViewAdapter(Context mContext, ListView mListView, List<ProductInfo> listData, ProductListItemCallback productListItemCallback) {
        super(mContext, R.layout.list_item, listData);
        this.mContext = mContext;
        this.mListView = mListView;
        this.listData = listData;
        this.mProductListItemCallback = productListItemCallback;
    }
    public void refreshAdapter(List<ProductInfo> listData){
        this.listData = listData;
        notifyDataSetChanged();
    }


    public View getView(final int position,View view,ViewGroup parent) {

        View v = LayoutInflater.from(mContext).inflate(R.layout.list_item, null);

        ImageView trash=(ImageView) v.findViewById(R.id.trash);
        trash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SwipeLayout)(mListView.getChildAt(position - mListView.getFirstVisiblePosition()))).close(true);
            }
        });

        Button delete=(Button)v.findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 removemy(position);

            }
        });
        ImageView im=(ImageView)v.findViewById(R.id.delete_btn);
        im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((SwipeLayout)(mListView.getChildAt(position - mListView.getFirstVisiblePosition()))).open(true);
            }
        });


        ImageButton ibtn_more = v.findViewById(R.id.btn_more);
        ImageButton ibtn_less = v.findViewById(R.id.btn_less);

        final ProductInfo productInfo = listData.get(position);
        productInfo.amount = GlobalVariables.SELECTED_PRODUCTS.get(position).amount;
        ibtn_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                productInfo.amount++;
                if (mProductListItemCallback != null)
                    mProductListItemCallback.onIncreaseClicked(productInfo);

            }
        });

        ibtn_less.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(productInfo.amount > 0){

                    productInfo.amount--;
                    if (mProductListItemCallback != null)
                        mProductListItemCallback.onDecreaseClicked(productInfo);
                }
            }
        });

        fillValues(position, v);

        final RadioGroup promo_group = (RadioGroup)v.findViewById(R.id.promo_group);
        Log.e("-- promo : ", productInfo.name + productInfo.variation_group.size());
        if( productInfo.variation_group.size() > 0) {
            promo_group.setVisibility(View.VISIBLE);
            RadioButton btn_default_sell = v.findViewById(R.id.btn_radio_default);
            btn_default_sell.setChecked(true);
            btn_default_sell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    productInfo.m_bEnableGroup = false;
                    if (mProductListItemCallback != null)
                        mProductListItemCallback.onPromoChanged();
                }
            });

            for (int i = 0; i < productInfo.variation_group.size(); i++) {
                RadioButton btn_group = new RadioButton(mContext);
                btn_group.setText(productInfo.variation_group.get(i).name);
                btn_group.setId(i);
                promo_group.addView(btn_group);
                if( productInfo.m_bEnableGroup && productInfo.group_price_id == productInfo.variation_group.get(i).id )
                    btn_group.setChecked(true);
                else
                    btn_group.setChecked(false);
                btn_group.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int nId = view.getId();
                        productInfo.m_bEnableGroup = true;
                        float business_tax = Float.parseFloat(GlobalVariables.BUSINESS_TAX) / 100;
                        productInfo.group_price = productInfo.variation_group.get(nId).price_inc_tax / ( 1 + business_tax);
                        productInfo.group_price_id = productInfo.variation_group.get(nId).id;
                        productInfo.strGroup = productInfo.variation_group.get(nId).name;
                        if (mProductListItemCallback != null)
                            mProductListItemCallback.onPromoChanged();
                    }
                });
            }

        }
        return v;
    }

    public void fillValues(int position, View convertView) {
        TextView t = convertView.findViewById(R.id.position);
        t.setVisibility(View.INVISIBLE);
//        t.setText((position + 1) + mContext.getString(R.string.comma));

        TextView txtTitle = convertView.findViewById(R.id.productName);
        ImageView imageView = convertView.findViewById(R.id.product_image);
        TextView rate = convertView.findViewById(R.id.rate);
        TextView amount = convertView.findViewById(R.id.item_amount);

        ProductInfo productInfo = getItem(position);
        txtTitle.setText(productInfo.name);
        txtTitle.setSelected(true);

        Glide.with(mContext)
                .load(GlobalConstants.PRODUCT_IMAGE_URL + productInfo.image)
//                .apply(new RequestOptions().override(75,75))
//                .apply(RequestOptions.placeholderOf(R.drawable.ic_place_holder))
//                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
//                .apply(RequestOptions.skipMemoryCacheOf(true))
                .into(imageView);
        rate.setText(String.format("%s%s", GlobalVariables.BUSINESS_CURRENCY, productInfo.getUnitPrice()));
        amount.setText(String.valueOf(productInfo.amount));
    }

    @Override
    public int getCount() {

        return listData.size();
    }

    public List<ProductInfo> getData(){

        return listData;
    }

    public ProductInfo getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void removemy(int pos) {
        listData.remove(pos);
        GlobalVariables.SELECTED_PRODUCTS = listData;
        ((SwipeLayout)(mListView.getChildAt(pos - mListView.getFirstVisiblePosition()))).close(true);
        notifyDataSetChanged();
        ((ScanActivity)mContext).listVisibiltiy();
    }
    public void insertData(ProductInfo item, int pos) {
        listData.add(pos,item);
        GlobalVariables.SELECTED_PRODUCTS = listData;
        notifyDataSetChanged();
    }


    public interface ProductListItemCallback
    {
        void onIncreaseClicked(ProductInfo productInfo);
        void onDecreaseClicked(ProductInfo productInfo);
        void onPromoChanged();
    }
}
