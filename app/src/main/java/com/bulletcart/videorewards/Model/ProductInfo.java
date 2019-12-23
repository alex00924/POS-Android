package com.bulletcart.videorewards.Model;

import com.bulletcart.videorewards.Global.GlobalFunctions;
import com.bulletcart.videorewards.Global.GlobalVariables;

import java.io.Serializable;
import java.util.ArrayList;

public class ProductInfo implements Serializable {
    //Real price = sell_price_inc_tax = dpp_inc_tax * (100 + profit_percent) / 100
    public int id;
    public String name;  //Product name
    public String sku;  //e.g. barcode
    public String price; // same as sell_price_inc_tax, sale price
    public String tax_type; //Inclusive, Exclusive
    public String sell_price_inc_tax; //It is the price after adding the tax on the total amount of default selling price.
    public String dpp_inc_tax;

    public String default_sell_price;
    public String profit_percent; //It’s the profit that the store will earn per product.
    public String image;
    public int enable_stock;
    public String default_purchase_price;
    public String tax;
    public int variation_id;
    public int amount = 0;
    public String strGroup;
    public boolean m_bEnableGroup = false;  //flag of group
    public float group_price;               //the default selling price group exc tax
    public int   group_price_id = 0;

    public ArrayList<Group> variation_group = new ArrayList<Group>();

    //Setter
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDefault_sell_price(String default_sell_price) {
        this.default_sell_price = default_sell_price;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }

    public void setTax_type(String tax_type) {
        this.tax_type = tax_type;
    }

    public void setSell_price_inc_tax(String sell_price_inc_tax) {
        this.sell_price_inc_tax = sell_price_inc_tax;
    }

    public void setDpp_inc_tax(String dpp_inc_tax) {
        this.dpp_inc_tax = dpp_inc_tax;
    }

    public void setProfit_percent(String profit_percent) {
        this.profit_percent = profit_percent;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setEnable_stock(int enable_stock) {
        this.enable_stock = enable_stock;
    }

    public void setDefault_purchase_price(String default_purchase_price) {
        this.default_purchase_price = default_purchase_price;
    }

    public void setVariation_id(int variation_id) {
        this.variation_id = variation_id;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    // Getter

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDefault_sell_price() {
        if( m_bEnableGroup )
            return "" + GlobalFunctions.roundToDecimal(group_price, 2);

        return default_sell_price;
    }

    public String getTax() {
        float business_tax = Float.parseFloat(GlobalVariables.BUSINESS_TAX) / 100;
        float fPrice = Float.parseFloat(default_sell_price);
        if( m_bEnableGroup )
            fPrice = group_price;

        return String.valueOf(GlobalFunctions.roundToDecimal( fPrice * (1 - GlobalVariables.BUISINESS_DEFAULT_DISCOUNT) * business_tax, 2));
    }

    public String getUnitPrice() {
        float business_tax = Float.parseFloat(GlobalVariables.BUSINESS_TAX) / 100;
        float fPrice = Float.parseFloat(default_sell_price);
        if( m_bEnableGroup )
            fPrice = group_price;

        return String.valueOf(GlobalFunctions.roundToDecimal(fPrice * (1 - GlobalVariables.BUISINESS_DEFAULT_DISCOUNT) * ( 1 + business_tax ), 2));
    }

    public String getDiscount()
    {
        float fPrice = Float.parseFloat(default_sell_price);
        if( m_bEnableGroup )
            fPrice = group_price;

        return String.valueOf(GlobalFunctions.roundToDecimal(fPrice * GlobalVariables.BUISINESS_DEFAULT_DISCOUNT, 2));
    }

    public String getSku() {
        return sku;
    }

    public String getPrice() {
        return price;
    }

    public String getTax_type() {
        return tax_type;
    }

    public String getSell_price_inc_tax() {
        return sell_price_inc_tax;
    }

    public String getDpp_inc_tax() {
        return dpp_inc_tax;
    }

    public String getProfit_percent() {
        return profit_percent;
    }

    public String getImage() {
        return image;
    }

    public int getEnable_stock() {
        return enable_stock;
    }

    public String getDefault_purchase_price() {
        return default_purchase_price;
    }

    public int getVariation_id() {
        return variation_id;
    }

    public int getAmount() {
        return amount;
    }

    public class Group implements Serializable{
        public int id;
        public float price_inc_tax;
        public String name;
    }
}
