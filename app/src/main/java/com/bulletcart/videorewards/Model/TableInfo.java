package com.bulletcart.videorewards.Model;

import java.util.ArrayList;

public class TableInfo {
    public String status;
    public int code;
    public String message;

    public ArrayList<Table> tables = new ArrayList<>();
    public class Table {
        public String id;
        public String name;
    }
}
