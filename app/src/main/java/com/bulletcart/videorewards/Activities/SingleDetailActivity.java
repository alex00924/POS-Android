package com.bulletcart.videorewards.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bulletcart.videorewards.Global.GlobalConstants;
import com.bulletcart.videorewards.Global.GlobalFunctions;
import com.bulletcart.videorewards.Global.GlobalVariables;
import com.bulletcart.videorewards.Model.ProductInfo;
import com.bulletcart.videorewards.R;
import com.bulletcart.videorewards.app.App;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

public class SingleDetailActivity extends AppCompatActivity {

    ImageView productImage, im;
    TextView toolbar_title, productName, productExcTax, productTax, productRate, productCompany, productDiscount, promoDetail;
    LinearLayout productNamell, productRatell, productCompanyll;
    ProductInfo listobject = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        productNamell = findViewById(R.id.productNamell);
        productRatell = findViewById(R.id.productRatell);
        productCompanyll = findViewById(R.id.productCompanyll);
        productName = findViewById(R.id.productName);
        productExcTax= findViewById(R.id.pp_exc_tax);
        productTax = findViewById(R.id.pp_tax);
        productDiscount = findViewById(R.id.pp_discount);
        productRate = findViewById(R.id.productRate);
        productCompany = findViewById(R.id.productCompany);
        productImage = findViewById(R.id.productImage);
        promoDetail = findViewById(R.id.txt_promo);

        listobject = (ProductInfo) getIntent().getExtras().getSerializable("List_Item");
        if (listobject != null) {

            productCompany.setText(String.format("%s%s",getString(R.string.company_txt), GlobalVariables.SHOP_NAME));
            Glide.with(SingleDetailActivity.this)
                    .load(GlobalConstants.PRODUCT_IMAGE_URL + listobject.image)
                    .apply(new RequestOptions().override(256,256))
                    .apply(RequestOptions.placeholderOf(R.drawable.ic_place_holder))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .apply(RequestOptions.skipMemoryCacheOf(true))
                    .into(productImage);
            productRate.setText(String.format("%s%s%s", getString(R.string.unit_price_txt), listobject.getUnitPrice(), GlobalVariables.BUSINESS_CURRENCY));
            productName.setText(String.format("%s%s", getString(R.string.name_txt), listobject.name));
            productExcTax.setText(String.format("%s%s%s", getString(R.string.exc_tax_txt), listobject.getDefault_sell_price(), GlobalVariables.BUSINESS_CURRENCY));
//            float tax = Float.parseFloat(listobject.sell_price_inc_tax) - Float.parseFloat(listobject.default_purchase_price);
//            productTax.setText(String.format("%s%s%s",  getString(R.string.tax_txt), String.valueOf(tax), GlobalVariables.BUSINESS_CURRENCY));
            String tax_type = "(" + GlobalVariables.BUSINESS_TAX_TYPE.toUpperCase() + ": " + GlobalVariables.BUSINESS_TAX + "%)";
            productTax.setText(getString(R.string.dpp_tax, listobject.getTax(), GlobalVariables.BUSINESS_CURRENCY, tax_type));
            String str_discount = "(" + GlobalVariables.BUISINESS_DEFAULT_DISCOUNT * 100 + "%)";
            productDiscount.setText( getString(R.string.default_discount_txt) + ": " + listobject.getDiscount() + GlobalVariables.BUSINESS_CURRENCY + str_discount );
            if(listobject.m_bEnableGroup) {
                promoDetail.setText(getString(R.string.promo_detail) + listobject.strGroup);
                promoDetail.setVisibility(View.VISIBLE);
            }
            else
                promoDetail.setVisibility(View.GONE);
        }

        getSupportActionBar().setTitle(listobject.name);

    }

    public void goBack() {
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_simple, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        menu.findItem(R.id.points).setTitle(getString(R.string.app_currency).toUpperCase()+" : " + App.getInstance().getBalance());
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home: {

                onBackPressed();
                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onBackPressed(){
        goBack();
    }
}
