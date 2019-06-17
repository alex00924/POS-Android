package com.bulletcart.videorewards.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

import java.util.List;

public class TransactionProductsListAdapter extends BaseAdapter {
    private Context context;
    private List<ProductInfo> mData;
    private LayoutInflater mInflater;

    public TransactionProductsListAdapter(Context context, List<ProductInfo> mData) {
        super();
        this.context = context;
        this.mData = mData;
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
            row = mInflater.inflate(R.layout.list_item_invoice, parent, false);
        }

        ProductInfo product = mData.get(position);

        TextView invoice_product_name = row.findViewById(R.id.invoice_product_name);
        TextView invoice_product_quantity = row.findViewById(R.id.invoice_product_quantity);
        TextView invoice_product_uprice = row.findViewById(R.id.invoice_product_uprice);
        TextView invoice_product_subtotal = row.findViewById(R.id.invoice_product_subtotal);
        TextView pp_exc_tax = row.findViewById(R.id.pp_exc_tax);
        TextView pp_tax = row.findViewById(R.id.pp_tax);
        TextView pp_discount = row.findViewById(R.id.pp_discount);
        TextView promoDetail = row.findViewById(R.id.txt_promo);

        ImageView product_image = row.findViewById(R.id.product_image);

        Glide.with(context)
                .load(GlobalConstants.PRODUCT_IMAGE_URL + product.image)
                .apply(new RequestOptions().override(256,256))
                .apply(RequestOptions.placeholderOf(R.drawable.ic_place_holder))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .into(product_image);
        invoice_product_name.setText(product.name);
        invoice_product_name.setSelected(true);
        invoice_product_quantity.setText(String.valueOf(product.amount));
        invoice_product_uprice.setText(String.format("%s%s", product.getUnitPrice(), GlobalVariables.BUSINESS_CURRENCY));
        float sub_total = GlobalFunctions.roundToDecimal(Float.parseFloat(product.getUnitPrice()) * product.amount, 2);
        invoice_product_subtotal.setText(String.format("%.2f%s", sub_total, GlobalVariables.BUSINESS_CURRENCY));
        pp_exc_tax.setText(context.getString(R.string.dpp_inc_tax, product.getDefault_sell_price(), GlobalVariables.BUSINESS_CURRENCY));
        String tax_type = "(" + GlobalVariables.BUSINESS_TAX_TYPE.toUpperCase() + ": " + GlobalVariables.BUSINESS_TAX + "%)";
        pp_tax.setText(context.getString(R.string.dpp_tax, product.getTax(), GlobalVariables.BUSINESS_CURRENCY, tax_type));
        pp_discount.setText(context.getString(R.string.dpp_discount) + product.getDiscount() + GlobalVariables.BUSINESS_CURRENCY + "(" + GlobalVariables.BUISINESS_DEFAULT_DISCOUNT * 100 + "%)");

        if( product.m_bEnableGroup )
        {
            promoDetail.setVisibility(View.VISIBLE);
            promoDetail.setText(context.getString(R.string.promo_detail) + product.strGroup);
        }
        else
            promoDetail.setVisibility(View.GONE);

        return row;
    }
}
