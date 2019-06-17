package com.bulletcart.videorewards.Utils;

public interface Notify {
    void onSuccess(Object object);
    void onAbort(Object object);
    void onFail();
}
