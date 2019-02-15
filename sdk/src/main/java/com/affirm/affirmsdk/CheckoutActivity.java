package com.affirm.affirmsdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import com.affirm.affirmsdk.di.AffirmInjector;
import com.affirm.affirmsdk.models.Checkout;
import com.affirm.affirmsdk.models.CheckoutResponse;
import com.affirm.affirmsdk.models.Merchant;

public final class CheckoutActivity extends AppCompatActivity
    implements CheckoutWebViewClient.Callbacks, CheckoutPresenter.Interface,
    PopUpWebChromeClient.Callbacks {

  public static final String CHECKOUT_TOKEN = "checkout_token";
  public static final String CHECKOUT_ERROR = "checkout_error";
  public static final int RESULT_ERROR = -8575;
  private static final String MERCHANT_PUBLIC_KEY_EXTRA = "merchant_extra";
  private static final String CHECKOUT_EXTRA = "checkout_extra";
  private static final String NAME_EXTRA = "name_extra";
  private WebView webView;
  private View progressIndicator;

  private Affirm.Environment env;
  private CheckoutPresenter presenter;

  static void launchCheckout(@NonNull Activity activity, int requestCode,
      @NonNull String merchantPublicKey, @NonNull Checkout checkout, @Nullable String name) {

    final Intent intent = new Intent(activity, CheckoutActivity.class);

    intent.putExtra(MERCHANT_PUBLIC_KEY_EXTRA, merchantPublicKey);
    intent.putExtra(CHECKOUT_EXTRA, checkout);
    intent.putExtra(NAME_EXTRA, name);

    activity.startActivityForResult(intent, requestCode);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    AffirmInjector component = AffirmInjector.instance();

    final String merchantPublicKey = getIntent().getStringExtra(MERCHANT_PUBLIC_KEY_EXTRA);
    final String name = getIntent().getStringExtra(NAME_EXTRA);
    final Checkout checkout = getIntent().getParcelableExtra(CHECKOUT_EXTRA);
    env = component.getEnv();

    final Merchant merchant = Merchant.builder()
        .setPublicApiKey(merchantPublicKey)
        .setConfirmationUrl(AffirmWebViewClient.AFFIRM_CONFIRMATION_URL)
        .setCancelUrl(AffirmWebViewClient.AFFIRM_CANCELLATION_URL)
        .setName(name)
        .build();

    ViewUtils.hideActionBar(this);

    setContentView(R.layout.activity_webview);
    webView = findViewById(R.id.webview);
    progressIndicator = findViewById(R.id.progressIndicator);

    setupWebview();

    final CheckoutEndpoint endpoint =
        new CheckoutEndpoint(merchant, checkout, component.provideGson());
    final AffirmRequest<CheckoutResponse> checkoutRequest =
        new AffirmRequest<>(CheckoutResponse.class, env.baseUrl1, component.provideOkHttpClient(),
            component.provideGson(), endpoint, component.provideTracking());
    presenter = new CheckoutPresenter(component.provideTracking(), checkoutRequest);
    presenter.onAttach(this);
  }

  private void setupWebview() {
    AffirmUtils.debuggableWebView(this);
    webView.setWebViewClient(new CheckoutWebViewClient(this));
    webView.setWebChromeClient(new PopUpWebChromeClient(this));
  }

  @Override protected void onDestroy() {
    presenter.onDetach();
    clearCookies();
    webView.destroy();
    super.onDestroy();
  }

  @SuppressWarnings("deprecation") public void clearCookies() {
    final CookieManager cookieManager = CookieManager.getInstance();
    final CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(this);
    CookiesUtil.clearCookieByUrl("https://" + env.baseUrl1, cookieManager, cookieSyncManager);
  }

  // -- PopUpWebChromeClient.Callbacks

  @Override public void chromeLoadCompleted() {
    progressIndicator.setVisibility(View.GONE);
  }

  // -- AffirmWebViewClient.Callbacks

  @Override public void onWebViewCancellation() {
    setResult(RESULT_CANCELED);
    finish();
  }

  @Override public void onWebViewError(@NonNull Throwable error) {
    presenter.onWebViewError(error);
  }

  @Override public void onWebViewConfirmation(@NonNull String checkoutToken) {
    presenter.onWebViewConfirmation(checkoutToken);
  }

  @Override public void onWebViewPageLoaded() {
  }

  // -- CheckoutPresenter.Interface

  @Override public void finishWithError(@Nullable String message) {
    final Intent intent = new Intent();
    intent.putExtra(CHECKOUT_ERROR, message);
    setResult(RESULT_ERROR, intent);
    finish();
  }

  @Override public void finishWithSuccess(@NonNull String token) {
    final Intent intent = new Intent();
    intent.putExtra(CHECKOUT_TOKEN, token);
    setResult(RESULT_OK, intent);
    finish();
  }

  @Override public void loadWebView(@NonNull final String url) {
    runOnUiThread(new Runnable() {
      @Override public void run() {
        webView.loadUrl(url);
      }
    });
  }
}
