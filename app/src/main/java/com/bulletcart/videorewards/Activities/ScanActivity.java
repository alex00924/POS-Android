package com.bulletcart.videorewards.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bulletcart.videorewards.Adapters.ListViewAdapter;
import com.bulletcart.videorewards.ApiResult.DeliveryProductInfoResult;
import com.bulletcart.videorewards.ApiResult.GroupPrice;
import com.bulletcart.videorewards.ApiResult.ProductInfoResult;
import com.bulletcart.videorewards.ApiResult.ResponseData;
import com.bulletcart.videorewards.Global.GlobalConstants;
import com.bulletcart.videorewards.Global.GlobalFunctions;
import com.bulletcart.videorewards.Global.GlobalVariables;
import com.bulletcart.videorewards.Model.ProductInfo;
import com.bulletcart.videorewards.Model.TableInfo;
import com.bulletcart.videorewards.R;
import com.bulletcart.videorewards.Utils.ApiUtil;
import com.bulletcart.videorewards.Utils.Notify;
import com.bulletcart.videorewards.Views.TintOnStateImageView;
import com.bulletcart.videorewards.app.App;
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;
import com.daimajia.swipe.util.Attributes;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.zxing.client.android.Intents;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.giovannidefrancesco.easysharedprefslib.IStorage;
import eu.giovannidefrancesco.easysharedprefslib.SharedPreferenceStorage;
import pl.droidsonroids.gif.GifImageView;

public class ScanActivity extends ActivityBase implements View.OnClickListener, ListViewAdapter.ProductListItemCallback {

    public ListView list;
    public TextView totalRate;
    public TextView totalRate_;
    private float   fTotVal;
    ListViewAdapter mAdapter = null;

    public static float totalvalue = 0;
    private Activity mContext;
    public LinearLayout btn_pay;
    public GifImageView iv_scanner;
    public TextView tv_instructor;

    public final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final int RC_CHECKOUT = 9002;
    private int used_point = 0;
    private String currency_used_point = "0.0";
    Button edt_barcode;

    private static App mInstance;

    private Dialog m_GroupDlg;
    private Dialog m_MethodDlg;
    private Dialog m_OrderDlg;

    private Button m_btnOrder;

    private ProductInfo m_selectedProduct;
    private TableInfo m_resTables;

    private boolean m_Clean = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(GlobalVariables.SHOP_NAME);

