package com.bulletcart.videorewards.Global;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bulletcart.videorewards.Activities.MainActivity;
import com.bulletcart.videorewards.R;
import com.bulletcart.videorewards.Utils.CustomRequest;
import com.bulletcart.videorewards.Utils.Notify;
import com.bulletcart.videorewards.app.App;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.callback.Callback;

import dmax.dialog.SpotsDialog;
import eu.giovannidefrancesco.easysharedprefslib.IStorage;

import static com.bulletcart.videorewards.Global.GlobalConstants.ACCOUNT_BALANCE;
import static com.bulletcart.videorewards.Global.GlobalConstants.UPDATE_BALANCE_AFTER_POS;

public class GlobalFunctions {
    public static void vibrate(Context context, long duration) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assert v != null;
            v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
        }else{
            //deprecated in API 26
            assert v != null;
            v.vibrate(300);
        }
    }
    private static ProgressDialog mProgressDialog;
    private static AlertDialog.Builder alertDialogBuilder;

    public static AlertDialog showSpotDialog(Context context, String title) {
        final AlertDialog processing = new SpotsDialog(context, R.style.CustomProgress);
        processing.setCancelable(false);
        processing.show();
        return processing;
    }

    public static void showAlertdialog(final Context context, String message) {
        alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    public static void showProgressDiaog(Context context) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
        } else {
            mProgressDialog = new ProgressDialog(context, ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(context.getString(R.string.waiting_msg));
            mProgressDialog.show();
        }
    }

    public static void hideProgressDialog() {
        try {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
        catch (Exception ignored) {
        }
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static float roundToDecimal(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public static ProgressDialog showClassicProgressDialog(Context context, String title, String message) {
        ProgressDialog progress;
        progress = new ProgressDialog(context, android.R.style.Theme_DeviceDefault_Dialog);
            progress.setCancelable(false);
            progress.setMessage(message);
            progress.setTitle(title);
            progress.show();
            return progress;
    }

    public static void hideClassicProgressDialog(ProgressDialog progressDialog) {
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public static void updateMyPoints(final Context context, final Notify notify) {

        final int used_points = App.getInstance().getUsedPoints();
        if (used_points > 0) {
            CustomRequest balanceRequest = new CustomRequest(Request.Method.POST, UPDATE_BALANCE_AFTER_POS,null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try{
                                if(!response.getBoolean("error")){

                                    MainActivity.setOptionTitle(context.getString(R.string.app_currency).toUpperCase()+" : " +response.getString("user_balance"));
                                    App.getInstance().store("balance",response.getString("user_balance"));
                                    App.getInstance().setUsedPoints(0);
                                    if (notify != null) {
                                        notify.onSuccess(null);
                                    }
                                }

                            }catch (Exception e){
                                // do nothing
                            }

                        }},new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                  int nnn = 0;
                }}){
                @Override
                protected Map<String,String> getParams(){
                    Map<String,String> params = new HashMap<>();
                    params.put("data", App.getInstance().getDataCustom("",String.valueOf(used_points)));
                    return params;
                }
            };

            App.getInstance().addToRequestQueue(balanceRequest);
        }
    }

    public static void getCurrencyRatio(){

        if( GlobalVariables.BUSINESS_CURRENCY_STRING.equalsIgnoreCase(GlobalVariables.BUSINESS_MERCHANT_CURRENCY) )
        {
            GlobalVariables.CURRENCY_RATIO_TO_MERCHANT = 1;
            return;
        }
        String api_url = "http://apilayer.net/api/live?access_key=27bb6c27a91ca59d7a9a17cd6bd3d62f&currencies=" + GlobalVariables.BUSINESS_CURRENCY_STRING +  "," + GlobalVariables.BUSINESS_MERCHANT_CURRENCY + "&source=usd&format=1";
        CustomRequest balanceRequest = new CustomRequest(Request.Method.GET, api_url,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            String status = response.getString("success");
                            if(status.equals("true")) {
                                JSONObject currencied = new JSONObject(response.getString("quotes"));
                                String org_rate = currencied.getString("USD" + GlobalVariables.BUSINESS_CURRENCY_STRING);
                                String target_rate = currencied.getString("USD" + GlobalVariables.BUSINESS_MERCHANT_CURRENCY);
                                GlobalVariables.CURRENCY_RATIO_TO_MERCHANT = Float.valueOf(target_rate) / Float.valueOf(org_rate);
//
//                                String currency2usd_string = response.getString("quotes");
//
//                                currency2usd_string = currency2usd_string.split(":")[1];
//                                currency2usd_string = currency2usd_string.replace("}", "0");
//                                float currency2usd = GlobalFunctions.roundToDecimal(1 / Float.parseFloat(currency2usd_string), 6);
//
//                                GlobalVariables.CURRENCY_RATIO_TO_MERCHANT = currency2usd;
                            }

                        } catch (JSONException e) { //e.printStackTrace();
                        }

                    }},new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {}});

        App.getInstance().addToRequestQueue(balanceRequest);

    }
}
