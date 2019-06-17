package com.bulletcart.videorewards.api;

import com.bulletcart.videorewards.ApiResult.GroupPrice;
import com.bulletcart.videorewards.ApiResult.ProductInfoResult;
import com.bulletcart.videorewards.ApiResult.ResponseData;
import com.bulletcart.videorewards.ApiResult.StoreInfoResult;
import com.bulletcart.videorewards.ApiResult.TransactionResult;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiInterface {

    @GET("/api/get_product/{barcode}/{business_id}/{location_id}")
    Call<ProductInfoResult> get_product(@Path("barcode") String barcode, @Path("business_id") int business_id, @Path("location_id") int location_id);

    @GET("/api/get_group_price/{variation_id}/{group_name}/{tax_id}")
    Call<GroupPrice> get_group_price(@Path("variation_id") int variation_id, @Path("group_name") String group_name, @Path("tax_id") int tax_id);

    @GET("/api/getBusinesses")
    Call<StoreInfoResult> get_stores();

    @POST("/api/register_products")
    Call<ResponseData> register_products(@Body RequestBody body);

    @GET("/api/remove_products/{uid}/{mode}")
    Call<ResponseData> remove_products(@Path("uid") String uid, @Path("mode") String mode);

    @GET("/api/get_braintree_token/{business_loc_id}")
    Call<ResponseData> get_token(@Path("business_loc_id") int business_loc_id);

    @POST("/api/payment/process")
    Call<TransactionResult> process_payment(@Body RequestBody body);

    @GET("/api/pos_pong/{event}")
    Call<ResponseData> pos_pong(@Path("event") String event_string);
}