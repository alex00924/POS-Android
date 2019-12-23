package com.bulletcart.videorewards.Activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.Card;
import com.braintreepayments.api.exceptions.BraintreeError;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.ConfigurationListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.CardBuilder;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.bulletcart.videorewards.ApiResult.TransactionResult;
import com.bulletcart.videorewards.CCFragment.CCNameFragment;
import com.bulletcart.videorewards.CCFragment.CCNumberFragment;
import com.bulletcart.videorewards.CCFragment.CCSecureCodeFragment;
import com.bulletcart.videorewards.CCFragment.CCValidityFragment;
import com.bulletcart.videorewards.Fragments.CardBackFragment;
import com.bulletcart.videorewards.Fragments.CardFrontFragment;
import com.bulletcart.videorewards.Global.GlobalFunctions;
import com.bulletcart.videorewards.Global.GlobalVariables;
import com.bulletcart.videorewards.Model.ProductInfo;
import com.bulletcart.videorewards.R;
import com.bulletcart.videorewards.Utils.ApiUtil;
import com.bulletcart.videorewards.Utils.CreditCardUtils;
import com.bulletcart.videorewards.Utils.Notify;
import com.bulletcart.videorewards.Utils.ViewPagerAdapter;
import com.bulletcart.videorewards.app.App;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CheckOutActivity extends ActivityBase implements
        FragmentManager.OnBackStackChangedListener,
        PaymentMethodNonceCreatedListener,
        ConfigurationListener, BraintreeErrorListener {

    private Context context;

    private float totalPrice = 0.0f;

    @BindView(R.id.btnNumNext)
    Button btnNumNext;

    @BindView(R.id.iv_background)
    ImageView btnBackground;

    public CardFrontFragment cardFrontFragment;
    public CardBackFragment cardBackFragment;

    //This is our viewPager
    private ViewPager viewPager;

    public CCNumberFragment numberFragment;
    public CCNameFragment nameFragment;
    public CCValidityFragment validityFragment;
    public CCSecureCodeFragment secureCodeFragment;

    int total_item;
    boolean backTrack = false;

    private boolean mShowingBack = false;

    private boolean mIsProcRunning = false;

    String cardNumber, cardCVV, cardValidity, cardName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("CHECK OUT");

        context = CheckOutActivity.this;

        ButterKnife.bind(this);

        cardFrontFragment = new CardFrontFragment();
        cardBackFragment = new CardBackFragment();

        if (savedInstanceState == null) {
            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, cardFrontFragment).commit();

        } else {
            mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
        }

        getFragmentManager().addOnBackStackChangedListener(this);

        //Initializing viewPager
        viewPager = findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(4);
        setupViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == total_item)
                    btnBackground.setImageResource(R.drawable.ic_upward);
                else
                    btnBackground.setImageResource(R.drawable.ic_next);

                Log.d("track", "onPageSelected: " + position);

                if (position == total_item) {
                    flipCard();
                    backTrack = true;
                } else if (position == total_item - 1 && backTrack) {
                    flipCard();
                    backTrack = false;
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        btnNumNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = viewPager.getCurrentItem();
                if (pos < total_item) {
                    viewPager.setCurrentItem(pos + 1);
                } else {
                    checkEntries();
                }
            }
        });


