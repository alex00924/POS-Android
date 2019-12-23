package com.bulletcart.videorewards.Global;

import com.bulletcart.videorewards.Model.Business;
import com.bulletcart.videorewards.Model.ProductInfo;
import com.bulletcart.videorewards.Model.Store;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GlobalVariables {

    public static boolean BUSSINESS_CAN_DELIVEY = false;            //current location can delivery
    public static List<String> BUSSINESS_DELIVERY_AREA= new ArrayList<>();
    public static float BUSSINESS_DELIVERY_CHARGE = 0.0F;
    public static String BUSSINESS_DELIVEY_TAX_TYPE = "";
    public static float BUSSINESS_DELIVEY_TAX = 0.0f;
    public static int BUSSINESS_DELIVEY_IS_MINIMUM = 0;
    public static float BUSSINESS_DELIVEY_MINIMUM = 0.0f;

    public static float TOTAL_PAIABLE_WITHOUT_DELIVERY = 0.0f;
    public static boolean BUSINESS_IS_DELYVERY = false;             //user want to delivery : true, direct sell : false
    public static String  BUSINESS_DELIVERY_UID = "";               //delivery uid

    public static float BUISINESS_DEFAULT_DISCOUNT = 0.0f;      //default discount of store/restaurant
    public static List<String> BUSINESS_DEFAULT_PRICE_GROUPS = new ArrayList<>();       //default selling price group array
    public static int BUSINESS_ID = 0;
    public static int LOCATION_ID = 0;
    public static String BUSINESS_NAME = "";
    public static String SHOP_NAME = "";
    public static String BUSINESS_CURRENCY = "";
    public static String BUSINESS_MERCHANT_CURRENCY = "";   //new added
    public static String BUSINESS_CURRENCY_STRING = "";
    public static String BUSINESS_TAX_TYPE = "gst";
    public static String BUSINESS_TAX = "0";
    public static float CURRENCY_RATIO_TO_MERCHANT = 1.0f;
    public static List<ProductInfo> SELECTED_PRODUCTS = new ArrayList<>();
    public static float TOTAL_PRICE_TO_CHECK = 0.0f;
    public static int USED_POINTS_TO_CHECK = 0;
    public static String CURRENCY_OF_USED_POINTS_TO_CHECK = "0.0";
    public static String POINT_CURRENCY_RATIO = "0";
    public static String CODE = "";
    public static List<Business> BUSINESSES_STORES = new ArrayList<>();
    public static List<Business> BUSINESSES_RESTAURANTS = new ArrayList<>();
    public static List<Store> STORES = new ArrayList<>();

    public static String PUSHER_APP_KEY = "01b908ea43142bcf4b39";
    public static String PUSHER_CHANNEL = "pos-channel";
    public static String PUSHER_EVENT = "pos-event";
    public static String PUSHER_CANCEL_EVENT = "pos-cancel-event-";
    public static String PUSHER_PING_EVENT = "pos-ping-";
    public static String PUSHER_PONG_EVENT = "pos-pong-";
    public static String PUSHER_CLUSTER = "us2";

    public static String BRAINTREE_TOKEN = "";
    public static String CARD_TYPE = "Visa";

    public static final String PAYPAL_CLIENT_ID = "AT8zkjV13uon590WKVTQPuGipsVGcKh8kfKYmfw0HHCRfQ_-gNQqZzkzL2d1h5GQqLxYvBUNoHQSdYlC";

    public static int BUSINESS_TYPE = 0;        //store or restaurant.
    public static String BUSINESS_RESTAURANT_TABLE = "";    //restaurant table id

    public static int BUSINESS_PAYMENT_TYPE = 0;
    public static JSONObject ORDER_DETAIL;
}
