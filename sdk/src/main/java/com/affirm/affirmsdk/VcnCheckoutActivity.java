package com.affirm.affirmsdk;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import com.affirm.affirmsdk.di.AffirmInjector;
import com.affirm.affirmsdk.models.CardDetails;
import com.affirm.affirmsdk.models.Checkout;
import com.affirm.affirmsdk.models.CheckoutResponse;
import com.affirm.affirmsdk.models.Merchant;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class VcnCheckoutActivity extends AppCompatActivity
    implements VcnCheckoutWebViewClient.Callbacks, VcnCheckoutPresenter.Interface,
    PopUpWebChromeClient.Callbacks {
  public static final String CREDIT_DETAILS = "credit_details";
  public static final String CHECKOUT_ERROR = "checkout_error";
  public static final int RESULT_ERROR = -8575;
  private static final String MERCHANT_PUBLIC_KEY_EXTRA = "merchant_extra";
  private static final String CHECKOUT_EXTRA = "checkout_extra";
  private static final String NAME_EXTRA = "name_extra";
  private WebView webView;
  private View progressIndicator;

  private Affirm.Environment env;
  private VcnCheckoutPresenter presenter;
  private boolean isForeground = false;

  static void launchCheckout(@NonNull Activity activity, int requestCode,
      @NonNull String merchantPublicKey, @NonNull Checkout checkout, @Nullable String name) {

    final Intent intent = new Intent(activity, VcnCheckoutActivity.class);

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

    final Merchant merchant =
        Merchant.builder().setPublicApiKey(merchantPublicKey).setUseVcn(true).setName(name).build();

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
    presenter = new VcnCheckoutPresenter(component.provideTracking(), checkoutRequest);
    presenter.onAttach(this);
  }

  private void setupWebview() {
    AffirmUtils.debuggableWebView(this);
    webView.setWebViewClient(
        new VcnCheckoutWebViewClient(AffirmInjector.instance().provideGson(), this));
    webView.setWebChromeClient(new PopUpWebChromeClient(this));
    clearCookies();
  }

  private void fixScroll() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      return;
    }

    if (!isForeground) {
      return;
    }

    final int oneDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1,
        getResources().getDisplayMetrics());
    (new Handler()).postDelayed(new Runnable() {
      @Override public void run() {
        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) webView.getLayoutParams();
        final int current = lp.bottomMargin;
        lp.setMargins(0, 0, 0, current == 0 ? -oneDp : 0);
        webView.setLayoutParams(lp);
        fixScroll();
      }
    }, 1000);
  }

  @Override protected void onResume() {
    super.onResume();
    isForeground = true;
    fixScroll();
  }

  @Override protected void onPause() {
    isForeground = false;
    super.onPause();
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

  private String initialHtml(CheckoutResponse response) {
    String html;
    try {
      final InputStream ins = getResources().openRawResource(R.raw.vcn_checkout);
      html = AffirmUtils.readInputStream(ins);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    final HashMap<String, String> map = new HashMap<>();

    map.put("URL", response.redirectUrl());
    map.put("URL2", response.redirectUrl());
    map.put("JS_CALLBACK_ID", response.jsCallbackId());
    map.put("CONFIRM_CB_URL", AffirmWebViewClient.AFFIRM_CONFIRMATION_URL);
    map.put("CANCELLED_CB_URL", AffirmWebViewClient.AFFIRM_CANCELLATION_URL);
    return AffirmUtils.replacePlaceholders(html, map);
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

  @Override public void onWebViewConfirmation(@NonNull CardDetails cardDetails) {
    presenter.onWebViewConfirmation(cardDetails);
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

  @Override public void finishWithSuccess(@NonNull CardDetails cardDetails) {
    final Intent intent = new Intent();
    intent.putExtra(CREDIT_DETAILS, cardDetails);
    setResult(RESULT_OK, intent);
    finish();
  }

  @Override public void loadWebView(@NonNull final CheckoutResponse response) {
    final String html = initialHtml(response);
    final Uri uri = Uri.parse(response.redirectUrl());

    runOnUiThread(new Runnable() {
      @Override public void run() {
        webView.loadDataWithBaseURL("https://" + uri.getHost(), html, "text/html", "utf-8", null);
      }
    });
  }
}
