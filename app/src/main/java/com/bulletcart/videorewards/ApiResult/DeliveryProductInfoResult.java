package com.bulletcart.videorewards.ApiResult;

import com.bulletcart.videorewards.Model.ProductInfo;

import java.util.ArrayList;
import java.util.List;

public class DeliveryProductInfoResult {
    public String status;
    public int code;
    public String message;

    public List<ProductInfo> products = new ArrayList<>();;
}
