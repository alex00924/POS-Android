package com.bulletcart.videorewards.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bulletcart.videorewards.Global.GlobalConstants;
import com.bulletcart.videorewards.Global.GlobalFunctions;
import com.bulletcart.videorewards.Global.GlobalVariables;
import com.bulletcart.videorewards.Model.ProductInfo;
import com.bulletcart.videorewards.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.w3c.dom.Text;

import java.util.List;

public class CashCheckRecyclerAdapter extends RecyclerView.Adapter<CashCheckRecyclerAdapter.MyViewHolder> {
    private Context context;
    private List<ProductInfo> mData;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View row;
        public MyViewHolder(View v) {
            super(v);
            row = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CashCheckRecyclerAdapter(Context context, List<ProductInfo> mData) {
        this.mData = mData;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public CashCheckRecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view

        View row;
        if(viewType == 0) {
            row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_cash_checkout_header, parent, false);
        } else {
            row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_cashcheck, parent, false);
        }

        return new MyViewHolder(row);
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if (position == 0) {
            TextView tv_total_price = holder.row.findViewById(R.id.total_check_price);
            TextView tv_added_points = holder.row.findViewById(R.id.added_points);
            tv_total_price.setText(String.format("%s%s%.2f", GlobalVariables.BUSINESS_CURRENCY, " ", GlobalVariables.TOTAL_PRICE_TO_CHECK));
//            tv_added_points.setText(String.format("- %d point(s)", GlobalVariables.USED_POINTS_TO_CHECK));
            tv_added_points.setText(context.getString(R.string.redeemed_points, GlobalVariables.USED_POINTS_TO_CHECK, GlobalVariables.CURRENCY_OF_USED_POINTS_TO_CHECK, GlobalVariables.BUSINESS_CURRENCY));
            if(GlobalVariables.BUSINESS_IS_DELYVERY) {
                holder.row.findViewById(R.id.txt_delivery).setVisibility(View.VISIBLE);
                ((TextView) holder.row.findViewById(R.id.txt_delivery)).setText( context.getString(R.string.invoice_delivery_price) + ": " + GlobalFunctions.getDeliveryPrice() +  " " + GlobalVariables.BUSINESS_CURRENCY + ",  " +
                        context.getString(R.string.invoice_delivery_price_tax) + ": " + GlobalFunctions.getDeliveryTax() + " " + GlobalVariables.BUSINESS_CURRENCY);
            }
            else {
                holder.row.findViewById(R.id.txt_delivery).setVisibility(View.GONE);
            }

        } else {
            ProductInfo productInfo;
            productInfo = mData.get(position - 1);
            ImageView iv_image = holder.row.findViewById(R.id.product_image);
            TextView tv_name = holder.row.findViewById(R.id.product_name);
            TextView tv_quantity = holder.row.findViewById(R.id.product_quantity);
            TextView tv_price = holder.row.findViewById(R.id.product_unit_price);
            TextView total_price = holder.row.findViewById(R.id.total_check_price);
            TextView pp_exc_tax = holder.row.findViewById(R.id.pp_exc_tax);
            TextView pp_tax = holder.row.findViewById(R.id.pp_tax);
            TextView pp_discount = holder.row.findViewById(R.id.pp_discount);
            TextView promDetail = holder.row.findViewById(R.id.txt_promo);

            Glide.with(context)
                    .load(GlobalConstants.PRODUCT_IMAGE_URL + productInfo.image)
                    .apply(new RequestOptions().override(75,75))
                    .apply(RequestOptions.placeholderOf(R.drawable.ic_place_holder))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .apply(RequestOptions.skipMemoryCacheOf(true))
                    .into(iv_image);
            tv_name.setText(productInfo.name);
            tv_quantity.setText(context.getString(R.string.quantity, productInfo.amount));
            float total = GlobalFunctions.roundToDecimal(productInfo.amount * Float.parseFloat(productInfo.getUnitPrice()), 2);
            total_price.setText(context.getString(R.string.total_check_price, String.valueOf(total), GlobalVariables.BUSINESS_CURRENCY));
            pp_exc_tax.setText(context.getString(R.string.dpp_inc_tax, productInfo.getDefault_sell_price(), GlobalVariables.BUSINESS_CURRENCY));
            String tax_type = "(" + GlobalVariables.BUSINESS_TAX_TYPE.toUpperCase() + ": " + GlobalVariables.BUSINESS_TAX + "%)";
            pp_tax.setText(context.getString(R.string.dpp_tax, productInfo.getTax(), GlobalVariables.BUSINESS_CURRENCY, tax_type));
            pp_discount.setText(context.getString(R.string.dpp_discount) + productInfo.getDiscount() + GlobalVariables.BUSINESS_CURRENCY + "(" + GlobalVariables.BUISINESS_DEFAULT_DISCOUNT * 100 + "%)");
            tv_price.setText(context.getString(R.string.unit_price, GlobalVariables.BUSINESS_CURRENCY, productInfo.getUnitPrice()));

            if( productInfo.m_bEnableGroup )
            {
                promDetail.setText(context.getString(R.string.promo_detail) + productInfo.strGroup);
                promDetail.setVisibility(View.VISIBLE);
            }
            else
                promDetail.setVisibility(View.GONE);
        }
//        holder.mTextView.setText(mDataset[position]);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mData.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return position;
    }
}