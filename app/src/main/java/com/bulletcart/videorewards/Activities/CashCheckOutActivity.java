package com.bulletcart.videorewards.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
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
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

public class CashCheckOutActivity extends ActivityBase {
    private static Menu menu;

    private RecyclerView rv_checkout;
    private RecyclerView.LayoutManager mLayoutManager;

    private CashCheckOutActivity context;
    private final Handler mHandler = new Handler();
    private AlertDialog alertDialog = null;
    private Runnable mTimer;
    private Pusher pusher;

    private Button m_btn_confirm;

    private List<ProductInfo> m_ProductData = new ArrayList<>();
    private boolean m_bCanClose = true;

    private TextView m_txtState;
    private AlertDialog dlgNewOrderSucess = null;
    private AlertDialog dlgWait = null;
    private AlertDialog dlgEnter = null;
    private AlertDialog dlgNewOrderCancel = null;
    private AlertDialog dlgCompleteOrderSucess = null;
    private AlertDialog dlgCompleteOrderCancel = null;

    private boolean bRunning = false;
    private boolean deliveryStarted = false;
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
        Log.e("onCreate::::", "aaaa");
    }
    @Override
    protected void onResume() {
        bRunning = true;
        super.onResume();
        Log.e("onResume::::", "aaaa");
    }

    @Override
    protected void onPause() {
        bRunning = false;
        super.onPause();
        Log.e("onPause::::", "aaaa");
    }

    @Override
    public void onDestroy() {
        if(pusher != null) {
            pusher.disconnect();
            pusher = null;
        }
        super.onDestroy();
    }
    /**
     * initialize view
     */
    public void initUI() {

        rv_checkout = findViewById(R.id.checkout);
        mLayoutManager = new LinearLayoutManager(this);
        rv_checkout.setLayoutManager(mLayoutManager);

        m_btn_confirm = findViewById(R.id.btn_confirm);

        m_btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if( GlobalVariables.BUSINESS_PAYMENT_TYPE == GlobalConstants.PAYMENT_CASH || !GlobalVariables.BUSINESS_IS_DELYVERY ) {
//                    register_products();
//                }
//                else {
//                    Intent cardCheckOut = new Intent(CashCheckOutActivity.this, CheckOutActivity.class);
//                    startActivity(cardCheckOut);
//                    finish();
//                }
                if( GlobalVariables.BUSINESS_IS_DELYVERY ) {
                    orderDelivery();
                }
                else {
                    register_products();
                }
            }
        });

        m_txtState = findViewById(R.id.txt_state);