        mContext = ScanActivity.this;
        initUI();
        initVariable();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
//        if( m_MethodDlg != null) {
//            m_MethodDlg.dismiss();
//            m_MethodDlg = null;
//        }
        super.onPause();
    }


    public void initVariable() {

        if(m_Clean) {
            GlobalVariables.SELECTED_PRODUCTS.clear();
        }
        mAdapter = new ListViewAdapter(mContext, list, GlobalVariables.SELECTED_PRODUCTS, this);
        list.setAdapter(mAdapter);

        updateRate();

        //get currency ration with business currency
        GlobalFunctions.getCurrencyRatio();
    }
    public void initUI() {

        tv_instructor = findViewById(R.id.tv_instructor);

        btn_pay = findViewById(R.id.payButton);
        edt_barcode= findViewById(R.id.enterManually);

        iv_scanner = findViewById(R.id.barcode_scanner);
        iv_scanner.setOnClickListener(this);

        edt_barcode.setOnClickListener(this);

        btn_pay.setOnClickListener(this);

        list = findViewById(R.id.list);
        totalRate = findViewById(R.id.totalRate);
        totalRate_ = findViewById(R.id.totalRate_);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i2 = new Intent(mContext, SingleDetailActivity.class);
                ProductInfo productInfo = mAdapter.getData().get(position);
                i2.putExtra("List_Item", productInfo);
                startActivity(i2);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
            }
        });

        m_btnOrder = findViewById(R.id.btn_order);
        m_btnOrder.setOnClickListener(this);

        if(!m_Clean) {
            showDeliveryView(true);
            return;
        }
        //this location can delivery, show select delivery or direct sell dialog
        if( GlobalVariables.BUSSINESS_CAN_DELIVEY )
            showMethodDlg();
        else
        {
            GlobalVariables.BUSINESS_IS_DELYVERY = false;
            if(GlobalVariables.BUSINESS_TYPE == GlobalConstants.BUSINESS_TYPE_RESTAURANT)
            {
                showDineinView();
                getAllProducts();
                getTables();
            }
            else
                showDeliveryView(false);
        }
    }

    //show/hide some views in the case of dine in restaurant
    private void showDineinView()
    {
        m_btnOrder.setVisibility(View.GONE);
        btn_pay.setVisibility(View.VISIBLE);
        edt_barcode.setVisibility(View.GONE);
        iv_scanner.setVisibility(View.GONE);
        tv_instructor.setVisibility(View.GONE);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        params.setMargins(0, 0, 0, 65);
        list.setLayoutParams(params);
    }

    //show/hide delivery related views
    //param = true : show delivery views, false: hide delivery views
    private void showDeliveryView(boolean bDeliveryShow)
    {
        if( !bDeliveryShow )
        {
            m_btnOrder.setVisibility(View.GONE);
            btn_pay.setVisibility(View.VISIBLE);
            edt_barcode.setVisibility(View.VISIBLE);
            iv_scanner.setVisibility(View.VISIBLE);
            tv_instructor.setVisibility(View.VISIBLE);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
            );
            params.setMargins(0, 0, 0, 110);
            list.setLayoutParams(params);
        }
        else
        {
            m_btnOrder.setVisibility(View.VISIBLE);
            btn_pay.setVisibility(View.GONE);
            edt_barcode.setVisibility(View.GONE);
            iv_scanner.setVisibility(View.GONE);
            tv_instructor.setVisibility(View.GONE);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
            );
            params.setMargins(0, 0, 0, 65);
            list.setLayoutParams(params);
        }
    }

    public void listVisibiltiy() {
        if (mAdapter == null)
            mAdapter = new ListViewAdapter(this, list, GlobalVariables.SELECTED_PRODUCTS, this);

        if (mAdapter.getData() != null && mAdapter.getData().size() > 0) {
            isProductLists(true);
        } else {
            isProductLists(false);
        }
        updateRate();
    }

    /**
     * if show barcode scanner image or not
     * @param isExist
     */
    public void isProductLists(boolean isExist) {
        if(isExist) {
            list.setVisibility(View.VISIBLE);
            iv_scanner.setVisibility(View.GONE);
            tv_instructor.setVisibility(View.GONE);
        } else {
            list.setVisibility(View.GONE);
            //in the case of store and isn't delivery
            if( !GlobalVariables.BUSINESS_IS_DELYVERY && GlobalVariables.BUSINESS_TYPE == GlobalConstants.BUSINESS_TYPE_STORE) {
                iv_scanner.setVisibility(View.VISIBLE);
                tv_instructor.setVisibility(View.VISIBLE);
            }
        }
    }

    public void getGroupPrice()
    {
        showpDialog();
        ApiUtil.get_group_price(m_selectedProduct.variation_id, m_selectedProduct.strGroup, Integer.parseInt(m_selectedProduct.tax), new Notify() {
            @Override
            public void onSuccess(Object object) {
                hidepDialog();
                GroupPrice groupPrice = (GroupPrice) object;
                if( groupPrice.state < 0 )
                {
                    Toast.makeText(ScanActivity.this, "Promo Not Valid for this Product", Toast.LENGTH_SHORT).show();
                    m_selectedProduct.m_bEnableGroup = false;
                    m_selectedProduct.group_price = 0;
                }
                else
                {
                    m_selectedProduct.m_bEnableGroup = true;
                    m_selectedProduct.group_price = groupPrice.group_price;
                    m_selectedProduct.group_price_id = groupPrice.group_id;
                }

                if(!existAlready(m_selectedProduct)) {
                    GlobalVariables.SELECTED_PRODUCTS.add(m_selectedProduct);
                }

                setListData();
                updateRate();
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

    public void getProduct(final String barcode) {
        showpDialog();
        ApiUtil.get_product(barcode, new Notify() {
            @Override
            public void onSuccess(Object object) {
                hidepDialog();
                ProductInfoResult data = (ProductInfoResult) object;
                m_selectedProduct = data.productInfo;
                if(!existAlready(m_selectedProduct)) {
                    m_selectedProduct.amount = 1;
                    GlobalVariables.SELECTED_PRODUCTS.add(m_selectedProduct);
                }

                setListData();
                updateRate();
            }

            @Override
            public void onAbort(Object object) {
                hidepDialog();
                ProductInfoResult data = (ProductInfoResult) object;
                GlobalFunctions.showAlertdialog(mContext, data.message);
            }

            @Override
            public void onFail() {

                hidepDialog();
            }
        });
    }

    /**
     * when scanned barcode, check if already get this product, if true, add on that, or, create new one.
     * */
    public boolean existAlready(ProductInfo productInfo) {
        for(int i = 0; i < GlobalVariables.SELECTED_PRODUCTS.size(); i++) {
            if(GlobalVariables.SELECTED_PRODUCTS.get(i).variation_id == productInfo.variation_id) {
                GlobalVariables.SELECTED_PRODUCTS.get(i).amount++;
                GlobalVariables.SELECTED_PRODUCTS.get(i).group_price = productInfo.group_price;
                GlobalVariables.SELECTED_PRODUCTS.get(i).m_bEnableGroup = productInfo.m_bEnableGroup;
                GlobalVariables.SELECTED_PRODUCTS.get(i).strGroup = productInfo.strGroup;
                GlobalVariables.SELECTED_PRODUCTS.get(i).group_price_id = productInfo.group_price_id;

                ((BaseAdapter) list.getAdapter()).notifyDataSetChanged();
                return true;
            }
        }
        return false;
    }

    /**
     * set selected product in the list
     */
    public void setListData() {
        mAdapter.refreshAdapter(GlobalVariables.SELECTED_PRODUCTS);

        if (GlobalVariables.SELECTED_PRODUCTS.size() > 0) {
            isProductLists(true);
        } else {
            isProductLists(false);
        }
    }
    public void updateRate() {
        if (GlobalVariables.SELECTED_PRODUCTS.size() > 0) {
            isProductLists(true);
        } else {
            isProductLists(false);
        }
        List<ProductInfo> totaldata = GlobalVariables.SELECTED_PRODUCTS;
        float totalv = 0.f;
        String currency_mark = "$";
        for (int i = 0; i < totaldata.size(); i++) {
            ProductInfo data1 = totaldata.get(i);
            currency_mark = GlobalVariables.BUSINESS_CURRENCY;
            totalvalue = GlobalFunctions.roundToDecimal(data1.amount * Float.parseFloat(data1.getUnitPrice()), 3);
            totalv = totalv + totalvalue;
        }
        GlobalVariables.TOTAL_PAIABLE_WITHOUT_DELIVERY = totalv;
        if( GlobalVariables.BUSINESS_IS_DELYVERY && totalv > 1 ) {
            totalv += GlobalFunctions.getDeliveryPrice() + GlobalFunctions.getDeliveryTax();
        }

        //if this is first time to add product in cart, show toast message.
        if( GlobalVariables.TOTAL_PRICE_TO_CHECK < 1 && totalv > 1 && GlobalVariables.BUSINESS_IS_DELYVERY ) {
            String strMsg = "Delivery fee: " + GlobalFunctions.getDeliveryPrice() + "\n";
            strMsg += "Delivery tax: " + GlobalFunctions.getDeliveryTax();
            Toast.makeText(ScanActivity.this, strMsg, Toast.LENGTH_LONG).show();
        }

        GlobalVariables.TOTAL_PRICE_TO_CHECK = totalv;
        fTotVal = totalv;
        totalRate.setText(String.format("%s%.2f", currency_mark, totalv));
        totalRate_.setText(String.format("%s%.2f", currency_mark, totalv));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                backtoStores();
                break;
            case R.id.barcode_scanner:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    startScan();
                } else {
                    checkPermissions();
                }
                break;
            case R.id.enterManually:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

                alertDialog.setTitle("Enter BarCode");

                final EditText input = new EditText(mContext);
                input.setGravity(Gravity.CENTER_HORIZONTAL);
                input.setSingleLine();
//                input.setText("6970091278616");
                alertDialog.setView(input);

                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        if (value.equals("")) {
                            GlobalFunctions.showToast(mContext, "Please input valid SKU.");
                            return;
                        }
                        getProduct(value);

                    }
                });

                alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //todo
                    }
                });

                alertDialog.show();
                break;
            case R.id.payButton:
            case R.id.btn_order:
                updateRate();
                if(GlobalVariables.TOTAL_PRICE_TO_CHECK >0){
                    AlertDialog pointAlertDialog = new ScanActivity.paymentPointAlertDialog(mContext, true);
                    pointAlertDialog.show();
                }else{
                    Toast toast=  Toast.makeText(getApplicationContext(), "Please Add the Products...", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP,0,150);
                    toast.show();
                }
                break;
                //showOrderDlg();
        }
    }

    /** Show point dialog */
    private class paymentPointAlertDialog extends AlertDialog {

        paymentPointAlertDialog(final Context context, boolean bCancelShow) {
            super(context);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.use_points_alert_dialog, null);
            setView(view);

            final String total_point = App.getInstance().getBalance();
            final TextView my_points = view.findViewById(R.id.my_points);
            my_points.setText(String.valueOf(Integer.parseInt(total_point) - GlobalVariables.USED_POINTS_TO_CHECK));

            TextView tv_current_rate = view.findViewById(R.id.current_rate);
            tv_current_rate.setText(String.format("1 bullet = %s%s", App.getInstance().getRatio(), GlobalVariables.BUSINESS_CURRENCY));

            final EditText payment_points = view.findViewById(R.id.payment_points);
            payment_points.setText(String.valueOf(GlobalVariables.USED_POINTS_TO_CHECK));

            final TextView currency_of_points = view.findViewById(R.id.currency_of_points);
            float calculated_currency = GlobalFunctions.roundToDecimal(Float.parseFloat(App.getInstance().getRatio()) * GlobalVariables.USED_POINTS_TO_CHECK, 6);
            currency_of_points.setText(String.format("= %s %s", String.valueOf(calculated_currency), GlobalVariables.BUSINESS_CURRENCY));

            used_point = GlobalVariables.USED_POINTS_TO_CHECK;
            payment_points.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String inputed_point = String.valueOf(payment_points.getText());
                    if (inputed_point.equals("")) inputed_point = "0";
                    if (Float.parseFloat(inputed_point) > Float.parseFloat(total_point)) {
                        GlobalFunctions.showToast(context, String.format("Your current bullet is %s.", total_point));
                        payment_points.setText(total_point);
                        return;
                    }

                    if (Float.parseFloat(App.getInstance().getRatio()) * Float.parseFloat(inputed_point) > GlobalVariables.TOTAL_PRICE_TO_CHECK) {
                        GlobalFunctions.showToast(context, String.format("The currency of bullets cannot exceed %s%s.", GlobalVariables.BUSINESS_CURRENCY, String.valueOf(GlobalVariables.TOTAL_PRICE_TO_CHECK)));
                        payment_points.setText("");
                        return;
                    }
                    my_points.setText(String.valueOf(Integer.parseInt(total_point) - Integer.parseInt(inputed_point)));
//                    float calculated_currency = GlobalFunctions.roundToDecimal(Float.parseFloat(GlobalVariables.POINT_CURRENCY_RATIO) * Float.parseFloat(inputed_point), 2);
                    float calculated_currency = GlobalFunctions.roundToDecimal(Float.parseFloat(App.getInstance().getRatio()) * Float.parseFloat(inputed_point), 6);
                    currency_of_points.setText(String.format("= %s %s", String.valueOf(calculated_currency), GlobalVariables.BUSINESS_CURRENCY));
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            LinearLayout btn_cash_pay = view.findViewById(R.id.btn_cash);
            LinearLayout btn_card_pay = view.findViewById(R.id.btn_ccard);
//            TextView btn_ok = view.findViewById(R.id.btn_confirm);
            TextView btn_cancel = view.findViewById(R.id.btn_cancel);

            btn_cash_pay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    used_point = String.valueOf(payment_points.getText()).equals("") ? 0: Integer.parseInt(String.valueOf(payment_points.getText()));
                    float calculated_currency = GlobalFunctions.roundToDecimal(Float.parseFloat(App.getInstance().getRatio()) * used_point, 6);
                    currency_used_point = String.valueOf(calculated_currency);

                    GlobalVariables.CURRENCY_OF_USED_POINTS_TO_CHECK = currency_used_point;
                    float total_price_to_check = GlobalVariables.TOTAL_PRICE_TO_CHECK- Float.parseFloat(currency_used_point);
                    GlobalVariables.TOTAL_PRICE_TO_CHECK = GlobalFunctions.roundToDecimal(total_price_to_check, 6);
                    GlobalVariables.USED_POINTS_TO_CHECK = used_point;
                    GlobalVariables.TOTAL_PAIABLE_WITHOUT_DELIVERY = GlobalVariables.TOTAL_PRICE_TO_CHECK;
                    if( GlobalVariables.BUSINESS_IS_DELYVERY ) {
                        GlobalVariables.TOTAL_PAIABLE_WITHOUT_DELIVERY = GlobalVariables.TOTAL_PRICE_TO_CHECK - GlobalFunctions.getDeliveryPrice() - GlobalFunctions.getDeliveryTax();
                    }
                    dismiss();
                    if(GlobalVariables.BUSINESS_IS_DELYVERY) {
                        GlobalVariables.BUSINESS_PAYMENT_TYPE = GlobalConstants.PAYMENT_CASH;
                        showOrderDlg();
                    }
                    else {
                        Intent cashCheckOut = new Intent(context, CashCheckOutActivity.class);
                        context.startActivity(cashCheckOut);
                        finish();
                    }
                }
            });

            btn_card_pay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    used_point = String.valueOf(payment_points.getText()).equals("") ? 0: Integer.parseInt(String.valueOf(payment_points.getText()));
                    float calculated_currency = GlobalFunctions.roundToDecimal(Float.parseFloat(App.getInstance().getRatio()) * used_point, 6);
                    currency_used_point = String.valueOf(calculated_currency);

                    GlobalVariables.CURRENCY_OF_USED_POINTS_TO_CHECK = currency_used_point;
                    float total_price_to_check = GlobalVariables.TOTAL_PRICE_TO_CHECK - Float.parseFloat(currency_used_point) ;//sgs
                    GlobalVariables.TOTAL_PRICE_TO_CHECK = GlobalFunctions.roundToDecimal(total_price_to_check, 6);
                    GlobalVariables.USED_POINTS_TO_CHECK = used_point;
                    GlobalVariables.TOTAL_PAIABLE_WITHOUT_DELIVERY = GlobalVariables.TOTAL_PRICE_TO_CHECK;
                    if( GlobalVariables.BUSINESS_IS_DELYVERY ) {
                        GlobalVariables.TOTAL_PAIABLE_WITHOUT_DELIVERY = GlobalVariables.TOTAL_PRICE_TO_CHECK - GlobalFunctions.getDeliveryPrice() - GlobalFunctions.getDeliveryTax();
                    }
                    dismiss();
                    GlobalVariables.BUSINESS_PAYMENT_TYPE = GlobalConstants.PAYMENT_CARD;
                    get_braintree_token();
                }
            });

            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

            if( !bCancelShow )
                view.findViewById(R.id.cancel_layout).setVisibility(View.GONE);
        }

    }
    /**
     * initializing variable to show
     * */

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void selectPaymentMethod() {
        AlertDialog alertDialog = new paymentAlertDialog(mContext);
        alertDialog.show();
    }

    private class paymentAlertDialog extends AlertDialog {

        paymentAlertDialog(final Context context) {
            super(context);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.payment_select_dialog, null);
            setView(view);
            LinearLayout cash_btn = view.findViewById(R.id.btn_cashpay);
            LinearLayout card_btn = view.findViewById(R.id.btn_cardpay);

            cash_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    Intent cashCheckOut = new Intent(context, CashCheckOutActivity.class);
                    context.startActivity(cashCheckOut);
                    finish();
                }
            });
            card_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    get_braintree_token();
                }
            });
        }

    }


    public void startScan() {
        Intent intent = new Intent(mContext, QRCodeActivity.class);

        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    // Confirm to leave store with clearing scanned products.
    public void backtoStores() {
        if(GlobalVariables.SELECTED_PRODUCTS.size() > 0) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

            alertDialog.setTitle(getString(R.string.leave_store));
            alertDialog.setMessage(getString(R.string.leave_store_msg));

            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    GlobalVariables.SELECTED_PRODUCTS.clear();
                    Intent intent = new Intent(mContext, FragmentsActivity.class);
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
        } else {
            Intent intent = new Intent(mContext, FragmentsActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //Increase or decrease selected item amount
    @Override
    public void onIncreaseClicked(ProductInfo productInfo) {
        mAdapter.notifyDataSetChanged();
        updateRate();
    }

    @Override
    public void onDecreaseClicked(ProductInfo productInfo) {
        mAdapter.notifyDataSetChanged();
        updateRate();
    }

    @Override
    public void onPromoChanged()
    {
        mAdapter.notifyDataSetChanged();
        updateRate();
    }

    //get braintree token to perform card payment

    public void get_braintree_token() {
        final AlertDialog processing = GlobalFunctions.showSpotDialog(mContext, getString(R.string.processing));
        ApiUtil.get_token(new Notify() {
            @Override
            public void onSuccess(Object object) {
                if (processing != null) processing.dismiss();
                ResponseData data = (ResponseData) object;
                GlobalVariables.BRAINTREE_TOKEN = data.message;
                if(data.code > 200 || data.amount >  GlobalVariables.TOTAL_PRICE_TO_CHECK ) { //if cannot use card for payment

//                    updateRate();

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    if( data.code > 200 )
                        builder.setMessage(getString(R.string.cardpay_error));
                    else
                        builder.setMessage("Minimum Total Payable amount must be equal to or greater than " + data.amount + ".");

                    builder.setPositiveButton("Close",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.setNegativeButton("Cash pay",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(GlobalVariables.BUSINESS_IS_DELYVERY) {
                                        GlobalVariables.BUSINESS_PAYMENT_TYPE = GlobalConstants.PAYMENT_CASH;
                                        showOrderDlg();
                                    }
                                    else {
                                        Intent cashCheckOut = new Intent(mContext, CashCheckOutActivity.class);
                                        mContext.startActivity(cashCheckOut);
                                        finish();
                                    }
//                                    Intent cashCheckOut = new Intent(mContext, CashCheckOutActivity.class);
//                                    mContext.startActivity(cashCheckOut);
//                                    finish();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                } else {
                    if(GlobalVariables.BUSINESS_IS_DELYVERY) {
                        showOrderDlg();
                    }
                    else {
                        Intent cardCheckOut = new Intent(mContext, CheckOutActivity.class);
                        startActivity(cardCheckOut);
                        finish();
                    }
                }

            }

            @Override
            public void onAbort(Object object) {
                if (processing != null) processing.dismiss();
                GlobalFunctions.showToast(mContext, getString(R.string.whoops));
            }

            @Override
            public void onFail() {
                if (processing != null) processing.dismiss();
                GlobalFunctions.showToast(mContext, getString(R.string.whoops));
            }
        });
    }

    /**
     * Return the current state of the permissions needed.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<>();
        String[] REQUIRED_SDK_PERMISSIONS = new String[] {Manifest.permission.CAMERA};
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(mContext, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (!missingPermissions.isEmpty()) {
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(mContext, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            mContext.onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }

    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        checkPermissions();
                        return;
                    }
                }
                startScan();
                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pos, menu);
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

            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_barcode:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    startScan();
                } else {
                    checkPermissions();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    getProduct(data.getStringExtra("barcode"));
                }
            } else {
                Toast.makeText(mContext, R.string.barcode_error, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == RC_CHECKOUT) {
            if (resultCode == Activity.RESULT_OK) {
                m_Clean = false;
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed(){
        backtoStores();
    }


    private void showGroupDlg()
    {
        m_GroupDlg = new Dialog(this);
        m_GroupDlg.setContentView(R.layout.dialog_group_layout);
        m_GroupDlg.setTitle("Select promo code...");
        m_GroupDlg.setCanceledOnTouchOutside(false);
        final RadioGroup radioGroup = m_GroupDlg.findViewById(R.id.radio_group);
        int i = 0;
        for( String strGroup : GlobalVariables.BUSINESS_DEFAULT_PRICE_GROUPS )
        {
            RadioButton btn_group = new RadioButton(getApplicationContext());
            if( i == 0 )
                btn_group.setChecked(true);
            btn_group.setText(strGroup);
            btn_group.setId(i);
            i++;
            radioGroup.addView(btn_group);
        }

        Button btn_ok = m_GroupDlg.findViewById(R.id.btn_ok);
        Button btn_cancel = m_GroupDlg.findViewById(R.id.btn_cancel);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int nCheckedId = radioGroup.getCheckedRadioButtonId();
                m_selectedProduct.strGroup = GlobalVariables.BUSINESS_DEFAULT_PRICE_GROUPS.get(nCheckedId);
                m_GroupDlg.dismiss();
                getGroupPrice();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_GroupDlg.dismiss();

                if(!existAlready(m_selectedProduct)) {
                    GlobalVariables.SELECTED_PRODUCTS.add(m_selectedProduct);
                }

                setListData();
                updateRate();
            }
        });

        m_GroupDlg.show();
    }


    private void showMethodDlg()
    {
        m_MethodDlg = new Dialog(this);
        m_MethodDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        m_MethodDlg.setContentView(R.layout.dialog_method_layout);
        m_MethodDlg.setCancelable(false);
        final RadioGroup radioGroup = m_MethodDlg.findViewById(R.id.radio_method);

        Button btn_ok = m_MethodDlg.findViewById(R.id.btn_ok);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_MethodDlg.dismiss();
                int nCheckedId = radioGroup.getCheckedRadioButtonId();
                if( nCheckedId == R.id.radio_delivery )
                {
                    GlobalVariables.BUSINESS_IS_DELYVERY = true;
                    showDeliveryView(true);

                    getDeliveryProducts();
                }
                else
                {
                    GlobalVariables.BUSINESS_IS_DELYVERY = false;
                    if(GlobalVariables.BUSINESS_TYPE == GlobalConstants.BUSINESS_TYPE_RESTAURANT)
                    {
                        showDineinView();
                        getAllProducts();
                        getTables();
                    } else {
                        showDeliveryView(false);
                    }
                }
            }
        });

        m_MethodDlg.show();
    }

    private void showTableDlg()
    {
        if(m_resTables == null || m_resTables.tables == null || m_resTables.tables.size() < 1)
            return;
        final Dialog tableDlg = new Dialog(this);
        tableDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        tableDlg.setCancelable(false);
        tableDlg .setContentView(R.layout.dialog_table_layout);
        Button btn_ok = tableDlg.findViewById(R.id.btn_ok);
        final RadioGroup radioGroup = tableDlg.findViewById(R.id.radio_table_group);
        for(int i = 0 ; i < m_resTables.tables.size() ; i ++ )
        {
            AppCompatRadioButton radioButton = new AppCompatRadioButton (this);
            if( i == 0 )
                radioButton.setChecked(true);
            radioButton.setText(m_resTables.tables.get(i).name);
            radioButton.setId(i);
            radioButton.setTextColor(Color.parseColor("#ffffff"));
            if(Build.VERSION.SDK_INT>=21)
            {
                ColorStateList colorStateList = new ColorStateList(
                        new int[][]{
                                new int[]{-android.R.attr.state_enabled}, //disabled
                                new int[]{android.R.attr.state_enabled} //enabled
                        },
                        new int[] {
                                Color.BLACK //disabled
                                ,Color.WHITE //enabled
                        }
                );
                radioButton.setButtonTintList(colorStateList);//set the color tint list
                radioButton.invalidate(); //could not be necessary
            }
            radioGroup.addView(radioButton);
        }
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int nCheckedId = radioGroup.getCheckedRadioButtonId();
                GlobalVariables.BUSINESS_RESTAURANT_TABLE = m_resTables.tables.get(nCheckedId).id;
                tableDlg.dismiss();
            }
        });
        tableDlg.getWindow().setLayout(((GlobalFunctions.getWidth(this) / 100) * 90), LinearLayout.LayoutParams.WRAP_CONTENT);

        tableDlg.show();
    }
    private void showOrderDlg()
    {
        m_OrderDlg = new Dialog(this);
        m_OrderDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        m_OrderDlg.setContentView(R.layout.dialog_order_layout);
        final EditText txt_user_name = m_OrderDlg.findViewById(R.id.txt_user_name);
        final EditText txt_user_email = m_OrderDlg.findViewById(R.id.txt_user_email);
        final EditText txt_user_phone = m_OrderDlg.findViewById(R.id.txt_user_phone);
        final EditText txt_area_address = m_OrderDlg.findViewById(R.id.txt_area_address);
        final Spinner spinner_area = m_OrderDlg.findViewById(R.id.spinner_area);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.custom_spinner_item, GlobalVariables.BUSSINESS_DELIVERY_AREA);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_area.setAdapter(adapter);
        Button btn_ok = m_OrderDlg.findViewById(R.id.btn_ok);
        Button btn_cancel = m_OrderDlg.findViewById(R.id.btn_cancel);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( txt_user_name.getText().toString().length() < 1 ) {
                    Toast.makeText(ScanActivity.this, "Insert your name", Toast.LENGTH_LONG).show();
                    txt_user_name.requestFocus();
                    return;
                }
                if( txt_user_email.getText().toString().length() < 1 || !txt_user_email.getText().toString().contains("@")) {
                    Toast.makeText(ScanActivity.this, "Insert your email correctly", Toast.LENGTH_LONG).show();
                    txt_user_email.requestFocus();
                    return;
                }
                if( txt_user_phone.getText().toString().length() < 1 ) {
                    Toast.makeText(ScanActivity.this, "Insert your phone number", Toast.LENGTH_LONG).show();
                    txt_user_phone.requestFocus();
                    return;
                }
                if( txt_area_address.getText().toString().length() < 1 ) {
                    Toast.makeText(ScanActivity.this, "Insert your address", Toast.LENGTH_LONG).show();
                    txt_area_address.requestFocus();
                    return;
                }

                //Send requirement delivery to server.
                String strArea = spinner_area.getSelectedItem().toString();
                //minimum is available and total price is bigger than minimum value, then delivery price and tax is 0
                try {
                    GlobalVariables.ORDER_DETAIL = new JSONObject();
                    JSONArray delivery_details = new JSONArray();
                    int i = 0;
                    String strPayment = "Cash";
                    if(GlobalVariables.BUSINESS_PAYMENT_TYPE == GlobalConstants.PAYMENT_CARD)
                        strPayment = "Card";

                    for(i = 0 ; i < GlobalVariables.SELECTED_PRODUCTS.size(); i ++ )
                    {
                        ProductInfo productInfo = GlobalVariables.SELECTED_PRODUCTS.get(i);
                        if( productInfo.amount < 1 )
                            continue;
                        JSONObject product = new JSONObject();
                        product.put("product_name", productInfo.name);
                        product.put("product_id", productInfo.id);
                        product.put("variation_id", productInfo.variation_id);
                        product.put("business_id", GlobalVariables.BUSINESS_ID);
                        product.put("location_id",  GlobalVariables.LOCATION_ID);
                        product.put("tax_id", productInfo.tax);
                        product.put("product_quantity", productInfo.amount);
                        product.put("user_name", txt_user_name.getText().toString());
                        product.put("user_phone", txt_user_phone.getText().toString());
                        product.put("user_email", txt_user_email.getText().toString());
                        product.put("order_area", strArea);
                        product.put("order_area_address", txt_area_address.getText().toString());
                        product.put("sub_total", GlobalFunctions.roundToDecimal(productInfo.amount * Float.parseFloat(productInfo.getDefault_sell_price()), 3));
                        product.put("tax", productInfo.getTax());
                        product.put("discount", productInfo.getDiscount());
                        product.put("delivery_tax", GlobalFunctions.getDeliveryTax());
                        product.put("delivery_price", GlobalFunctions.getDeliveryPrice());
                        product.put("unit_price", productInfo.getDefault_sell_price());
                        product.put("unit_price_inc_tax", productInfo.getUnitPrice());
                        product.put("points", GlobalVariables.USED_POINTS_TO_CHECK );
                        product.put("payment_method", strPayment );
                        if(productInfo.m_bEnableGroup)
                            product.put("selling_group", productInfo.strGroup);
                        else
                            product.put("selling_group", "Default Selling Price");

                        delivery_details.put(product);
                    }
                    GlobalVariables.ORDER_DETAIL.put("delivery_details", delivery_details);
//                    orderDelivery(jsonOrder);
//                    m_OrderDlg.setCancelable(false);
                    Intent cashCheckOut = new Intent(ScanActivity.this, CashCheckOutActivity.class);
                    startActivityForResult(cashCheckOut, RC_CHECKOUT);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                m_OrderDlg.dismiss();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_OrderDlg.dismiss();
            }
        });
        m_OrderDlg.getWindow().setLayout(((GlobalFunctions.getWidth(this) / 100) * 90), LinearLayout.LayoutParams.WRAP_CONTENT);
        m_OrderDlg.show();
    }

    //in the case of restaurant, read all products
    public void getAllProducts()
    {
        showpDialog();
        ApiUtil.get_all_products( new Notify() {
            @Override
            public void onSuccess(Object object) {
                hidepDialog();
                DeliveryProductInfoResult data = (DeliveryProductInfoResult) object;
                GlobalVariables.SELECTED_PRODUCTS = data.products;
                setListData();
                updateRate();
            }

            @Override
            public void onAbort(Object object) {
                hidepDialog();
                DeliveryProductInfoResult data = (DeliveryProductInfoResult) object;
                GlobalFunctions.showAlertdialog(mContext, data.message);
            }

            @Override
            public void onFail() {
                hidepDialog();
            }
        });
    }

    public void getTables()
    {
        ApiUtil.get_tables( new Notify() {
            @Override
            public void onSuccess(Object object) {
                hidepDialog();
                m_resTables = (TableInfo) object;
                //Show select groups
                showTableDlg();
            }

            @Override
            public void onAbort(Object object) {
                DeliveryProductInfoResult data = (DeliveryProductInfoResult) object;
                GlobalFunctions.showAlertdialog(mContext, data.message);
            }

            @Override
            public void onFail() {
            }
        });
    }


    public void getDeliveryProducts() {
        showpDialog();
        ApiUtil.get_delivery_products( new Notify() {
            @Override
            public void onSuccess(Object object) {
                hidepDialog();
                DeliveryProductInfoResult data = (DeliveryProductInfoResult) object;
                GlobalVariables.SELECTED_PRODUCTS = data.products;
                setListData();
                updateRate();
            }

            @Override
            public void onAbort(Object object) {
                hidepDialog();
                DeliveryProductInfoResult data = (DeliveryProductInfoResult) object;
                GlobalFunctions.showAlertdialog(mContext, data.message);
            }

            @Override
            public void onFail() {
                hidepDialog();
            }
        });
    }
}
