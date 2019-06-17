package com.bulletcart.videorewards.Activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.toolbox.StringRequest;
import com.bulletcart.videorewards.Global.GlobalConstants;
import com.bulletcart.videorewards.R;
import com.bulletcart.videorewards.Services.MyFirebaseInstanceIDService;
import com.bulletcart.videorewards.Services.MyFirebaseMessagingService;
import com.bulletcart.videorewards.app.App;
import com.bulletcart.videorewards.Utils.CustomRequest;
import com.bulletcart.videorewards.Utils.Dialogs;
import com.bulletcart.videorewards.Utils.Helper;
import com.google.android.gms.auth.api.Auth;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import static android.provider.ContactsContract.Intents.Insert.EMAIL;

/**
 * Created by DroidOXY
 */

public class LoginActivity extends ActivityBase implements GoogleApiClient.OnConnectionFailedListener{


    Button signinBtn;
    EditText signinUsername, signinPassword;
    TextView mActionForgot, mActionSignup;
    private static final int RC_SIGN_IN = 007;

    private CallbackManager mCallbackManager;

    private GoogleApiClient mGoogleApiClient;

    private SignInButton btnSignIn;
    String username, password, user_name;
    String str;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {

                        if (task.isSuccessful())
                            str = task.getResult().getToken();
                        int nn = 0;
                    }
                });
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);

        signinUsername = findViewById(R.id.signinUsername);
        signinPassword = findViewById(R.id.signinPassword);

        signinBtn = findViewById(R.id.signinBtn);

        mActionForgot = findViewById(R.id.actionForgot);
        mActionSignup = findViewById(R.id.action_sign_up);

        if(!GlobalConstants.ENABLE_EMAIL_LOGIN) {

            signinUsername.setVisibility(View.GONE);
            signinPassword.setVisibility(View.GONE);
            signinBtn.setVisibility(View.GONE);
            mActionForgot.setVisibility(View.GONE);
        }

        signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                username = signinUsername.getText().toString();
                password = signinPassword.getText().toString();

                if (!App.getInstance().isConnected()) {

                    Toast.makeText(getApplicationContext(), R.string.msg_network_error, Toast.LENGTH_SHORT).show();

                } else if (!checkUsername() || !checkPassword()) {


                } else {

                    signin();
                }
            }
        });

        mActionForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getApplicationContext(), RecoveryActivity.class);
                startActivity(i);
            }
        });

        mActionSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(i);
            }
        });

        App.getInstance().getCountryCode();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        btnSignIn = findViewById(R.id.btn_sign_in);
        if(!GlobalConstants.ENABLE_GMAIL_LOGIN) {

            btnSignIn.setVisibility(View.GONE);

        }

        // Customizing G+ button
        btnSignIn.setSize(SignInButton.SIZE_STANDARD);
        btnSignIn.setScopes(gso.getScopeArray());

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        // facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        mCallbackManager = CallbackManager.Factory.create();

        LoginButton mLoginButton = findViewById(R.id.login_button);
        if(!GlobalConstants.ENABLE_FACEBOOK_LOGIN){
            mLoginButton.setVisibility(View.GONE);
        }

        mLoginButton.setReadPermissions(Arrays.asList(EMAIL));

        // Register a callback to respond to the user
        mLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                setResult(RESULT_OK);

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject jsonObject,GraphResponse response) {
                                try {

                                    String name = jsonObject.getString("name");
                                    String email =  jsonObject.getString("email");
                                    String id =  jsonObject.getString("id");

                                    signin_auto(getUsername(email));

                                } catch(JSONException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();

                // finish();
            }

            @Override
            public void onCancel() {
                setResult(RESULT_CANCELED);

                Toast.makeText(getApplicationContext(), "user cancelled", Toast.LENGTH_LONG).show();


                // finish();
            }

            @Override
            public void onError(FacebookException e) {
                // Handle exception

                Toast.makeText(getApplicationContext(), "some error : " + e, Toast.LENGTH_LONG).show();

            }
        });


        if (App.getInstance().get("isNotified",true) )
        {
            pushNotify();
            App.getInstance().store("isNotified",false);
        }


    }

    private void pushNotify()
    {
        String channel_name = "FIREBASE_NOTI";
        String channel_description = "PUSH";
        String CHANNEL_ID = "NOTIFICATION";
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channel_name, importance);
            channel.setDescription(channel_description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        notificationBuilder
                .setSmallIcon(R.drawable.ic_stat_shopping_cart)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Experience the new way of shopping/ordering."))
                .setContentTitle("Hi, it’s Bullet Cart.")
                .setContentText("Experience the new way of shopping/ordering.")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(defaultSoundUri);
        Intent appintent = new Intent(this, AppActivity.class);
        appintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent appIntent = PendingIntent.getActivity(this, 0, appintent,PendingIntent.FLAG_ONE_SHOT);
        notificationBuilder.setContentIntent(appIntent);
        notificationBuilder.setColor(Color.RED);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(App.getInstance().get("noid",10)+1 , notificationBuilder.build());
    }

    private void handleSignInResult(GoogleSignInResult result) {

        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            if(GlobalConstants.DEBUG_MODE){Log.e(TAG, "display name: " + acct.getDisplayName());}

            String personName = acct.getDisplayName();
            // String personPhotoUrl = acct.getPhotoUrl().toString();
            String email = acct.getEmail();

            signin_auto(getUsername(email));

        } else {
            // Signed out, show unauthenticated UI.
            // show toast
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }else{
            mCallbackManager.onActivityResult(requestCode, resultCode, data);

        }
    }

    @Override
    public void onStart() {
        super.onStart();

//        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
//        if (opr.isDone()) {
//            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
//            // and the GoogleSignInResult will be available instantly.
//            if(DEBUG_MODE){Log.d(TAG, "Got cached sign-in");}
//            GoogleSignInResult result = opr.get();
//            handleSignInResult(result);
//        } else {
//            // If the user has not previously signed in on this device or the sign-in has expired,
//            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
//            // single sign-on will occur in this branch.
//            showpDialog();
//            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
//                @Override
//                public void onResult(GoogleSignInResult googleSignInResult) {
//                    hidepDialog();
//                    handleSignInResult(googleSignInResult);
//                }
//            });
//        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        if(GlobalConstants.DEBUG_MODE){ Log.d(TAG, "onConnectionFailed:" + connectionResult);}
    }

    String getUsername(String email){

        String user_name_text = email.substring(0,email.lastIndexOf("@"));

        if(user_name_text.contains(".")){
            user_name = user_name_text.replace(".", "");
        }else{
            user_name = user_name_text;
        }

        return user_name;
    }

    public void signin() {

        showpDialog();


        StringRequest jsonReq = new StringRequest(Request.Method.POST, GlobalConstants.METHOD_ACCOUNT_LOGIN,
                new Response.Listener<String >() {
                    @Override
                    public void onResponse(String  response) {

                        JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(response);

                            if (App.getInstance().authorize(jsonObject)) {

                                if (App.getInstance().getState() == GlobalConstants.ACCOUNT_STATE_ENABLED) {

                                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(i);

                                    ActivityCompat.finishAffinity(LoginActivity.this);

                                } else {

                                    App.getInstance().logout();
                                    Toast.makeText(LoginActivity.this, getText(R.string.msg_account_blocked), Toast.LENGTH_SHORT).show();
                                }

                            } else if(App.getInstance().getErrorCode() == 699 || App.getInstance().getErrorCode() == 999){

                                Dialogs.validationError(LoginActivity.this,App.getInstance().getErrorCode());

                            } else {

                                Toast.makeText(getApplicationContext(), getString(R.string.error_signin), Toast.LENGTH_SHORT).show();
                            }

                            hidepDialog();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(), getText(R.string.error_data_loading), Toast.LENGTH_LONG).show();

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("password", password);
                params.put("clientId", GlobalConstants.CLIENT_ID);

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void signin_auto(final String user_name) {

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, GlobalConstants.METHOD_ACCOUNT_LOGIN, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (App.getInstance().authorize(response)) {

                            if (App.getInstance().getState() == GlobalConstants.ACCOUNT_STATE_ENABLED) {

                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(i);

                                ActivityCompat.finishAffinity(LoginActivity.this);

                            } else {

                                App.getInstance().logout();
                                Toast.makeText(LoginActivity.this, getText(R.string.msg_account_blocked), Toast.LENGTH_SHORT).show();
                            }

                        } else if(App.getInstance().getErrorCode() == 699 || App.getInstance().getErrorCode() == 999){

                            Dialogs.validationError(LoginActivity.this,App.getInstance().getErrorCode());

                        } else {

                            Toast.makeText(getApplicationContext(), getString(R.string.error_signin), Toast.LENGTH_SHORT).show();
                        }

                        hidepDialog();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(), getText(R.string.error_data_loading), Toast.LENGTH_LONG).show();

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", user_name);
                params.put("password", user_name);
                params.put("clientId", GlobalConstants.CLIENT_ID);

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public Boolean checkUsername() {

        username = signinUsername.getText().toString();

        signinUsername.setError(null);

        Helper helper = new Helper();

        if (username.length() == 0) {

            signinUsername.setError(getString(R.string.error_field_empty));

            return false;
        }

        return  true;
    }

    public Boolean checkPassword() {

        password = signinPassword.getText().toString();

        signinPassword.setError(null);

        if (username.length() == 0) {

            signinPassword.setError(getString(R.string.error_field_empty));

            return false;
        }

        return  true;
    }

    @Override
    public void onBackPressed(){

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
}
