package com.bulletcart.videorewards.Model;

import java.io.Serializable;

/**
 * Created by cloudfuze on 20/02/15.
 */
public class ListItemBean implements Serializable {

    private int id = 0;
    private String itemName="";
    private String sku = "";
    private int rate = 0;
    private String price = "";
    private int quantity = 0;


    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }



    public int getItemresid() {
        return itemresid;
    }

    public void setItemresid(int itemresid) {
        this.itemresid = itemresid;
    }

    private int itemresid;

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }


}
