package com.affirm.affirmsdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import com.affirm.affirmsdk.models.Checkout;
import com.affirm.affirmsdk.models.ErrorResponse;
import com.affirm.affirmsdk.models.Merchant;
import com.affirm.affirmsdk.models.MyAdapterFactory;
import com.affirm.affirmsdk.views.ProgressIndicator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public final class CheckoutActivity extends AppCompatActivity
    implements AffirmWebViewClient.Callbacks {

  private static final String TAG = CheckoutActivity.class.getCanonicalName();
  public static final String CHECKOUT_TOKEN = "checkout_token";
  public static final String CHECKOUT_ERROR = "checkout_error";
  public static final int RESULT_ERROR = -8575;
  private static final String MERCHANT_PUBLIC_KEY_EXTRA = "merchant_extra";
  private static final String CHECKOUT_EXTRA = "checkout_extra";
  private static final String FINANCIAL_PRODUCT_KEY_EXTRA = "financial_product_key_extra";
  private static final String BASE_URL_EXTRA = "base_url_extra";
  private static final String NAME_EXTRA = "name_extra";
  private WebView webView;
  private ProgressIndicator progressIndicator;

  private Checkout checkout;
  private CheckoutRequest checkoutRequest;
  private String baseUrl;
  private Gson gson;

  public static void launchCheckout(@NonNull Activity activity, int requestCode,
      @NonNull String merchantPublicKey, @NonNull Checkout checkout,
      @NonNull String financialProductKey, @NonNull String baseUrl, @Nullable String name) {

    final Intent intent = new Intent(activity, CheckoutActivity.class);

    intent.putExtra(MERCHANT_PUBLIC_KEY_EXTRA, merchantPublicKey);
    intent.putExtra(CHECKOUT_EXTRA, checkout);
    intent.putExtra(FINANCIAL_PRODUCT_KEY_EXTRA, financialProductKey);
    intent.putExtra(BASE_URL_EXTRA, baseUrl);
    intent.putExtra(NAME_EXTRA, name);

    activity.startActivityForResult(intent, requestCode);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "onCreate");
    gson = new GsonBuilder().registerTypeAdapterFactory(MyAdapterFactory.create()).create();

    final String merchantPublicKey = getIntent().getStringExtra(MERCHANT_PUBLIC_KEY_EXTRA);
    final String name = getIntent().getStringExtra(NAME_EXTRA);
    checkout = getIntent().getParcelableExtra(CHECKOUT_EXTRA);
    final String financialProductKey = getIntent().getStringExtra(FINANCIAL_PRODUCT_KEY_EXTRA);
    baseUrl = getIntent().getStringExtra(BASE_URL_EXTRA);

    final OkHttpClient.Builder clientBuilder = new OkHttpClient().newBuilder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(false);

    final OkHttpClient client = clientBuilder.build();

    final Merchant merchant = Merchant.builder()
        .setPublicApiKey(merchantPublicKey)
        .setConfirmationUrl(AffirmWebViewClient.AFFIRM_CHECKOUT_CONFIRMATION_URL)
        .setCancelUrl(AffirmWebViewClient.AFFIRM_CHECKOUT_CANCELLATION_URL)
        .setName(name)
        .build();

    checkoutRequest = new CheckoutRequest(merchant, financialProductKey, baseUrl, client, gson);

    hideActionBar();

    setContentView(R.layout.checkout_activity);
    webView = (WebView) findViewById(R.id.webview);
    progressIndicator = (ProgressIndicator) findViewById(R.id.progressIndicator);

    setupWebview();

    startCheckout();
  }

  public void hideActionBar() {
    getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
    if (getActionBar() != null) {
      getActionBar().hide();
    } else if (getSupportActionBar() != null) {
      getSupportActionBar().hide();
    }
  }

  public void setupWebview() {
    webView.setWebViewClient(new AffirmWebViewClient(this));
    webView.getSettings().setJavaScriptEnabled(true);
    webView.getSettings().setDomStorageEnabled(true);
    webView.getSettings().setSupportMultipleWindows(true);
    webView.setVerticalScrollBarEnabled(false);
    webView.setWebChromeClient(new WebChromeClient() {
      @Override public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture,
          Message resultMsg) {
        final WebView.HitTestResult result = view.getHitTestResult();
        final String data = result.getExtra();
        final Context context = view.getContext();
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
        context.startActivity(browserIntent);
        return false;
      }
    });
  }

  private void startCheckout() {

    checkoutRequest.create(checkout, new Callback() {
      @Override public void onFailure(Call call, IOException e) {
        Log.e(TAG, e.toString());
        onCheckoutError(e);
      }

      @Override public void onResponse(Call call, Response response) throws IOException {
        if (!response.isSuccessful()) {
          if (response.code() >= 400 && response.code() < 500) {
            final ErrorResponse errorResponse =
                gson.fromJson(response.body().string(), ErrorResponse.class);
            finishWithError(errorResponse.toString());
          } else {
            finishWithError("Got error from checkout request: " + response.code());
          }
        } else {
          final JsonParser jsonParser = new JsonParser();
          final JsonObject jsonResponse =
              jsonParser.parse(response.body().string()).getAsJsonObject();

          final String url = jsonResponse.get("redirect_url").getAsString();
          Log.d(TAG, "redirect to " + url);

          runOnUiThread(new Runnable() {
            @Override public void run() {
              webView.loadUrl(url);
            }
          });
        }
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

  @Override public void onCheckoutCancellation() {
    setResult(RESULT_CANCELED);
    finish();
  }

  @Override public void onCheckoutError(@NonNull Exception error) {
    Log.e(TAG, error.toString());
    finishWithError(error.toString());
  }

  @Override public void onCheckoutConfirmation(@NonNull String checkoutToken) {
    final Intent intent = new Intent();
    intent.putExtra(CHECKOUT_TOKEN, checkoutToken);
    setResult(RESULT_OK, intent);
    finish();
  }

  @Override public void onCheckoutPageLoaded() {
    progressIndicator.hideAnimated();
  }
}
