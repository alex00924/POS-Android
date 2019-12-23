package com.bulletcart.videorewards.Utils;

import android.util.Log;

import com.bulletcart.videorewards.ApiResult.DeliveryProductInfoResult;
import com.bulletcart.videorewards.ApiResult.GroupPrice;
import com.bulletcart.videorewards.ApiResult.ProductInfoResult;
import com.bulletcart.videorewards.ApiResult.ResponseData;
import com.bulletcart.videorewards.ApiResult.StoreInfoResult;
import com.bulletcart.videorewards.ApiResult.TransactionResult;
import com.bulletcart.videorewards.Global.GlobalConstants;
import com.bulletcart.videorewards.Global.GlobalFunctions;
import com.bulletcart.videorewards.Global.GlobalVariables;
import com.bulletcart.videorewards.Model.ProductInfo;
import com.bulletcart.videorewards.Model.TableInfo;
import com.bulletcart.videorewards.api.ApiClient;
import com.bulletcart.videorewards.app.App;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiUtil {
    public static final String TAG = "ApiUtil---------->";

    public static void get_all_products(final Notify notify) {
        Call<DeliveryProductInfoResult> call = ApiClient.getApiClient().get_all_products(GlobalVariables.BUSINESS_ID, GlobalVariables.LOCATION_ID);
        call.enqueue(new Callback<DeliveryProductInfoResult>() {
            @Override
            public void onResponse(Call<DeliveryProductInfoResult> call, Response<DeliveryProductInfoResult> response) {

                DeliveryProductInfoResult data = response.body();
                if (response.isSuccessful()) {
                    if (data.status.equals("success")) {
                        if (notify != null) {
                            notify.onSuccess(data);
                        }
                    } else {
                        if (notify != null)
                            notify.onAbort(data);
                    }
                }
            }

            @Override
            public void onFailure(Call<DeliveryProductInfoResult> call, Throwable t) {

                if (notify != null)
                    notify.onFail();
            }
        });
    }

    public static void get_tables(final Notify notify) {
        Call<TableInfo> call = ApiClient.getApiClient().get_tables(GlobalVariables.BUSINESS_ID, GlobalVariables.LOCATION_ID);
        call.enqueue(new Callback<TableInfo>() {
            @Override
            public void onResponse(Call<TableInfo> call, Response<TableInfo> response) {

                TableInfo data = response.body();
                if (response.isSuccessful()) {
                    if (data.status.equals("success")) {
                        if (notify != null) {
                            notify.onSuccess(data);
                        }
                    } else {
                        if (notify != null)
                            notify.onAbort(data);
                    }
                }
            }

            @Override
            public void onFailure(Call<TableInfo> call, Throwable t) {

                if (notify != null)
                    notify.onFail();
            }
        });
    }


    public static void get_delivery_products(final Notify notify) {
        Call<DeliveryProductInfoResult> call = ApiClient.getApiClient().get_delivery_products(GlobalVariables.BUSINESS_ID, GlobalVariables.LOCATION_ID);
        call.enqueue(new Callback<DeliveryProductInfoResult>() {
            @Override
            public void onResponse(Call<DeliveryProductInfoResult> call, Response<DeliveryProductInfoResult> response) {

                DeliveryProductInfoResult data = response.body();
                if (response.isSuccessful()) {
                    if (data.status.equals("success")) {
                        if (notify != null) {
                            notify.onSuccess(data);
                        }
                    } else {
                        if (notify != null)
                            notify.onAbort(data);
                    }
                }
            }

            @Override
            public void onFailure(Call<DeliveryProductInfoResult> call, Throwable t) {

                if (notify != null)
                    notify.onFail();
            }
        });
    }

    public static void order_delivery( JSONObject order_detail, final Notify notify) {

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), order_detail.toString());
        Call<ResponseData> call = ApiClient.getApiClient().order_delivery(body);
        call.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if (response.isSuccessful()) {
                    if(response.body().status.equals("success")) {
                        Log.e("++++CASH-- : ", response.body().message);
                        if (notify != null)
                            notify.onSuccess(response.body());
                    }
                }
            }
            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                if (notify != null)
                    notify.onFail();
            }
        });
    }

    public static void get_product(String barcode, final Notify notify) {
        Call<ProductInfoResult> call = ApiClient.getApiClient().get_product(barcode, GlobalVariables.BUSINESS_ID, GlobalVariables.LOCATION_ID);
        call.enqueue(new Callback<ProductInfoResult>() {
            @Override
            public void onResponse(Call<ProductInfoResult> call, Response<ProductInfoResult> response) {

                ProductInfoResult data = response.body();
                if (response.isSuccessful()) {
                    if (data.status.equals("success")) {
                        if (notify != null) {
                            notify.onSuccess(data);
                        }
                    } else {
                        if (notify != null)
                            notify.onAbort(data);
                    }
                }
            }

            @Override
            public void onFailure(Call<ProductInfoResult> call, Throwable t) {

                if (notify != null)
                    notify.onFail();
            }
        });
    }

    public static void get_group_price(int variation_id, String group_name, int tax_id, final Notify notify) {
        Call<GroupPrice> call = ApiClient.getApiClient().get_group_price(variation_id, group_name, tax_id);
        call.enqueue(new Callback<GroupPrice>() {
            @Override
            public void onResponse(Call<GroupPrice> call, Response<GroupPrice> response) {
                notify.onSuccess(response.body());
            }
            @Override
            public void onFailure(Call<GroupPrice> call, Throwable t) {
                if (notify != null)
                    notify.onFail();
            }
        });
    }

    public static void get_stores(int nType, final Notify notify) {
        Call<StoreInfoResult> call = ApiClient.getApiClient().get_stores(nType);
        call.enqueue(new Callback<StoreInfoResult>() {
            @Override
            public void onResponse(Call<StoreInfoResult> call, Response<StoreInfoResult> response) {
                StoreInfoResult data = response.body();
                if (response.isSuccessful()) {
                    if (data.status.equals("success")) {
                        if (notify != null) {
                            notify.onSuccess(data);
                        }
                    } else {
                        if (notify != null)
                            notify.onAbort(data);
                    }
                }
            }

            @Override
            public void onFailure(Call<StoreInfoResult> call, Throwable t) {
                if (notify != null)
                    notify.onFail();
            }
        });
    }

    public static void register_products(final Notify notify) {

        JSONObject jsonProductObject = new JSONObject();
        JSONObject jsonProductsObject = new JSONObject();

            try {
                for (int i = 0; i < GlobalVariables.SELECTED_PRODUCTS.size(); i++) {
                    ProductInfo productInfo = GlobalVariables.SELECTED_PRODUCTS.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("product_id", productInfo.getId());
                    jsonObject.put("product_quantity", productInfo.getAmount());
                    jsonObject.put("product_variation_id", productInfo.getVariation_id());
                    jsonObject.put("selling_group_id", productInfo.group_price_id);

                    jsonProductObject.put(String.valueOf(i), jsonObject);
                }

                jsonProductsObject.put("business_id", GlobalVariables.BUSINESS_ID);
                jsonProductsObject.put("location_id", GlobalVariables.LOCATION_ID);
                jsonProductsObject.put("old_uid", GlobalVariables.CODE);
                jsonProductsObject.put("total_cash_price", String.valueOf(GlobalVariables.TOTAL_PRICE_TO_CHECK));
                jsonProductsObject.put("points", GlobalVariables.USED_POINTS_TO_CHECK);
//                jsonProductsObject.put("point_ratio", GlobalVariables.POINT_CURRENCY_RATIO);
                jsonProductsObject.put("point_ratio", App.getInstance().getRatio());
                jsonProductsObject.put("products", jsonProductObject);
                jsonProductsObject.put("is_delivery", GlobalVariables.BUSINESS_IS_DELYVERY);
                jsonProductsObject.put("delivery_uid", GlobalVariables.BUSINESS_DELIVERY_UID);

                if(GlobalVariables.BUSINESS_TYPE == GlobalConstants.BUSINESS_TYPE_RESTAURANT  && !GlobalVariables.BUSINESS_IS_DELYVERY)
                    jsonProductsObject.put("res_table_id", GlobalVariables.BUSINESS_RESTAURANT_TABLE);
                Log.e("----CASH : ", jsonProductsObject.toString());

            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonProductsObject.toString());
            Call<ResponseData> call = ApiClient.getApiClient().register_products(body);
            call.enqueue(new Callback<ResponseData>() {
                @Override
                public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                    if (response.isSuccessful()) {
                        if(response.body().status.equals("success")) {
                            Log.e("++++CASH-- : ", response.body().message);
                            if (notify != null)
                                notify.onSuccess(response.body());
                        }
                    }
                }
                @Override
                public void onFailure(Call<ResponseData> call, Throwable t) {
                    if (notify != null)
                        notify.onFail();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void remove_products(String uid, String mode, final Notify notify) {
        Call<ResponseData> call = ApiClient.getApiClient().remove_products(uid, mode);
        call.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                ResponseData data = response.body();
                if (response.isSuccessful()) {
                    if (data.status.equals("success")) {
                        if (notify != null) {
                            notify.onSuccess(data);
                        }
                    } else {
                        if (notify != null)
                            notify.onAbort(data);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                if (notify != null)
                    notify.onFail();
            }
        });
    }
    public static void pos_pong(String event_string, final Notify notify) {
        Call<ResponseData> call = ApiClient.getApiClient().pos_pong(event_string);
        call.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                ResponseData data = response.body();
                if (response.isSuccessful()) {
                    if (data.status.equals("success")) {
                        if (notify != null) {
                            notify.onSuccess(data);
                        }
                    } else {
                        if (notify != null)
                            notify.onAbort(data);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                if (notify != null)
                    notify.onFail();
            }
        });
    }

    public static void get_token(final Notify notify) {
        Call<ResponseData> call = ApiClient.getApiClient().get_token(GlobalVariables.LOCATION_ID);
        call.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                ResponseData data = response.body();
                if (response.isSuccessful()) {
                    if (data.status.equals("success")) {
                        if (notify != null) {
                            notify.onSuccess(data);
                        }
                    } else {
                        if (notify != null)
                            notify.onAbort(data);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                if (notify != null)
                    notify.onFail();
            }
        });
    }

    public static void process_payment(String nonce, String card_type, String pay_method, final Notify notify) {
        JSONArray productJson = new JSONArray();
        JSONObject fullJson = new JSONObject();
        try {
            fullJson.put("currency", GlobalVariables.BUSINESS_CURRENCY_STRING);
            fullJson.put("nonce", nonce);
            fullJson.put("cardType", card_type);
            fullJson.put("amount", GlobalVariables.TOTAL_PRICE_TO_CHECK);
            float fVal = GlobalVariables.TOTAL_PRICE_TO_CHECK * GlobalVariables.CURRENCY_RATIO_TO_MERCHANT;
            float converted_total_price = GlobalFunctions.roundToDecimal(fVal, 2);
            fullJson.put("amount_for_braintree", String.valueOf(converted_total_price));
            fullJson.put("CURRENCY_NAME", GlobalVariables.BUSINESS_CURRENCY_STRING );
            fullJson.put("points", GlobalVariables.USED_POINTS_TO_CHECK);
//            fullJson.put("point_ratio", GlobalVariables.POINT_CURRENCY_RATIO);
            fullJson.put("point_ratio", App.getInstance().getRatio());
            fullJson.put("type", pay_method);
            fullJson.put("business_id", GlobalVariables.BUSINESS_ID);
            fullJson.put("location_id", GlobalVariables.LOCATION_ID);
            fullJson.put("is_delivery", GlobalVariables.BUSINESS_IS_DELYVERY);
            if(GlobalVariables.BUSINESS_IS_DELYVERY) {
                fullJson.put("delivery_uid", GlobalVariables.BUSINESS_DELIVERY_UID);
            }

            if(GlobalVariables.BUSINESS_TYPE == GlobalConstants.BUSINESS_TYPE_RESTAURANT  && !GlobalVariables.BUSINESS_IS_DELYVERY)
                fullJson.put("res_table_id", GlobalVariables.BUSINESS_RESTAURANT_TABLE);

            for(int i = 0; i < GlobalVariables.SELECTED_PRODUCTS.size(); i++) {
                if( GlobalVariables.SELECTED_PRODUCTS.get(i).amount < 1 )
                    continue;
                ProductInfo data = GlobalVariables.SELECTED_PRODUCTS.get(i);
                JSONObject productItemJson = new JSONObject();
                productItemJson.put("unit_price", data.getDefault_sell_price());
                productItemJson.put("line_discount_type", "fixed");
                productItemJson.put("line_discount_amount", 0.00);
                productItemJson.put("item_tax", data.getTax());
                productItemJson.put("tax_id", data.tax);
                productItemJson.put("sell_line_note", null);
                productItemJson.put("product_id", data.id);
                productItemJson.put("variation_id", data.variation_id);
                productItemJson.put("enable_stock", data.enable_stock);
                productItemJson.put("quantity", data.amount);
                productItemJson.put("unit_price_inc_tax", data.sell_price_inc_tax);
//                productJson.put(String.valueOf(data.id), productItemJson);
                productJson.put(productItemJson);
            }

            fullJson.put("products", productJson);

            Log.e("----CARD : ", fullJson.toString());

            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), fullJson.toString());
            Call<TransactionResult> call = ApiClient.getApiClient().process_payment(body);
            call.enqueue(new Callback<TransactionResult>() {
                @Override
                public void onResponse(Call<TransactionResult> call, Response<TransactionResult> response) {
                    if (response.isSuccessful()) {
                        if(response.body().status.equals("success")) {

                            Log.e("++++CARD-- : ", response.body().message);
                            if (notify != null)
                                notify.onSuccess(response.body());
                        } else {
                            if (notify != null)
                                notify.onFail();
                        }
                    }
                }
                @Override
                public void onFailure(Call<TransactionResult> call, Throwable t) {
                    if (notify != null)
                        notify.onFail();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void process_payment_for_delivery(String nonce, final Notify notify) {
        JSONObject fullJson = new JSONObject();
        try {
            fullJson.put("nonce", nonce);
            float fVal = GlobalVariables.TOTAL_PRICE_TO_CHECK * GlobalVariables.CURRENCY_RATIO_TO_MERCHANT;
            float converted_total_price = GlobalFunctions.roundToDecimal(fVal, 2);
            fullJson.put("amount_for_braintree", String.valueOf(converted_total_price));
            fullJson.put("location_id", GlobalVariables.LOCATION_ID);
            fullJson.put("order_uid", GlobalVariables.BUSINESS_DELIVERY_UID);

            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), fullJson.toString());
            Call<TransactionResult> call = ApiClient.getApiClient().process_for_delivery(body);
            call.enqueue(new Callback<TransactionResult>() {
                @Override
                public void onResponse(Call<TransactionResult> call, Response<TransactionResult> response) {
                    if (response.isSuccessful()) {
                        if(response.body().status.equals("success")) {

                            Log.e("++++CARD-- : ", response.body().message);
                            if (notify != null)
                                notify.onSuccess(response.body());
                        } else {
                            if (notify != null)
                                notify.onFail();
                        }
                    }
                }
                @Override
                public void onFailure(Call<TransactionResult> call, Throwable t) {
                    if (notify != null)
                        notify.onFail();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
