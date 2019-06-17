package com.bulletcart.videorewards.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bulletcart.videorewards.Adapters.TransactionProductsListAdapter;
import com.bulletcart.videorewards.Global.GlobalFunctions;
import com.bulletcart.videorewards.Global.GlobalVariables;
import com.bulletcart.videorewards.Model.Transaction;
import com.bulletcart.videorewards.R;
import com.bulletcart.videorewards.app.App;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BillActivity extends ActivityBase {

    private TextView tv_invocie_no, tv_uid, tv_invoice_date, tv_cash_price, tv_date, tv_total_paid, tv_points;
    private ListView lv_invoice_products;
    private TransactionProductsListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.title_invoice);

          //Update user balance after purchase on POS
        if(GlobalVariables.USED_POINTS_TO_CHECK > 0) {
            GlobalFunctions.updateMyPoints( BillActivity.this, null);
        }

        initUI();
        setVariable();
    }

    private void initUI() {
        tv_invocie_no = findViewById(R.id.invoice_no);
//        tv_uid = findViewById(R.id.uid);
        tv_invoice_date = findViewById(R.id.invoice_date);
        tv_cash_price = findViewById(R.id.cash_price);
        tv_date = findViewById(R.id.date);
        tv_total_paid = findViewById(R.id.total_paid);
        tv_points = findViewById(R.id.total_points);

        lv_invoice_products = findViewById(R.id.invoice_products);

    }

    private void setVariable() {
        Transaction transaction = (Transaction) getIntent().getExtras().getSerializable("transaction");

        Date c = Calendar.getInstance().getTime();
        Date d = Calendar.getInstance().getTime();

        SimpleDateFormat invoice_date = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        SimpleDateFormat date = new SimpleDateFormat("mm/dd/yyyy");
        String formattedInvoiceDate = invoice_date.format(c);
        String formattedDate = date.format(d);

        tv_invocie_no.setText(getString(R.string.invoice_no, transaction.invoice_no));
//        tv_uid.setText(getString(R.string.uid, transaction.created_by));
        tv_invoice_date.setText(getString(R.string.invoice_date, formattedInvoiceDate));
//        float total_price = GlobalFunctions.roundToDecimal(Float.parseFloat(transaction.final_total), 2);
        float cash_total_price = GlobalVariables.TOTAL_PRICE_TO_CHECK + Float.parseFloat(GlobalVariables.CURRENCY_OF_USED_POINTS_TO_CHECK);
        float total_price = GlobalVariables.TOTAL_PRICE_TO_CHECK;
        tv_cash_price.setText( String.valueOf(cash_total_price) + " " + GlobalVariables.BUSINESS_CURRENCY);
        tv_date.setText(formattedInvoiceDate);
        tv_total_paid.setText(String.valueOf(total_price) + " " + GlobalVariables.BUSINESS_CURRENCY);
        tv_points.setText(getString(R.string.invoice_redeemed_points, GlobalVariables.USED_POINTS_TO_CHECK, GlobalVariables.CURRENCY_OF_USED_POINTS_TO_CHECK, GlobalVariables.BUSINESS_CURRENCY));

        if (GlobalVariables.USED_POINTS_TO_CHECK == 0) {
            tv_points.setText("0");
        }

        adapter = new TransactionProductsListAdapter(BillActivity.this, GlobalVariables.SELECTED_PRODUCTS);
        lv_invoice_products.setAdapter(adapter);

        GlobalVariables.USED_POINTS_TO_CHECK = 0;
        GlobalVariables.TOTAL_PRICE_TO_CHECK = 0;
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
    public void onBackPressed() {
        back();
    }

    private void back() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BillActivity.this);

        alertDialog.setTitle("");
        alertDialog.setMessage(getString(R.string.leave_bill));

        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {//Clear purchased products
                GlobalVariables.SELECTED_PRODUCTS.clear();

                Intent intent = new Intent(BillActivity.this, ScanActivity.class);
                startActivity(intent);
                finish();
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }
}
