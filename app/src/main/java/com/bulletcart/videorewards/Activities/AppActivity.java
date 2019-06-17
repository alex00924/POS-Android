package com.bulletcart.videorewards.Activities;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.HashMap;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.Map;

import com.bulletcart.videorewards.Global.GlobalConstants;
import com.bulletcart.videorewards.R;
import com.bulletcart.videorewards.Services.MyFirebaseMessagingService;
import com.bulletcart.videorewards.app.App;
import com.bulletcart.videorewards.Utils.AppUtils;
import com.bulletcart.videorewards.Utils.CustomRequest;
import com.bulletcart.videorewards.Utils.Dialogs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by DroidOXY
 */

public class AppActivity extends ActivityBase {

    Button loginBtn, signupBtn, posBtn;
    LinearLayout loadingScreen;
    LinearLayout contentScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

       contentScreen = findViewById(R.id.contentScreen);
        loadingScreen = findViewById(R.id.loadingScreen);

        if (App.getInstance().get("isFirstTimeLaunch",true) )
        {
           if( GlobalConstants.ENABLE_APP_INTRO) {
               startActivity(new Intent(this, IntroActivity.class));
               finish();
           }
        }

        loginBtn = findViewById(R.id.loginBtn);
        signupBtn = findViewById(R.id.signupBtn);
        posBtn = findViewById(R.id.posBtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            startActivity(new Intent(AppActivity.this, LoginActivity.class));
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            startActivity(new Intent(AppActivity.this, SignupActivity.class));

            }
        });
        posBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(AppActivity.this, MainActivity.class));

            }
        });

        App.getInstance().getCountryCode();
    }

    @Override
    protected void onStart() {

        super.onStart();

        if(!App.getInstance().isConnected()) {

            showLoadingScreen();

            Dialogs.warningDialog(this, getResources().getString(R.string.title_network_error), getResources().getString(R.string.msg_network_error), false, false, "", getResources().getString(R.string.retry), new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    onStart();
                }
            });

        }else if(App.getInstance().getId() != 0) {

            showLoadingScreen();

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, GlobalConstants.METHOD_ACCOUNT_AUTHORIZE, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            if (App.getInstance().authorize(response)) {

                                if (App.getInstance().getState() == GlobalConstants.ACCOUNT_STATE_ENABLED) {

                                    // AppInit();
                                    startMain();
//                                    ActivityCompat.finishAffinity(AppActivity.this);
//                                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
//                                    startActivity(i);

                                } else {

                                    showContentScreen();
                                    App.getInstance().logout();
                                }

                            } else if(App.getInstance().getErrorCode() == 699 || App.getInstance().getErrorCode() == 999){
                                Dialogs.validationError(AppActivity.this,App.getInstance().getErrorCode());

                            } else if(App.getInstance().getErrorCode() == 799){
                                Dialogs.warningDialog(AppActivity.this, getResources().getString(R.string.update_app), getResources().getString(R.string.update_app_description), false, false, "", getResources().getString(R.string.update), new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        AppUtils.gotoMarket(AppActivity.this);
                                    }
                                });

                            } else {
                                startMain();
                                showContentScreen();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    App.getInstance().removeData();
                    App.getInstance().readData();
                    startMain();
                    showContentScreen();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("data", App.getInstance().getAuthorize());

                    return params;
                }
            };

            App.getInstance().addToRequestQueue(jsonReq);

        } else {
            App.getInstance().removeData();
            App.getInstance().readData();
            startAuth();
//            showContentScreen();

        }
    }

    public void startMain() {
        ActivityCompat.finishAffinity(AppActivity.this);
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }

    public void startAuth() {
        ActivityCompat.finishAffinity(AppActivity.this);
//        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        Intent i2 = new Intent(getApplicationContext(), LoginActivity.class);
//        startActivity(i);
        startActivity(i2);
    }

    public void showContentScreen() {

        loadingScreen.setVisibility(View.GONE);

//        contentScreen.setVisibility(View.VISIBLE);
    }

    public void showLoadingScreen() {

//        contentScreen.setVisibility(View.GONE);

        loadingScreen.setVisibility(View.VISIBLE);
    }
}
