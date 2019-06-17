package com.bulletcart.videorewards.Model;

import java.io.Serializable;

public class Transaction implements Serializable {
    public int business_id;
    public int location_id;
    public String type;
    public String status;
    public String invoice_no;
    public String final_total;
    public String created_by;
    public int id;

}
