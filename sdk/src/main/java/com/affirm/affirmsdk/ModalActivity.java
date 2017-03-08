package com.affirm.affirmsdk;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import com.affirm.affirmsdk.views.ProgressIndicator;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class ModalActivity extends AppCompatActivity implements AffirmWebViewClient.Callbacks {
  private static final String MAP_EXTRA = "MAP_EXTRA";
  private static final String RAW_RES_ID_EXTRA = "STRING_EXTRA";
  private static final String BASE_URL_EXTRA = "BASE_URL_EXTRA";

  private static final String JS_PATH = "/js/v2/affirm.js";
  private static final String PROTOCOL = "https://";

  private static final String AMOUNT = "AMOUNT";
  private static final String MODAL_ID = "MODAL_ID";
  private static final String API_KEY = "API_KEY";
  private static final String JAVASCRIPT = "JAVASCRIPT";
  private static final String CANCEL_URL = "CANCEL_URL";

  private WebView webView;
  private ProgressIndicator progressIndicator;
  private @RawRes int rawResId;
  private HashMap<String, String> map;
  private String baseUrlExtra;

  static void launch(@NonNull Activity activity, @NonNull String apiKey, @NonNull float amount,
      @NonNull String baseUrl, boolean productModal, String modalId) {
    final Intent intent = new Intent(activity, ModalActivity.class);
    final String stringAmount = String.valueOf(AffirmUtils.decimalDollarsToIntegerCents(amount));
    final String fullPath = PROTOCOL + baseUrl + JS_PATH;

    final HashMap<String, String> map = new HashMap<>();
    map.put(AMOUNT, stringAmount);
    map.put(MODAL_ID, modalId);
    map.put(API_KEY, apiKey);
    map.put(JAVASCRIPT, fullPath);
    map.put(CANCEL_URL, AffirmWebViewClient.AFFIRM_CANCELLATION_URL);

    intent.putExtra(BASE_URL_EXTRA, baseUrl);
    intent.putExtra(RAW_RES_ID_EXTRA,
        productModal ? R.raw.product_modal_template : R.raw.site_modal_template);
    intent.putExtra(MAP_EXTRA, map);

    activity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ViewUtils.hideActionBar(this);

    if (savedInstanceState != null) {
      map = (HashMap<String, String>) savedInstanceState.getSerializable(MAP_EXTRA);
      rawResId = savedInstanceState.getInt(RAW_RES_ID_EXTRA);
      baseUrlExtra = savedInstanceState.getString(BASE_URL_EXTRA);
    } else {
      map = (HashMap<String, String>) getIntent().getSerializableExtra(MAP_EXTRA);
      rawResId = getIntent().getIntExtra(RAW_RES_ID_EXTRA, 0);
      baseUrlExtra = getIntent().getStringExtra(BASE_URL_EXTRA);
    }

    setContentView(R.layout.activity_product);
    webView = (WebView) findViewById(R.id.webview);
    progressIndicator = (ProgressIndicator) findViewById(R.id.progressIndicator);

    setupWebview();

    loadWebview();
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putInt(RAW_RES_ID_EXTRA, rawResId);
    outState.putSerializable(MAP_EXTRA, map);
  }

  private void setupWebview() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && 0 != (getApplicationInfo().flags
        & ApplicationInfo.FLAG_DEBUGGABLE)) {
      WebView.setWebContentsDebuggingEnabled(true);
    }
    webView.setWebViewClient(new AffirmWebViewClient(this));
    webView.getSettings().setJavaScriptEnabled(true);
    webView.getSettings().setDomStorageEnabled(true);
    webView.getSettings().setSupportMultipleWindows(true);
    webView.setVerticalScrollBarEnabled(false);
    webView.setWebChromeClient(new PopUpWebChromeClient());
  }

  private String initialHtml() {
    String html;
    try {
      final InputStream ins = getResources().openRawResource(rawResId);
      html = AffirmUtils.readInputStream(ins);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return AffirmUtils.replacePlaceholders(html, map);
  }

  private void loadWebview() {
    final String html = initialHtml();
    webView.loadDataWithBaseURL(PROTOCOL + baseUrlExtra, html, "text/html", "utf-8", null);
  }

  @Override public void onWebViewCancellation() {
    finish();
  }

  @Override public void onWebViewConfirmation(@NonNull String checkoutToken) {
    finish();
  }

  @Override public void onWebViewError(@NonNull Throwable error) {
    finish();
  }

  @Override public void onWebViewPageLoaded() {
    progressIndicator.hideAnimated();
  }
}

