package com.bulletcart.videorewards.Activities;

// import statements
import java.util.Map;
import java.util.Timer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.os.Bundle;
import java.util.HashMap;
import android.os.Handler;
import java.util.TimerTask;

import android.view.MenuItem;
import android.content.Intent;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.support.v7.widget.Toolbar;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;

// External import statements
import android.app.AlertDialog;
import android.widget.Toast;

import cn.pedant.SweetAlert.SweetAlertDialog;
import dmax.dialog.SpotsDialog;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

// Google import statements
import com.bulletcart.videorewards.Fragments.VideosFragment;
import com.bulletcart.videorewards.Global.GlobalConstants;
import com.bulletcart.videorewards.Global.GlobalFunctions;
import com.bulletcart.videorewards.Model.Videos;
import com.bulletcart.videorewards.R;
import com.bulletcart.videorewards.Services.MyFirebaseInstanceIDService;
import com.bulletcart.videorewards.Services.MyFirebaseMessagingService;
import com.bulletcart.videorewards.Utils.Notify;
import com.bulletcart.videorewards.Views.CountDownTimerView;
import com.bulletcart.videorewards.Utils.Dialogs;
import com.google.android.gms.ads.InterstitialAd;

// import statements
import com.bulletcart.videorewards.app.App;
import com.bulletcart.videorewards.Utils.AppUtils;
import com.bulletcart.videorewards.Utils.CustomRequest;
import com.bulletcart.videorewards.Utils.UtilsMiscellaneous;
import com.bulletcart.videorewards.Utils.SlidingTabLayout;
import com.bulletcart.videorewards.Adapters.ViewPagerAdapter;
import com.bulletcart.videorewards.Views.ScrimInsetsFrameLayout;

import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.startapp.android.publish.adsCommon.StartAppAd;
import com.startapp.android.publish.adsCommon.StartAppSDK;

import com.thefinestartist.ytpa.YouTubePlayerActivity;
import com.thefinestartist.ytpa.enums.Orientation;
import com.thefinestartist.ytpa.utils.YouTubeUrlParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import javax.security.auth.callback.Callback;


/**
 * Created by DroidOXY
 */

public class MainActivity extends ActivityBase implements View.OnClickListener {

