package com.bulletcart.videorewards.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bulletcart.videorewards.ApiResult.StoreInfoResult;
import com.bulletcart.videorewards.Global.GlobalVariables;
import com.bulletcart.videorewards.R;
import com.bulletcart.videorewards.Adapters.StoresListAdapter;
import com.bulletcart.videorewards.Utils.ApiUtil;
import com.bulletcart.videorewards.Utils.Notify;
import com.bulletcart.videorewards.app.App;

public class StoresActivity extends ActivityBase {

    private ListView stores;
    StoresListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stores);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.stores);

        initUI();
        initVar();
    }

    public void initUI() {
        stores = findViewById(R.id.stores);
    }

    public void initVar() {
        Context context = StoresActivity.this;
        mAdapter = new StoresListAdapter(StoresActivity.this, GlobalVariables.STORES);
        stores.setAdapter(mAdapter);
        stores.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                GlobalVariables.BUSINESS_ID = GlobalVariables.STORES.get(i).business_id;
                GlobalVariables.LOCATION_ID = GlobalVariables.STORES.get(i).id;
                GlobalVariables.SHOP_NAME = GlobalVariables.STORES.get(i).name;
                GlobalVariables.BUSINESS_CURRENCY = GlobalVariables.STORES.get(i).currency;
                GlobalVariables.BUSINESS_MERCHANT_CURRENCY = GlobalVariables.STORES.get(i).merchant_currency;
                GlobalVariables.BUSINESS_CURRENCY_STRING = GlobalVariables.STORES.get(i).currency_string;
                GlobalVariables.BUISINESS_DEFAULT_DISCOUNT = GlobalVariables.STORES.get(i).default_sales_discount;
                GlobalVariables.BUSINESS_DEFAULT_PRICE_GROUPS = GlobalVariables.STORES.get(i).selling_group;
                Intent intent = new Intent(StoresActivity.this, ScanActivity.class);
                startActivity(intent);
                finish();
            }
        });
        getStores();
    }

    @Override
    public void onBackPressed(){
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

                finish();
                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }

    public void getStores() {
        showpDialog();
        ApiUtil.get_stores(new Notify() {
            @Override
            public void onSuccess(Object object) {
                final StoreInfoResult data = (StoreInfoResult) object;
                GlobalVariables.STORES.clear();
                GlobalVariables.STORES.addAll(data.stores);
                mAdapter.notifyDataSetChanged();
                hidepDialog();
            }

            @Override
            public void onAbort(Object object) {
                hidepDialog();
            }

            @Override
            public void onFail() {
                hidepDialog();
            }
        });
    }
}