//        if(GlobalVariables.BUSINESS_IS_DELYVERY) {
//            m_txtState.setVisibility(View.VISIBLE);
//        }
    }

    /**
     * initializing variable to show
     * */
    public void initVariable() {
        for(ProductInfo productInfo: GlobalVariables.SELECTED_PRODUCTS) {
            if(productInfo.amount > 0)
                m_ProductData.add(productInfo);
        }
        rv_checkout.setAdapter(new CashCheckRecyclerAdapter(context, m_ProductData));
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
                            Log.e("MESSAGE---::: ", message);
                            if (message.equals(GlobalVariables.CODE)) {
                                show_successDlg();
                            } else if (message.equals(GlobalVariables.PUSHER_CANCEL_EVENT + GlobalVariables.CODE)) {
                                cancel_products();
                            } else if (message.equals(GlobalVariables.PUSHER_PING_EVENT + GlobalVariables.CODE)) {
                                pong_pos();
                            }
                            else if (message.equals(GlobalVariables.PUSHER_PING_EVENT + GlobalVariables.BUSINESS_DELIVERY_UID)) {
                                pong_delivery();
                            }
                            //Delivery Order confirm / cancel
                            else if (message.equals(GlobalConstants.PUSHER_ORDER_CONFIRM + GlobalVariables.BUSINESS_DELIVERY_UID)) {
                                deliveryStarted = true;
                                if (GlobalVariables.BUSINESS_PAYMENT_TYPE == GlobalConstants.PAYMENT_CARD) {
                                    showNotify("Congrats!! Your order is confirmed.", "Please enter your card details.");
                                    if(bRunning) {
                                        hideAllOrderDlg();
                                        if(pusher != null) {
                                            pusher.disconnect();
                                            pusher = null;
                                        }
                                        Intent appintent = new Intent(context, CheckOutActivity.class);
                                        startActivityForResult(appintent, 1);
                                    } else {
                                        show_reenterCardDlg();
                                    }
                                } else {
                                    showNotify("Congrats!! Your order is confirmed.", "It will be delivered to you soon.");
                                    show_successDlgForDelivery();
                                }
                            } else if (message.equals(GlobalConstants.PUSHER_ORDER_CANCEL + GlobalVariables.BUSINESS_DELIVERY_UID)) {
                                showNotify("Unfortunately your order is cancelled", "");
                                show_failDlgForDelivery();
                            } else if (message.equals(GlobalConstants.PUSHER_PENDING_ORDER_CONFIRM+ GlobalVariables.BUSINESS_DELIVERY_UID)) {
                                showNotify("Congrats! Your order has been delivered.", "Thanks for using Bullet Cart.");
                                show_successDlgForCompleteDelivery();
                            } else if (message.equals(GlobalConstants.PUSHER_PENDING_ORDER_CANCEL+ GlobalVariables.BUSINESS_DELIVERY_UID)) {
                                showNotify("Unfortunately your order was cancelled", "");
                                show_failDlgForCompleteDelivery();
                            }
                        } catch (JSONException e) {}
                    }
                });
            }
        });
        pusher.connect();
    }

    private void showNotify(String strTitle, String strMsg) {
        NotificationManager notif=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "NOTIFICATION")
                .setSmallIcon(R.drawable.ic_stat_shopping_cart)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_launcher))
                .setContentTitle(strTitle)
                .setContentText(strMsg)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(strMsg))
                .setAutoCancel(true);
        if(!bRunning) {
            Intent appintent = new Intent(this, CashCheckOutActivity.class);
            appintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent appIntent = PendingIntent.getActivity(this, 1, appintent, PendingIntent.FLAG_ONE_SHOT);
            notificationBuilder.setContentIntent(appIntent);
        }
        notif.notify(0, notificationBuilder.build());
    }
    /**
     * Send products he bought to the cashier to pay
     */
    public void register_products() {
       final AlertDialog processing = GlobalFunctions.showSpotDialog(context, getString(R.string.processing));
        ApiUtil.register_products(new Notify() {
            @Override
            public void onSuccess(Object object) {

                if (processing != null)
                    processing.dismiss();

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
                    if(pusher != null) {
                        pusher.disconnect();
                        pusher = null;
                    }
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

    private void show_dlg(final AlertDialog dlg) {
        new Thread() {
            public void run() {
                while (!bRunning) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dlg.show();
                        GlobalFunctions.vibrate(context, 100);
                    }
                });
            }
        }.start();
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
        show_dlg(successDlg);
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
                    goScanPage();
                }
            });
        }
    }

    /////////////////New Delivery///////////////////////

    public void show_waitDlg() {
        hideAllOrderDlg();
        dlgWait = new WaitDlg(context);
        dlgWait.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
        dlgWait.setCancelable(true);
        show_dlg(dlgWait);
    }

    public void show_reenterCardDlg() {
        hideAllOrderDlg();
        m_txtState.setText(getString(R.string.enter_card_content));
        dlgEnter = new EnterCardInfoDlg(context);
        dlgEnter.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
        dlgEnter.setCancelable(false);
        show_dlg(dlgEnter);
    }


    public void show_successDlgForDelivery() {
        hideAllOrderDlg();
        if(GlobalVariables.BUSINESS_PAYMENT_TYPE == GlobalConstants.PAYMENT_CASH) {
            App.getInstance().setUsedPoints(GlobalVariables.USED_POINTS_TO_CHECK);
            if(GlobalVariables.USED_POINTS_TO_CHECK > 0) {
                GlobalFunctions.updateMyPoints(context, null);
            }
        }

        m_txtState.setText(getString(R.string.wait_delivery));
        dlgNewOrderSucess = new SuccessDialogForNewOrder(context);
        dlgNewOrderSucess.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
        dlgNewOrderSucess.setCancelable(true);
        show_dlg(dlgNewOrderSucess);
    }

    public void show_failDlgForDelivery() {
        hideAllOrderDlg();
        dlgNewOrderCancel = new FailDialogForNewOrder(context);
        dlgNewOrderCancel.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
        dlgNewOrderCancel.setCancelable(false);
        show_dlg(dlgNewOrderCancel);
    }

    /////////////////Pending Delivery///////////////////////
    public void show_successDlgForCompleteDelivery() {
        hideAllOrderDlg();
//        App.getInstance().setUsedPoints(GlobalVariables.USED_POINTS_TO_CHECK);
//        //Update user balance in web panel
//        if(GlobalVariables.USED_POINTS_TO_CHECK > 0) {
//            GlobalFunctions.updateMyPoints(context, null);
//            GlobalVariables.USED_POINTS_TO_CHECK = 0;
//        }
        GlobalVariables.USED_POINTS_TO_CHECK = 0;
        m_txtState.setText(getString(R.string.congrats_delivery_sucess));
        dlgCompleteOrderSucess = new SuccessDialogForCompleteOrder(context);
        dlgCompleteOrderSucess.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
        dlgCompleteOrderSucess.setCancelable(false);
        show_dlg(dlgCompleteOrderSucess);
    }

    public void show_failDlgForCompleteDelivery() {
        hideAllOrderDlg();
        //When cancel, increase bullet
        App.getInstance().setUsedPoints(-GlobalVariables.USED_POINTS_TO_CHECK);
        //Update user balance in web panel
        if(GlobalVariables.USED_POINTS_TO_CHECK > 0) {
            GlobalFunctions.updateMyPoints(context, null);
            GlobalVariables.USED_POINTS_TO_CHECK = 0;
        }

        dlgCompleteOrderCancel = new FailDialogForCompleteOrder(context);
        dlgCompleteOrderCancel.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
        dlgCompleteOrderCancel.setCancelable(false);
        show_dlg(dlgCompleteOrderCancel);
    }

    ///////////////////////////////////////////
    private class SuccessDialogForNewOrder extends AlertDialog {
        SuccessDialogForNewOrder(final Context context) {
            super(context);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.payment_success_dialog, null);
            setView(view);

            Button btn_close = view.findViewById(R.id.btn_close);

            btn_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
    }

    private class WaitDlg extends AlertDialog {
        WaitDlg(final Context context) {
            super(context);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.wait_dlg, null);
            setView(view);
        }
    }

    private class EnterCardInfoDlg extends AlertDialog {
        EnterCardInfoDlg(final Context context) {
            super(context);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.enter_card_info_dlg, null);
            setView(view);

            Button btn_ok = view.findViewById(R.id.btn_ok);

            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideAllOrderDlg();
                    if(pusher != null) {
                        pusher.disconnect();
                        pusher = null;
                    }
                    Intent appintent = new Intent(context, CheckOutActivity.class);
                    startActivityForResult(appintent, 1);
                }
            });
        }
    }

    private class FailDialogForNewOrder extends AlertDialog {
        FailDialogForNewOrder(final Context context) {
            super(context);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.new_delivery_cancel_dialog, null);
            ((TextView)view.findViewById(R.id.txt_title)).setText(getString(R.string.new_delivery_cancel, GlobalVariables.BUSINESS_NAME));
            setView(view);

            Button btn_close = view.findViewById(R.id.btn_close);

            btn_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    m_bCanClose = true;
                    goScanPage();
                }
            });
        }
    }
    private class SuccessDialogForCompleteOrder extends AlertDialog {
        SuccessDialogForCompleteOrder(final Context context) {
            super(context);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.delivery_complete_sucess_dialog, null);
            setView(view);
            ((TextView)view.findViewById(R.id.txt_title)).setText(getString(R.string.delivery_complete_sucess));

            Button btn_close = view.findViewById(R.id.btn_close);

            btn_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    m_bCanClose = true;
                    goScanPage();
                }
            });
        }
    }

    private class FailDialogForCompleteOrder extends AlertDialog {
        FailDialogForCompleteOrder(final Context context) {
            super(context);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.delivery_complete_cancel_dialog, null);
            setView(view);
            ((TextView)view.findViewById(R.id.txt_title)).setText(getString(R.string.delivery_complete_fail, GlobalVariables.BUSINESS_NAME));
            Button btn_close = view.findViewById(R.id.btn_close);

            btn_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    m_bCanClose = true;
                    goScanPage();
                }
            });
        }
    }

    private void hideAllOrderDlg() {

        if (dlgWait != null) {
            dlgWait.dismiss();
            dlgWait = null;
        }
        if (dlgEnter != null) {
            dlgEnter.dismiss();
            dlgEnter = null;
        }
        if (dlgNewOrderSucess != null ) {
            dlgNewOrderSucess.dismiss();
            dlgNewOrderSucess = null;
        }
        if (dlgNewOrderCancel != null ) {
            dlgNewOrderCancel.dismiss();
            dlgNewOrderCancel = null;
        }
        if (dlgCompleteOrderSucess != null ) {
            dlgCompleteOrderSucess.dismiss();
            dlgCompleteOrderSucess = null;
        }
        if (dlgCompleteOrderCancel != null ) {
            dlgCompleteOrderCancel.dismiss();
            dlgCompleteOrderCancel = null;
        }
    }
