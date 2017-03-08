package com.affirm.affirmsdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import com.affirm.affirmsdk.di.AffirmInjector;
import com.affirm.affirmsdk.models.Checkout;
import com.affirm.affirmsdk.models.CheckoutResponse;
import com.affirm.affirmsdk.models.Merchant;
import com.affirm.affirmsdk.views.ProgressIndicator;
import com.google.gson.Gson;

public final class CheckoutActivity extends AppCompatActivity
    implements AffirmWebViewClient.Callbacks {

  private static final String TAG = CheckoutActivity.class.getCanonicalName();
  public static final String CHECKOUT_TOKEN = "checkout_token";
  public static final String CHECKOUT_ERROR = "checkout_error";
  public static final int RESULT_ERROR = -8575;
  private static final String MERCHANT_PUBLIC_KEY_EXTRA = "merchant_extra";
  private static final String CHECKOUT_EXTRA = "checkout_extra";
  private static final String BASE_URL_EXTRA = "base_url_extra";
  private static final String NAME_EXTRA = "name_extra";
  private WebView webView;
  private ProgressIndicator progressIndicator;

  private Checkout checkout;
  private AffirmRequest<CheckoutResponse> checkoutRequest;
  private String baseUrl;
  private Gson gson;

  static void launchCheckout(@NonNull Activity activity, int requestCode,
      @NonNull String merchantPublicKey, @NonNull Checkout checkout, @NonNull String baseUrl,
      @Nullable String name) {

    final Intent intent = new Intent(activity, CheckoutActivity.class);

    intent.putExtra(MERCHANT_PUBLIC_KEY_EXTRA, merchantPublicKey);
    intent.putExtra(CHECKOUT_EXTRA, checkout);
    intent.putExtra(BASE_URL_EXTRA, baseUrl);
    intent.putExtra(NAME_EXTRA, name);

    activity.startActivityForResult(intent, requestCode);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    AffirmInjector component = new AffirmInjector();

    gson = component.provideGson();

    final String merchantPublicKey = getIntent().getStringExtra(MERCHANT_PUBLIC_KEY_EXTRA);
    final String name = getIntent().getStringExtra(NAME_EXTRA);
    checkout = getIntent().getParcelableExtra(CHECKOUT_EXTRA);
    baseUrl = getIntent().getStringExtra(BASE_URL_EXTRA);

    final Merchant merchant = Merchant.builder()
        .setPublicApiKey(merchantPublicKey)
        .setConfirmationUrl(AffirmWebViewClient.AFFIRM_CONFIRMATION_URL)
        .setCancelUrl(AffirmWebViewClient.AFFIRM_CANCELLATION_URL)
        .setName(name)
        .build();

    final CheckoutEndpoint endpoint = new CheckoutEndpoint(merchant, checkout, gson);
    checkoutRequest =
        new AffirmRequest<>(CheckoutResponse.class, baseUrl, component.provideOkHttpClient(), gson,
            endpoint);

    ViewUtils.hideActionBar(this);

    setContentView(R.layout.activity_checkout);
    webView = (WebView) findViewById(R.id.webview);
    progressIndicator = (ProgressIndicator) findViewById(R.id.progressIndicator);

    setupWebview();

    startCheckout();
  }

  public void setupWebview() {
    webView.setWebViewClient(new AffirmWebViewClient(this));
    webView.getSettings().setJavaScriptEnabled(true);
    webView.getSettings().setDomStorageEnabled(true);
    webView.getSettings().setSupportMultipleWindows(true);
    webView.setVerticalScrollBarEnabled(false);
    webView.setWebChromeClient(new PopUpWebChromeClient());
  }

  private void startCheckout() {
    checkoutRequest.create(new AffirmRequest.Callback<CheckoutResponse>() {
      @Override public void onFailure(Throwable throwable) {
        onWebViewError(throwable);
      }

      @Override public void onSuccess(final CheckoutResponse result) {
        runOnUiThread(new Runnable() {
          @Override public void run() {
            webView.loadUrl(result.redirectUrl());
          }
        });
      }
    });
  }

  @Override protected void onDestroy() {
    checkoutRequest.cancel();
    clearCookies();
    webView.destroy();
    super.onDestroy();
  }

  @SuppressWarnings("deprecation") public void clearCookies() {
    final CookieManager cookieManager = CookieManager.getInstance();
    final CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(this);
    CookiesUtil.clearCookieByUrl("https://" + baseUrl, cookieManager, cookieSyncManager);
  }

  public void finishWithError(@Nullable String message) {
    final Intent intent = new Intent();
    intent.putExtra(CHECKOUT_ERROR, message);
    setResult(RESULT_ERROR, intent);
    finish();
  }

  @Override public void onWebViewCancellation() {
    setResult(RESULT_CANCELED);
    finish();
  }

  @Override public void onWebViewError(@NonNull Throwable error) {
    Log.e(TAG, error.toString());
    finishWithError(error.toString());
  }

  @Override public void onWebViewConfirmation(@NonNull String checkoutToken) {
    final Intent intent = new Intent();
    intent.putExtra(CHECKOUT_TOKEN, checkoutToken);
    setResult(RESULT_OK, intent);
    finish();
  }

  @Override public void onWebViewPageLoaded() {
    progressIndicator.hideAnimated();
  }
}
