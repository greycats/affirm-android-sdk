package com.affirm.affirmsdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;

import java.util.HashMap;

public class PrequalActivity extends AppCompatActivity
    implements AffirmWebViewClient.Callbacks, PopUpWebChromeClient.Callbacks {
  private static final String BASE_URL_EXTRA = "BASE_URL_EXTRA";
  private static final String PROTOCOL = "https://";
  private static final String REFERRING_URL = "https://androidsdk/";

  private static final String AMOUNT = "AMOUNT";
  private static final String API_KEY = "API_KEY";
  private static final String PROMO_ID = "PROMO_ID";
  private static final String MAP_EXTRA = "MAP_EXTRA";

  private WebView webView;
  private View progressIndicator;

  private String baseUrlExtra;
  private HashMap<String, String> map;

  static void launch(@NonNull Context context, @NonNull String apiKey, float amount,
      @Nullable String promoId, @NonNull String baseUrl) {
    final Intent intent = new Intent(context, PrequalActivity.class);

    final HashMap<String, String> map = new HashMap<>();
    map.put(AMOUNT, String.valueOf(amount));
    map.put(API_KEY, apiKey);
    map.put(PROMO_ID, promoId);

    intent.putExtra(BASE_URL_EXTRA, baseUrl);
    intent.putExtra(MAP_EXTRA, map);
    context.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ViewUtils.hideActionBar(this);

    if (savedInstanceState != null) {
      map = (HashMap<String, String>) savedInstanceState.getSerializable(MAP_EXTRA);
      baseUrlExtra = savedInstanceState.getString(BASE_URL_EXTRA);
    } else {
      map = (HashMap<String, String>) getIntent().getSerializableExtra(MAP_EXTRA);
      baseUrlExtra = getIntent().getStringExtra(BASE_URL_EXTRA);
    }

    setContentView(R.layout.activity_webview);
    webView = findViewById(R.id.webview);
    progressIndicator = findViewById(R.id.progressIndicator);

    setupWebview();

    loadWebview();
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putString(BASE_URL_EXTRA, baseUrlExtra);
    outState.putSerializable(MAP_EXTRA, map);
  }

  private void setupWebview() {
    AffirmUtils.debuggableWebView(this);
    webView.setWebViewClient(new AffirmWebViewClient(this) {
      @Override
      boolean hasCallbackUrl(WebView view, String url) {
        if (url.equals(REFERRING_URL)) {
          finish();
          return true;
        }
        return false;
      }
    });
    webView.setWebChromeClient(new PopUpWebChromeClient(this));
  }

  private void loadWebview() {
    String path;
    if (TextUtils.isEmpty(map.get(PROMO_ID))) {
      path = String.format(
              "/apps/prequal?public_api_key=%s&unit_price=%s&isSDK=true&use_promo=True"
                      + "&referring_url=%s",
              map.get(API_KEY), map.get(AMOUNT), REFERRING_URL);
    } else {
      path = String.format(
              "/apps/prequal?public_api_key=%s&unit_price=%s&promo_external_id=%s&isSDK=true"
                      + "&use_promo=True&referring_url=%s",
              map.get(API_KEY), map.get(AMOUNT), map.get(PROMO_ID), REFERRING_URL);
    }
    webView.loadUrl(PROTOCOL + baseUrlExtra + path);
  }

  @Override public void onWebViewCancellation() {
    finish();
  }

  @Override public void onWebViewError(@NonNull Throwable error) {
    finish();
  }

  @Override public void onWebViewPageLoaded() {

  }

  @Override public void chromeLoadCompleted() {
    progressIndicator.setVisibility(View.GONE);
  }
}

