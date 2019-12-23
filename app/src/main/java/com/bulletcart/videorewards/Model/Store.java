package com.bulletcart.videorewards.Model;

import java.util.ArrayList;
import java.util.List;

public class Store {
    public int id;          //business location id
    public String name;
    public String mobile;
    public int business_id;
    public String business_name;
    public String logo;
    public String currency_string;
    public String currency;
    public String tax;
    public String merchant_currency;
    public ArrayList<String> selling_group = new ArrayList<>();

    public boolean can_delivery;
    public ArrayList<String> delivery_area = new ArrayList<>();
    public float delivery_charge;
    public String delivery_tax_type;
    public float delivery_tax;
    public int delivery_is_minimum;
    public float delivery_minimum;

    public String getTax_type() {
        return tax_type;
    }

    public void setTax_type(String tax_type) {
        this.tax_type = tax_type;
    }

    public String tax_type;

    public float getDefault_sales_discount() {
        return default_sales_discount;
    }

    public void setDefault_sales_discount(float default_sales_discount) {
        this.default_sales_discount = default_sales_discount;
    }

    public float default_sales_discount = 0.0f;

    public String getTax() {
        return tax;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getBusiness_id() {
        return business_id;
    }

    public void setBusiness_id(int business_id) {
        this.business_id = business_id;
    }

    public String getBusiness_name() {
        return business_name;
    }

    public void setBusiness_name(String business_name) {
        this.business_name = business_name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getCurrency_string() {
        return currency_string;
    }

    public void setCurrency_string(String currency_string) {
        this.currency_string = currency_string;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
