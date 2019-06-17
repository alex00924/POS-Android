package com.bulletcart.videorewards.Activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.bulletcart.videorewards.Global.GlobalFunctions;
import com.bulletcart.videorewards.R;
import com.bulletcart.videorewards.app.App;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.List;

public class QRCodeActivity extends AppCompatActivity {

    private CompoundBarcodeView bc;
    private BeepManager beepManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        beepManager = new BeepManager(this);
        bc = findViewById(R.id.barcode);

        try {
            scanQRCode();
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_simple, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        menu.findItem(R.id.points).setTitle(getString(R.string.app_currency).toUpperCase()+" : " + App.getInstance().getBalance());
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home: {

                onBackPressed();
                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }
    private void scanQRCode() {
        try {

            bc.decodeSingle(new BarcodeCallback() {

                @Override
                public void barcodeResult(BarcodeResult result) {
                    try {
                        bc.pause();
                        beepManager.playBeepSound();
                        GlobalFunctions.vibrate(QRCodeActivity.this, 100);

                        if (!result.toString().contentEquals("")) {
                            Intent data = new Intent();
                            data.putExtra("barcode", result.toString());
                            setResult(CommonStatusCodes.SUCCESS, data);
                            finish();
                        } else {
                            final Handler scanBCHandler = new Handler();
                            scanBCHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    bc.resume();
                                    scanQRCode();
                                }
                            }, 1000);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        final Handler scanBCHandler = new Handler();
                        scanBCHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                bc.resume();
                                scanQRCode();
                            }
                        }, 1000);
                    }
                }

                @Override
                public void possibleResultPoints(List<ResultPoint> resultPoints) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onResume() {
        bc.resume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        bc.pause();
        super.onPause();
    }
}