/////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        context.menu = menu;
        getMenuInflater().inflate(R.menu.menu_simple, menu);
        return true;
    }

    public static void setOptionTitle(String title){
        MenuItem item = menu.findItem(R.id.points);
        item.setTitle(title);
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
        goScanPage();
    }

    private void goScanPage() {
        if(!m_bCanClose)
        {
            Toast.makeText(CashCheckOutActivity.this,"Please Wait while server response...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(pusher != null) {
            pusher.disconnect();
            pusher = null;
        }
        if(GlobalVariables.BUSINESS_IS_DELYVERY && deliveryStarted) {
            GlobalVariables.SELECTED_PRODUCTS.clear();
            GlobalVariables.USED_POINTS_TO_CHECK = 0;
            GlobalVariables.TOTAL_PRICE_TO_CHECK = 0;
            GlobalVariables.CODE = "";
        }
        if(alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
        hideAllOrderDlg();

        if(deliveryStarted) {
            Intent i = new Intent(context, ScanActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        } else {
            Intent intent = new Intent(context, ScanActivity.class);
            setResult(Activity.RESULT_OK, intent);
        }
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

    public void pong_delivery(){
        ApiUtil.pos_pong(GlobalVariables.PUSHER_PONG_EVENT + GlobalVariables.BUSINESS_DELIVERY_UID, new Notify() {
            @Override
            public void onSuccess(Object object) {
                Log.d("pong=>","successfully pong to DELIVERY");
            }

            @Override
            public void onAbort(Object object) {
                Log.e("pong=>","pong error to DELIVERY");
            }

            @Override
            public void onFail() {

            }
        });
    }


    public void orderDelivery() {
        showpDialog();
        Log.e(  " ORDER Detail : ", GlobalVariables.ORDER_DETAIL.toString());
        ApiUtil.order_delivery( GlobalVariables.ORDER_DETAIL, new Notify() {
            @Override
            public void onSuccess(Object object) {
                hidepDialog();
                Toast.makeText(CashCheckOutActivity.this, "Your order is transfered successfully", Toast.LENGTH_LONG).show();
                GlobalVariables.BUSINESS_DELIVERY_UID =  ((ResponseData)object).order_uid;
                m_txtState.setText(getString(R.string.wait_order_confirm));
                m_btn_confirm.setVisibility(View.GONE);
                m_bCanClose = false;

                show_waitDlg();

//                TextView txt_title = m_OrderDlg.findViewById(R.id.txt_title);
//                txt_title.setText(R.string.please_wait);
//                (m_OrderDlg.findViewById(R.id.layout_btn)).setVisibility(View.GONE);
//                GlobalVariables.BUSINESS_DELIVERY_UID =  ((ResponseData)object).order_uid;


                /////////Temp
//                Toast.makeText(ScanActivity.this, "Your order confirmed!", Toast.LENGTH_SHORT).show();
//                m_OrderDlg.dismiss();
//                AlertDialog pointAlertDialog = new ScanActivity.paymentPointAlertDialog(mContext, false);
//                pointAlertDialog.setCancelable(false);
//                pointAlertDialog.show();

                //////////

//                listenWebPusher();
            }

            @Override
            public void onAbort(Object object) {
                hidepDialog();
                ResponseData data = (ResponseData) object;
                GlobalFunctions.showAlertdialog(CashCheckOutActivity.this, data.message);
            }

            @Override
            public void onFail() {
                hidepDialog();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                show_successDlgForDelivery();
                m_btn_confirm.setVisibility(View.GONE);
                deliveryStarted = true;
                initPusher();

                App.getInstance().setUsedPoints(GlobalVariables.USED_POINTS_TO_CHECK);
                if(GlobalVariables.USED_POINTS_TO_CHECK > 0) {
                    GlobalFunctions.updateMyPoints(context, null);
                }
            } else {
                Toast.makeText(CashCheckOutActivity.this,"Your card transaction was not successed, Please try again", Toast.LENGTH_LONG).show();
                show_reenterCardDlg();
            }
        }
        Log.e("onActivityResult::::", "aaaa");
    }
}