    // View Variables
    private static Menu menu;
    ViewPagerAdapter adapter;
    MainActivity context;
    private RewardedVideoAd mAd;
    ProgressDialog progressDialog ;
    private InterstitialAd interstitial;
    public boolean doubleBackToExitPressedOnce = false;
    public int REQUEST_CODE_YOUTUBE_PLAY = 999;
    ViewPager pager;
    FrameLayout redeem, legal_policy, about, help, reward_his, daily_check_in, pos, sign_in, sign_up, sign_out;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        init_v3();
    }

    public void displayInterstitial() {
        if (interstitial.isLoaded()) {
            interstitial.show();
        }
    }

    void init_v3(){

        initViews();
        initNavDrawer();

        init_startapp();

        checkUsedPoints();

    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshDisplay();

    }

    // Main functions
    void openWebVideos(){
        pager.setCurrentItem(1);
//        Intent webvids = new Intent(context, FragmentsActivity.class);
//        webvids.putExtra("show","webvids");
//        startActivityForResult(webvids,1);

    }


    void openAbout(){
        startActivity(new Intent(context, AboutActivity.class));
    }

    void openTransactions(){

        Intent transactions = new Intent(context, FragmentsActivity.class);
        transactions.putExtra("show","transactions");
        startActivity(transactions);
    }

    void openRedeem(){
        Intent redeem = new Intent(context, FragmentsActivity.class);
        redeem.putExtra("show","redeem");
        startActivityForResult(redeem,1);
    }
    void parseURL(String url){
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        context.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.points:

//                openRedeem();

                return true;

            case R.id.sync:
                if(App.getInstance().getId() == 0) {
                    GlobalFunctions.showToast(context, getString(R.string.not_logged_in));
                } else {
                    updateBalance();
                }

            default:
                return super.onOptionsItemSelected(item);
        }
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

    public void updateBalance() {
        final AlertDialog updating = new SpotsDialog(context, R.style.Custom);
        updating.show();
        updateBalanceInBg();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                updating.dismiss();
            }
        }, 1000);
    }

    public void updateBalanceInBg() {

        CustomRequest balanceRequest = new CustomRequest(Request.Method.POST, GlobalConstants.ACCOUNT_BALANCE,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try{

                            if(!response.getBoolean("error")){

                                setOptionTitle(getString(R.string.app_currency).toUpperCase()+" : " +response.getString("user_balance"));
                                App.getInstance().store("balance",response.getString("user_balance"));

                                //update nav
                                refreshDisplay();

                            }else if(response.getInt("error_code") == 699 || response.getInt("error_code") == 999){

                                Dialogs.validationError(context,response.getInt("error_code"));

                            }else if(response.getInt("error_code") == 799) {

                                Dialogs.warningDialog(context, getResources().getString(R.string.update_app), getResources().getString(R.string.update_app_description), false, false, "", getResources().getString(R.string.update), new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        AppUtils.gotoMarket(context);
                                    }
                                });

                            }

                        }catch (Exception e){
                            // do nothin
                        }

                    }},new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {}}){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("data", App.getInstance().getData());
                return params;
            }
        };

        App.getInstance().addToRequestQueue(balanceRequest);
    }

    // Linked Functions

    void award(final int Points, final String CreditType){

        CustomRequest rewardRequest = new CustomRequest(Request.Method.POST, GlobalConstants.ACCOUNT_REWARD,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try{

                            JSONObject Response = new JSONObject(App.getInstance().deData(response.toString()));

                            if(!Response.getBoolean("error") && Response.getInt("error_code") == GlobalConstants.ERROR_SUCCESS){

                                // User Rewarded Successfully
                                AppUtils.toastShort(context,Points + " " + getResources().getString(R.string.app_currency) + " " + getResources().getString(R.string.successfull_received));
                                updateBalanceInBg();

                            }else if(GlobalConstants.DEBUG_MODE){

                                // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                                Dialogs.errorDialog(context,Response.getString("error_code"),Response.getString("error_description"),false,false,"",getResources().getString(R.string.ok),null);

                            }else{

                                // Server error
                                AppUtils.toastShort(context,getResources().getString(R.string.msg_server_problem));
                            }

                        }catch (Exception e){

                            if(GlobalConstants.DEBUG_MODE){
                                // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                                Dialogs.errorDialog(context,"Got Error",e.toString() + ", please contact developer immediately",false,false,"","ok",null);

                            }

                        }

                    }},new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if(GlobalConstants.DEBUG_MODE){
                    // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                    Dialogs.errorDialog(context,"Got Error",error.toString(),true,false,"","ok",null);
                }

            }}){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("data", App.getInstance().getDataCustom(Integer.toString(Points),CreditType));
                return params;
            }
        };

        App.getInstance().addToRequestQueue(rewardRequest);

    }


    public void openOfferWall(String Title, String SubTitle, String Type){

        switch (Type) {

            case "redeem":

//                openRedeem();

                break;

            case "about":

                openAbout();

                break;

            case "transactions":

                openTransactions();

                break;

            case "webvids":

                openWebVideos();

                break;

            default:

                parseURL(Type);

                break;
        }
    }

    void showTimerDialog(int TimeLeft){

        CountDownTimerView timerView = new CountDownTimerView(context);
        timerView.setTextSize(getResources().getInteger(R.integer.daily_checkin_timer_size));
        timerView.setPadding(0,0,0,25);
        timerView.setGravity(Gravity.CENTER);
        timerView.setTime(TimeLeft * 1000);
        timerView.startCountDown();
        Dialogs.customDialog(context, timerView,getResources().getString(R.string.daily_reward_taken),false,false,"",getResources().getString(R.string.ok),null);

    }


    // AdNetworks Linked Functions
    void init_startapp(){
        if(App.getInstance().get("StartAppActive",true)){
            StartAppSDK.init(this, App.getInstance().get("StartApp_AppID",""), false);
            StartAppAd.disableSplash();
        }
    }

    void showLoadingVideo(){
        progressDialog = ProgressDialog.show(context,getResources().getString(R.string.loading_video),getResources().getString(R.string.please_wait),false,false);
    }


    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mActionBarDrawerToggle;
    ScrimInsetsFrameLayout mScrimInsetsFrameLayout;

    void initViews(){

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        int Numboftabs = 1;

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            toolbar.setElevation(4);
        }

        pager = findViewById(R.id.pager);
        SlidingTabLayout tabs = findViewById(R.id.tabs);
        CharSequence Titles[] = {getResources().getString(R.string.stores)};
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), Titles, Numboftabs);

        if(App.getInstance().get("APP_TABS_ENABLE",false)){

            Numboftabs = 2;
            CharSequence Titles2[] = {getResources().getString(R.string.stores), getResources().getString(R.string.videos)};
            adapter =  new ViewPagerAdapter(getSupportFragmentManager(), Titles2, Numboftabs);
            tabs.setVisibility(View.VISIBLE);

            if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                tabs.setElevation(4);
            }

        }

        pager.setAdapter(adapter);
        tabs.setDistributeEvenly(true);
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        tabs.setViewPager(pager);

        // Navigation Drawer
        mDrawerLayout = findViewById(R.id.main_activity_DrawerLayout);
        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        mScrimInsetsFrameLayout = findViewById(R.id.navigation_drawer_Layout);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(context,mDrawerLayout,toolbar,R.string.navigation_drawer_opened,R.string.navigation_drawer_closed)
        {   @Override
            public void onDrawerSlide(View drawerView, float slideOffset)
            {
                // Disables the burger/arrow animation by default
                super.onDrawerSlide(drawerView, 0);
            }
        };

        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setTitle(R.string.app_name);
        }

        mActionBarDrawerToggle.setDrawerIndicatorEnabled(false);

        if(App.getInstance().get("APP_NAVBAR_ENABLE",true)){

            Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.menu_icon, context.getTheme());
            mActionBarDrawerToggle.setHomeAsUpIndicator(drawable);
            mActionBarDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mDrawerLayout.isDrawerVisible(GravityCompat.START)){
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                    }else{
                        mDrawerLayout.openDrawer(GravityCompat.START);
                    }
                }
            });

        }else{
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        mActionBarDrawerToggle.syncState();

        // Navigation Drawer layout width
        int possibleMinDrawerWidth = AppUtils.getScreenWidth(context) - UtilsMiscellaneous.getThemeAttributeDimensionSize(context, android.R.attr.actionBarSize);
        int maxDrawerWidth = getResources().getDimensionPixelSize(R.dimen.space280);
        mScrimInsetsFrameLayout.getLayoutParams().width = Math.min(possibleMinDrawerWidth, maxDrawerWidth);

    }

    public void refreshDisplay(){

        TextView fullname = findViewById(R.id.nav_bar_display_name);
        TextView email = findViewById(R.id.nav_bar_display_email);
        TextView point = findViewById(R.id.nav_bar_point);
        TextView user_state = findViewById(R.id.nav_bar_user_state);


        if(App.getInstance().getId() > 0) {
            user_state.setBackground(getResources().getDrawable(R.drawable.state_on));
            fullname.setText(App.getInstance().getFullname());
            email.setText(App.getInstance().getEmail());
            point.setText(App.getInstance().getBalance());
        } else {
            user_state.setBackground(getResources().getDrawable(R.drawable.state_off));
            fullname.setText(getString(R.string.placeholder));
            email.setText(getString(R.string.placeholder));
            point.setText(App.getInstance().getBalance());
        }

    }

    void initNavDrawer() {

        invalidateOptionsMenu();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

        // Redeem
//        redeem = findViewById(R.id.redeem);
//        redeem.setOnClickListener(this);

        // Redeem
        legal_policy = findViewById(R.id.legal_policy);
        legal_policy.setOnClickListener(this);

        // About
//        about = findViewById(R.id.about);
//        about.setOnClickListener(this);

        // Help
        help = findViewById(R.id.help);
        help.setOnClickListener(this);

        // Transactions
        reward_his = findViewById(R.id.nav_transactions);
        reward_his.setOnClickListener(this);

        // Web videos
        daily_check_in = findViewById(R.id.nav_daily_checkin);
        daily_check_in.setOnClickListener(this);

        // POS
        pos = findViewById(R.id.nav_pos);
        pos.setOnClickListener(this);

        // Sign in
        sign_in = findViewById(R.id.nav_sign_in);
        sign_in.setOnClickListener(this);

        // Sign up
        sign_up = findViewById(R.id.nav_sign_up);
        sign_up.setOnClickListener(this);

        // Sign out
        sign_out = findViewById(R.id.nav_sign_out);
        sign_out.setOnClickListener(this);

        /* if not logged in*/
        if(App.getInstance().getId() == 0) {
            sign_in.setVisibility(View.VISIBLE);
            sign_up.setVisibility(View.VISIBLE);
            sign_out.setVisibility(View.GONE);
        } else {
            sign_in.setVisibility(View.GONE);
            sign_up.setVisibility(View.GONE);
            sign_out.setVisibility(View.VISIBLE);
        }

    }

    private void signOut() {
        final AlertDialog alertDialog = GlobalFunctions.showSpotDialog(context, "");
//        showpDialog();

        if (App.getInstance().isConnected() && App.getInstance().getId() != 0) {

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, GlobalConstants.METHOD_ACCOUNT_LOGOUT, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if(alertDialog != null) alertDialog.dismiss();

                            try {

                                if(!response.getBoolean("error")) {

                                    App.getInstance().removeData();
                                    App.getInstance().readData();

                                    Intent i = new Intent(getApplicationContext(), AppActivity.class);
                                    startActivity(i);
                                    ActivityCompat.finishAffinity(context);


                                    Toast.makeText(context, getString(R.string.success_logout), Toast.LENGTH_SHORT).show();
                                    //Logout success
                                    String j = "kk";
                                }

                            } catch (JSONException e) {

                                e.printStackTrace();

                            }
                            hidepDialog();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), getText(R.string.unknown_error), Toast.LENGTH_LONG).show();
                    if(alertDialog != null) alertDialog.dismiss();
                    Log.e(TAG, "Sign out error");
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String,String> params = new HashMap<>();
                    params = App.getInstance().getCredential();
                    return params;
                }
            };

            App.getInstance().addToRequestQueue(jsonReq);

        }
    }
    public void playVideo(String videoId, String videoPoints, String videoURL, String timeDuration, String openLink, int holder_position){

        Intent playVideo = new Intent(context, YouTubePlayerActivity.class);
        playVideo.putExtra(YouTubePlayerActivity.EXTRA_VIDEO_ID, YouTubeUrlParser.getVideoId(videoURL));
        playVideo.putExtra(YouTubePlayerActivity.EXTRA_REWARDS, videoPoints);
        playVideo.putExtra(YouTubePlayerActivity.EXTRA_ID, videoId);
        playVideo.putExtra(YouTubePlayerActivity.EXTRA_TIME_DURATION, timeDuration);
        playVideo.putExtra(YouTubePlayerActivity.EXTRA_LINK, openLink);
        playVideo.putExtra(YouTubePlayerActivity.EXTRA_ORIENTATION, Orientation.ONLY_LANDSCAPE);
        playVideo.putExtra(YouTubePlayerActivity.EXTRA_SHOW_AUDIO_UI, true);
        playVideo.putExtra(YouTubePlayerActivity.EXTRA_HANDLE_ERROR, false);
        playVideo.putExtra(YouTubePlayerActivity.VIDEO_ITEM_HOLDER_POSITION, holder_position);
        startActivityForResult(playVideo,REQUEST_CODE_YOUTUBE_PLAY);
    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) { finish(); return; }
        context.doubleBackToExitPressedOnce = true;

        AppUtils.toastShort(context,getString(R.string.click_back_again));

        new Handler().postDelayed(new Runnable() { @Override public void run() { doubleBackToExitPressedOnce = false; }}, 1500);

        //  super.onBackPressed();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_YOUTUBE_PLAY) {
            if(resultCode == RESULT_OK) {


                String videoId = data.getStringExtra("id");
                String Points = data.getStringExtra("points");
                String openLink = data.getStringExtra("openLink");
                int holder_position = Integer.parseInt(data.getStringExtra("holder_position"));
                if(!videoId.isEmpty() && !Points.isEmpty()){
                    awardVideo(Points,videoId,openLink, holder_position);
                }

            }
        } else {

            updateBalanceInBg();
        }

    }

    /**
     * Get point after watch video
     * @param Points : video its point
     * @param videoId
     * @param openLink
     * @param holder_position :Video position to be updated
    * */
    void awardVideo(final String Points,final String videoId,final String openLink, final int holder_position){
        if(!openLink.equals("none")){ AppUtils.parse(context, openLink); }

        CustomRequest videoRewardRequest = new CustomRequest(Request.Method.POST, GlobalConstants.APP_VIDEOSTATUS,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try{

                            JSONObject Response = new JSONObject(App.getInstance().deData(response.toString()));

                            if(!Response.getBoolean("error") && Response.getInt("error_code") == GlobalConstants.ERROR_SUCCESS){

                                //successfully accepted
                                JSONObject obj = Response.getJSONObject("video");
                                if (obj != null) {
                                    GlobalFunctions.showToast(context, getString(R.string.video_awarded, obj.getString("video_amount")));
                                    // Update current watched video
                                    updateVideo(obj, holder_position);
                                }

                                //Update points
                                updateBalanceInBg();

                                // Video saved Success
                                App.getInstance().store("APPVIDEO_"+videoId,true);

                            }else if(Response.getInt("error_code") == GlobalConstants.ERROR_ALREADY_AWARDED) {
                                JSONObject obj = Response.getJSONObject("video");
                                //Update watched video item
                                if (obj != null) {
                                    GlobalFunctions.showToast(context, getString(R.string.already_awarded));
                                    // Update current watched video
                                    updateVideo(obj, holder_position);
                                }
                                App.getInstance().store("APPVIDEO_"+videoId,true);

                            }else if(Response.getInt("error_code") == GlobalConstants.ERROR_POINT_LIMITED) {

                                JSONObject obj = Response.getJSONObject("video");
                                //Update watched video item
                                if (obj != null) {
                                    GlobalFunctions.showToast(context, getString(R.string.already_limited));
                                    // Update current watched video
                                    updateVideo(obj, holder_position);
                                }

                                App.getInstance().store("APPVIDEO_"+videoId,true);
                            }else if(Response.getInt("error_code") == 699 || Response.getInt("error_code") == 999){

                                Dialogs.validationError(context,Response.getInt("error_code"));

                            }else if(GlobalConstants.DEBUG_MODE){

                                // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                                Dialogs.errorDialog(context,Response.getString("error_code"),Response.getString("error_description"),false,false,"",getResources().getString(R.string.ok),null);

                            }else{

                                // Server error
                                AppUtils.toastShort(context,getResources().getString(R.string.msg_server_problem));
                            }

                        }catch (Exception e){

                            if(GlobalConstants.DEBUG_MODE){

                                // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                                Dialogs.errorDialog(context,"Got Error",e.toString() + ", please contact developer immediately",false,false,"","ok",null);

                            }else{

                                // Server error
                                AppUtils.toastShort(context, getResources().getString(R.string.msg_server_problem));
                            }

                        }

                    }},new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if(GlobalConstants.DEBUG_MODE){

                    // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                    Dialogs.errorDialog(context,"Got Error",error.toString(),true,false,"","ok",null);

                }else{

                    // Server error
                    AppUtils.toastShort(context,getResources().getString(R.string.msg_server_problem));
                }

            }}){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("data", App.getInstance().getDataCustom(videoId,Points));
                return params;
            }
        };

        App.getInstance().addToRequestQueue(videoRewardRequest);

    }

    /**
     * Update video single item after watch/award.
     * @param obj : video data
     * @param holder_position : Video position to be updated
     * */
    private void updateVideo(JSONObject obj, int holder_position) {
        Videos video = VideosFragment.allvideos.get(holder_position);
        try {
            video.setLimit(obj.getString("video_limit"));
            video.setLimitPlayingTimes(obj.getString("video_limit_playing_times"));
            video.setWatchedTimes(obj.getString("video_watched_times"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        VideosFragment.videosAdapter.notifyDataSetChanged();
    }

    private boolean checkLogIn() {
        /* if not logged in*/
        if(App.getInstance().getId() == 0) {
            GlobalFunctions.showToast(context, getString(R.string.not_logged_in));
            Intent signIn = new Intent(context, LoginActivity.class);
            startActivity(signIn);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.legal_policy:
                //todo
                break;
            case R.id.help:
                //todo
                break;
            case R.id.nav_transactions:
                if (checkLogIn()) openTransactions();
                mDrawerLayout.closeDrawers();
                break;
            case R.id.nav_daily_checkin:
                if (checkLogIn()) openWebVideos();
                mDrawerLayout.closeDrawers();
                break;
            case R.id.nav_pos:
                Intent pos = new Intent(context, StoresActivity.class);
                startActivity(pos);
                mDrawerLayout.closeDrawers();
                break;
            case R.id.nav_sign_in:
                Intent in = new Intent(context, LoginActivity.class);
                startActivity(in);
                break;
            case R.id.nav_sign_up:
                Intent up = new Intent(context, SignupActivity.class);
                startActivity(up);
                break;
            case R.id.nav_sign_out:
                signOut();
                mDrawerLayout.closeDrawers();
                break;
        }
    }

    /**Check if updated or not user balance points after POS*/
    private void checkUsedPoints() {
        if(App.getInstance().getId() != 0) {
            GlobalFunctions.updateMyPoints(context, new Notify() {
                @Override
                public void onSuccess(Object object) {
                    refreshDisplay();
                }

                @Override
                public void onAbort(Object object) {

                }

                @Override
                public void onFail() {

                }
            });
        }
    }
}