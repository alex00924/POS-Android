package com.bulletcart.videorewards.Global;

import com.bulletcart.videorewards.Model.ProductInfo;
import com.bulletcart.videorewards.Model.Store;
import com.bulletcart.videorewards.app.App;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by DroidOXY
 */
 
public class GlobalConstants {
//      public static String SERVER_URL = "http://10.0.2.2/";
//      public static final String POS_SERVER_URL = "http://10.0.2.2:8000";

//    public static String SERVER_URL = "http://10.0.3.2/";
//    public static final String POS_SERVER_URL = "http://10.0.3.2:8000";
//    public static String SERVER_URL = "http://127.0.0.1/";
//    public static final String POS_SERVER_URL = "http://127.0.0.1:8000";

    public static String SERVER_URL = "https://advertise.bulletcart.store/";
    public static final String POS_SERVER_URL = "https://bulletcart.store";

    // Manual/Email Login and Signup enable = true || disable = false
    public static Boolean ENABLE_EMAIL_LOGIN = true;

    // Gmail Login and Signup enable = true || disable = false
    public static Boolean ENABLE_GMAIL_LOGIN = false;

    // Facebook Login and Signup enable = true || disable = false
    public static Boolean ENABLE_FACEBOOK_LOGIN = false;

    // App Intro enable = true || disable = false
    public static Boolean ENABLE_APP_INTRO = false;

    public static final String CLIENT_ID = "1";

    public static final String API_DOMAIN = SERVER_URL;
    public static final String API_DOMAIN_POS = POS_SERVER_URL;

    public static final String API_FILE_EXTENSION = ".php";
    public static final String API_VERSION = "v2";


    public static final String METHOD_ACCOUNT_LOGIN = API_DOMAIN + "api/" + API_VERSION + "/account.signIn" + API_FILE_EXTENSION;
    public static final String METHOD_ACCOUNT_SIGNUP = API_DOMAIN + "api/" + API_VERSION + "/account.signUp" + API_FILE_EXTENSION;
    public static final String METHOD_ACCOUNT_RECOVERY = API_DOMAIN + "api/" + API_VERSION + "/account.recovery" + API_FILE_EXTENSION;
    public static final String METHOD_ACCOUNT_AUTHORIZE = API_DOMAIN + "api/" + API_VERSION + "/account.authorize" + API_FILE_EXTENSION;
    public static final String METHOD_ACCOUNT_LOGOUT = API_DOMAIN + "api/" + API_VERSION + "/account.logOut" + API_FILE_EXTENSION;

    public static final String APP_PAYOUTS = API_DOMAIN + "api/" + API_VERSION + "/app.Payouts" + API_FILE_EXTENSION;
    public static final String ACCOUNT_REFER = API_DOMAIN + "api/" + API_VERSION + "/account.Refer" + API_FILE_EXTENSION;
    public static final String APP_OFFERWALLS = API_DOMAIN + "api/" + API_VERSION + "/app.OfferWalls" + API_FILE_EXTENSION;
    public static final String APP_VIDEOS = API_DOMAIN + "api/" + API_VERSION + "/app.Videos" + API_FILE_EXTENSION;
    public static final String ACCOUNT_REDEEM = API_DOMAIN + "api/" + API_VERSION + "/account.Redeem" + API_FILE_EXTENSION;
    public static final String ACCOUNT_REWARD = API_DOMAIN + "api/" + API_VERSION + "/account.Reward" + API_FILE_EXTENSION;
    public static final String ACCOUNT_BALANCE = API_DOMAIN + "api/" + API_VERSION + "/account.Balance" + API_FILE_EXTENSION;
    public static final String UPDATE_BALANCE_AFTER_POS = API_DOMAIN + "api/" + API_VERSION + "/account.pos.Balance" + API_FILE_EXTENSION;
    public static final String ACCOUNT_CHECKIN = API_DOMAIN + "api/" + API_VERSION + "/account.Checkin" + API_FILE_EXTENSION;
    public static final String APP_VIDEOSTATUS = API_DOMAIN + "api/" + API_VERSION + "/app.VideoStatus" + API_FILE_EXTENSION;public static final String APP_OFFERSTATUS = API_DOMAIN + "api/" + API_VERSION + "/app.OfferStatus" + API_FILE_EXTENSION;
    public static final String ACCOUNT_TRANSACTIONS = API_DOMAIN + "api/" + API_VERSION + "/account.Transactions" + API_FILE_EXTENSION;


    /*Bullet cart POS apis*/

    public static final String PRODUCT_IMAGE_URL = API_DOMAIN_POS + "/storage/img/";
    public static final String PRODUCT_BUSINESS_LOGO_URL = API_DOMAIN_POS + "/storage/business_logos/";


    /* Bullet cart POS api end*/

    public static final int ERROR_SUCCESS = 0;

    public static final int ERROR_LOGIN_TAKEN = 300;
    public static final int ERROR_EMAIL_TAKEN = 301;
    public static final int ERROR_IP_TAKEN = 302;

    public static final int ACCOUNT_STATE_ENABLED = 0;
    public static final int ACCOUNT_STATE_DISABLED = 1;
    public static final int ACCOUNT_STATE_BLOCKED = 2;
    public static final int ACCOUNT_STATE_DEACTIVATED = 3;

    public static final int ERROR_UNKNOWN = 100;
    public static final int ERROR_ACCESS_TOKEN = 101;

    public static final int ERROR_POINT_LIMITED = 102;
    public static final int ERROR_ALREADY_AWARDED = 103;

    public static final int ERROR_ACCOUNT_ID = 400;

    public static Boolean DEBUG_MODE = App.getInstance().get("APP_DEBUG_MODE",false);

    public static final String LICENSE_COPY = "http://www.codyhub.com/item/video-rewards-android-app";

    public static int BUSINESS_TYPE_STORE = 0;              //business type
    public static int BUSINESS_TYPE_RESTAURANT = 1;

    public static final int PAYMENT_CASH = 1;              //payment methods, is used in delivery
    public static final int PAYMENT_CARD = 2;

    public static String PUSHER_ORDER_CONFIRM = "Confirm order-";
    public static String PUSHER_ORDER_CANCEL = "Cancel order-";
    public static String PUSHER_PENDING_ORDER_CONFIRM = "Confirm pending order-";
    public static String PUSHER_PENDING_ORDER_CANCEL = "Cancel pending order-";

}