//        setTotalPrice();
    }

    public void setTotalPrice() {
        for(int i = 0; i < GlobalVariables.SELECTED_PRODUCTS.size(); i++) {
            ProductInfo item = GlobalVariables.SELECTED_PRODUCTS.get(i);
            totalPrice += item.amount * Float.parseFloat(item.price);
        }
        totalPrice = GlobalFunctions.roundToDecimal(totalPrice, 6);
    }

    public void checkEntries() {
        cardName = nameFragment.getName();
        cardNumber = numberFragment.getCardNumber();
        cardValidity = validityFragment.getValidity();
        cardCVV = secureCodeFragment.getValue();

        if (TextUtils.isEmpty(cardName)) {
            Toast.makeText(context, "Enter Valid Name", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(cardNumber) || !CreditCardUtils.isValid(cardNumber.replace(" ",""))) {
            Toast.makeText(context, "Enter Valid card number", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(cardValidity)||!CreditCardUtils.isValidDate(cardValidity)) {
            Toast.makeText(context, "Enter correct validity", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(cardCVV)||cardCVV.length()<3) {
            Toast.makeText(context, "Enter valid security number", Toast.LENGTH_SHORT).show();
        } else {
            getNonce();
        }
    }

    //get Braintree nonce
    private void getNonce() {
        try {
            BraintreeFragment mBraintreeFragment = BraintreeFragment.newInstance(this, GlobalVariables.BRAINTREE_TOKEN);
            CardBuilder cardBuilder = new CardBuilder()
                    .cardNumber(cardNumber)
                    .expirationDate(cardValidity)
                    .cardholderName(cardName)
                    .cvv(cardCVV);

//            progressDialog = GlobalFunction.showClassicProgressDialog(context, "", getString(R.string.waiting_msg));
            GlobalFunctions.showProgressDiaog(context);
            Card.tokenize(mBraintreeFragment, cardBuilder);

        } catch (InvalidArgumentException e) {
            // There was an issue with your authorization string.
        }
    }

    @Override
    public void onBackStackChanged() {
        mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        numberFragment = new CCNumberFragment();
        nameFragment = new CCNameFragment();
        validityFragment = new CCValidityFragment();
        secureCodeFragment = new CCSecureCodeFragment();
        adapter.addFragment(numberFragment);
        adapter.addFragment(nameFragment);
        adapter.addFragment(validityFragment);
        adapter.addFragment(secureCodeFragment);

        total_item = adapter.getCount() - 1;
        viewPager.setAdapter(adapter);

    }

    private void flipCard() {
        if (mShowingBack) {
            getFragmentManager().popBackStack();
            return;
        }

        // Flip to the back.
        //setCustomAnimations(int enter, int exit, int popEnter, int popExit)
        mShowingBack = true;
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.animator.card_flip_right_in,
                        R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in,
                        R.animator.card_flip_left_out)
                .replace(R.id.fragment_container, cardBackFragment)
                .addToBackStack(null)
                .commit();
    }

    public void nextClick() {
        btnNumNext.performClick();
    }

    //Brain tree listener
    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        // Send this nonce to your server
        String nonce = paymentMethodNonce.getNonce();
//        GlobalFunction.hideClassicProgressDialog(progressDialog);
        if(GlobalVariables.BUSINESS_IS_DELYVERY) {
            processPaymentForDelivery(nonce, GlobalVariables.CARD_TYPE, "CreditCard");
        } else {
            processPayment(nonce, GlobalVariables.CARD_TYPE, "CreditCard");
        }
        Log.d("Braintree-nonce=>", nonce);
    }

    //Brain tree listener
    @Override
    public void onConfigurationFetched(Configuration configuration) {
        configuration.getCardConfiguration();
    }

    //Brain tree listener
    @Override
    public void onError(Exception error) {
        GlobalFunctions.hideProgressDialog();
        if (error instanceof ErrorWithResponse) {
            ErrorWithResponse errorWithResponse = (ErrorWithResponse) error;
            BraintreeError cardErrors = errorWithResponse.errorFor("creditCard");
            if (cardErrors != null) {
                // There is an issue with the credit card.
                BraintreeError expirationMonthError = cardErrors.errorFor("expirationMonth");
                if (expirationMonthError != null) {
                    // There is an issue with the expiration month.
                    GlobalFunctions.showToast(CheckOutActivity.this, expirationMonthError.getMessage());
                } else {
                    GlobalFunctions.showToast(CheckOutActivity.this, getString(R.string.card_info_error));
                }
            }
        }
    }


    private void processPaymentForDelivery(String nonce, String card_type, String pay_type) {
        if(mIsProcRunning)
            return;
        mIsProcRunning = true;
        showpDialog();
        ApiUtil.process_payment_for_delivery(nonce, new Notify() {
            @Override
            public void onSuccess(Object object) {
                mIsProcRunning = false;
                hidepDialog();
                GlobalFunctions.showToast(context, getString(R.string.card_added));
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }

            @Override
            public void onAbort(Object object) {
                mIsProcRunning = false;
                hidepDialog();
                GlobalFunctions.hideProgressDialog();
                GlobalFunctions.showToast(context, getString(R.string.whoops));
            }

            @Override
            public void onFail() {
                mIsProcRunning = false;
                hidepDialog();
                GlobalFunctions.hideProgressDialog();
                GlobalFunctions.showToast(context, getString(R.string.whoops));
            }
        });
    }

    private void processPayment(String nonce, String card_type, String pay_type) {
        if(mIsProcRunning)
            return;
        mIsProcRunning = true;
        showpDialog();
        ApiUtil.process_payment(nonce, card_type, pay_type, new Notify() {
            @Override
            public void onSuccess(Object object) {
                mIsProcRunning = false;
                hidepDialog();
                App.getInstance().setUsedPoints(GlobalVariables.USED_POINTS_TO_CHECK);
                TransactionResult data = (TransactionResult) object;
                Log.d("Braintree-payment", data.message);
                GlobalFunctions.showToast(context, getString(R.string.card_added));
                Intent intent = new Intent(context, BillActivity.class);
                intent.putExtra("transaction", data.transaction);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAbort(Object object) {
                mIsProcRunning = false;
                hidepDialog();
                GlobalFunctions.hideProgressDialog();
                GlobalFunctions.showToast(context, getString(R.string.whoops));
            }

            @Override
            public void onFail() {
                mIsProcRunning = false;
                hidepDialog();
                GlobalFunctions.hideProgressDialog();
                GlobalFunctions.showToast(context, getString(R.string.whoops));
            }
        });

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
    @Override
    public void onBackPressed() {
        int pos = viewPager.getCurrentItem();
        if (pos > 0) {
            viewPager.setCurrentItem(pos - 1);
        } else {
            if(GlobalVariables.BUSINESS_IS_DELYVERY) {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED,returnIntent);
            } else {
                Intent mIntent = new Intent(context, ScanActivity.class);
                startActivity(mIntent);
            }
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
