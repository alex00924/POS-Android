package com.bulletcart.videorewards.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bulletcart.videorewards.Adapters.CashCheckRecyclerAdapter;

import com.bulletcart.videorewards.ApiResult.ResponseData;
import com.bulletcart.videorewards.Global.GlobalConstants;
import com.bulletcart.videorewards.Global.GlobalFunctions;
import com.bulletcart.videorewards.Global.GlobalVariables;
import com.bulletcart.videorewards.Model.ProductInfo;
import com.bulletcart.videorewards.R;
import com.bulletcart.videorewards.Utils.ApiUtil;
import com.bulletcart.videorewards.Utils.BarcodeGenerator;
import com.bulletcart.videorewards.Utils.Notify;
import com.bulletcart.videorewards.app.App;
import com.google.android.gms.common.internal.StringResourceValueReader;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;

import org.json.JSONException;
import org.json.JSONObject;

public class CashCheckOutActivity extends ActivityBase {

    private RecyclerView rv_checkout;
    private RecyclerView.LayoutManager mLayoutManager;

    private Context context;
    private final Handler mHandler = new Handler();
    private AlertDialog alertDialog = null;
    private Runnable mTimer;
    private Pusher pusher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_check_out);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("CHECK OUT");

        context = CashCheckOutActivity.this;
        initUI();
        initVariable();
        initPusher();
    }


    /**
     * initialize view
     */
    public void initUI() {

        rv_checkout = findViewById(R.id.checkout);
        rv_checkout.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        rv_checkout.setLayoutManager(mLayoutManager);

        Button btn_confirm = findViewById(R.id.btn_confirm);

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register_products();
            }
        });
    }

    /**
     * initializing variable to show
     * */
    public void initVariable() {
        rv_checkout.setAdapter(new CashCheckRecyclerAdapter(context, GlobalVariables.SELECTED_PRODUCTS));
    }

    /**
     * Initialize pusher so that listening the check result.
     */
    public void initPusher() {
        PusherOptions options = new PusherOptions();
        options.setCluster("us2");
        pusher = new Pusher(GlobalVariables.PUSHER_APP_KEY, options);
        Channel channel = pusher.subscribe(GlobalVariables.PUSHER_CHANNEL);

        // Show code dialog event
        channel.bind(GlobalVariables.PUSHER_EVENT, new SubscriptionEventListener() {
            @Override
            public void onEvent(String channelName, String eventName, final String data) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject pusher_data = new JSONObject(data);
                            String message = pusher_data.getString("message");
                            if (message.equals(GlobalVariables.CODE)) {
                                show_successDlg();
                            } else if (message.equals(GlobalVariables.PUSHER_CANCEL_EVENT + GlobalVariables.CODE)) {
                                cancel_products();
                            } else if (message.equals(GlobalVariables.PUSHER_PING_EVENT + GlobalVariables.CODE)) {
                                pong_pos();
                            }
                        } catch (JSONException e) {}
                    }
                });
            }
        });

    }

    /**
     * Send products he bought to the cashier to pay
     */
    public void register_products() {
       final AlertDialog processing = GlobalFunctions.showSpotDialog(context, getString(R.string.processing));
        ApiUtil.register_products(new Notify() {
            @Override
            public void onSuccess(Object object) {

                if (processing != null) processing.dismiss();
                //establish socket communication to listen checking result
                pusher.connect();

                ResponseData responseData = (ResponseData) object;
                GlobalVariables.CODE = responseData.message;
                alertDialog = new BarcodeDialog(context, GlobalVariables.CODE);
                alertDialog.setCancelable(false);
                alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
                alertDialog.show();

            }

            @Override
            public void onAbort(Object object) {
                if (processing != null) processing.dismiss();
                GlobalFunctions.showAlertdialog(context, getString(R.string.register_product_error));
            }

            @Override
            public void onFail() {
                if (processing != null) processing.dismiss();
                GlobalFunctions.showAlertdialog(context, getString(R.string.register_product_error));
            }
        });

    }

    /**
     * customized alert dialog to show barcode for check payment.
     */
    private class BarcodeDialog extends AlertDialog {

        BarcodeDialog(final Context context, String code) {
            super(context);
            Bitmap bitmap = null;
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.cash_check_code_dialog, null);
            setView(view);

            ImageView barcode = view.findViewById(R.id.img_barcode);
            TextView tv_code = view.findViewById(R.id.check_code);
            TextView cash_to_pay = view.findViewById(R.id.cash_to_pay);
            cash_to_pay.setText(String.format("%s %s", String.valueOf(GlobalVariables.TOTAL_PRICE_TO_CHECK), GlobalVariables.BUSINESS_CURRENCY));
            final TextView tv_count_down = view.findViewById(R.id.count_down);
            TextView btn_close = view.findViewById(R.id.btn_close);

            btn_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove_products(GlobalVariables.CODE, "delete");
                }
            });

            final int[] current_time = {0};
            final int[] display_time = {30};
            mTimer = new Runnable() {
                @Override
                public void run() {
                    String time_txt = "00:30";
                    current_time[0]++;
                    if(current_time[0] <= 30) {
                        display_time[0] = 30 - current_time[0];
                        time_txt = getString(R.string.count_down, (display_time[0] < 10) ? "0" + String.valueOf(display_time[0]) : String.valueOf(display_time[0]));
                    } else {
                        time_txt = "Please Wait...\n Don’t close the app!";
                    }
                    tv_count_down.setText(time_txt);
                    if(current_time[0] == 30 && !GlobalVariables.CODE.equals("")) {
                        mHandler.removeCallbacksAndMessages(null);
                        remove_products(GlobalVariables.CODE, "onlyUnchecked");
                    }
                    mHandler.postDelayed(this, 1000);
                }
            };
            mHandler.postDelayed(mTimer, 1000);
            try {
                BarcodeGenerator barcodeGenerator = new BarcodeGenerator(code, BarcodeFormat.CODE_128, 600, 300);
                bitmap = barcodeGenerator.encodeAsBitmap();
                barcode.setImageBitmap(bitmap);
                tv_code.setText(code);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Remove products from the paylist and create new code next time
     * @param code : uid to identify user's product
     * @param mode : delete anyway.
     */
    public void remove_products(String code, String mode) {
        ApiUtil.remove_products(code, mode, new Notify() {
            @Override
            public void onSuccess(Object object) {
                //disconnect socket

                ResponseData responseData = (ResponseData) object;
//                mHandler.removeCallbacksAndMessages(null);
                if(!responseData.message.equals("checked") && !responseData.message.equals("awaiting checkout")) {
                    pusher.disconnect();
                    if(alertDialog != null) {
                        alertDialog.dismiss();
                        alertDialog = null;
                        GlobalVariables.CODE = "";
                    }
                }
            }

            @Override
            public void onAbort(Object object) {

            }

            @Override
            public void onFail() {

            }
        });
    }

    public void cancel_products() {
        if(alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
            GlobalVariables.CODE = "";
            GlobalFunctions.vibrate(context, 100);
        }
    }

    public void show_successDlg() {
        App.getInstance().setUsedPoints(GlobalVariables.USED_POINTS_TO_CHECK);
        //Update user balance in web panel
        if(GlobalVariables.USED_POINTS_TO_CHECK > 0) {
            GlobalFunctions.updateMyPoints(context, null);
        }

        GlobalVariables.SELECTED_PRODUCTS.clear();
        GlobalVariables.USED_POINTS_TO_CHECK = 0;
        GlobalVariables.TOTAL_PRICE_TO_CHECK = 0;
        GlobalVariables.CODE = "";
        if(alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
        AlertDialog successDlg = new SuccessDialog(context);
        successDlg.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
        successDlg.setCancelable(false);
        successDlg.show();
        GlobalFunctions.vibrate(context, 100);
    }

    private class SuccessDialog extends AlertDialog {

        SuccessDialog(final Context context) {
            super(context);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.payment_success_dialog, null);
            setView(view);

            Button btn_close = view.findViewById(R.id.btn_close);

            btn_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    Intent mIntent = new Intent(context, ScanActivity.class);
                    startActivity(mIntent);
                    finish();
                }
            });

        }

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
        Intent i = new Intent(context, ScanActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        finish();
    }

    public void pong_pos(){
        if (alertDialog != null) {
            ApiUtil.pos_pong(GlobalVariables.PUSHER_PONG_EVENT + GlobalVariables.CODE, new Notify() {
                @Override
                public void onSuccess(Object object) {
                    Log.d("pong=>","successfully pong to POS");
                }

                @Override
                public void onAbort(Object object) {
                    Log.e("pong=>","pong error to POS");
                }

                @Override
                public void onFail() {

                }
            });
        }
    }
}
