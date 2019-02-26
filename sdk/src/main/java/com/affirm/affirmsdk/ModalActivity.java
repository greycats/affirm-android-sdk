package com.affirm.affirmsdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;

import com.affirm.affirmsdk.di.AffirmInjector;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import static com.affirm.affirmsdk.Tracker.TrackingEvent.PRODUCT_WEBVIEW_FAIL;
import static com.affirm.affirmsdk.Tracker.TrackingEvent.SITE_WEBVIEW_FAIL;
import static com.affirm.affirmsdk.Tracker.TrackingLevel.ERROR;

public class ModalActivity extends AppCompatActivity
    implements AffirmWebViewClient.Callbacks, PopUpWebChromeClient.Callbacks {
  private static final String MAP_EXTRA = "MAP_EXTRA";
  private static final String TYPE_EXTRA = "TYPE_EXTRA";
  private static final String BASE_URL_EXTRA = "BASE_URL_EXTRA";

  private static final String JS_PATH = "/js/v2/affirm.js";
  private static final String PROTOCOL = "https://";

  private static final String AMOUNT = "AMOUNT";
  private static final String API_KEY = "API_KEY";
  private static final String JAVASCRIPT = "JAVASCRIPT";
  private static final String CANCEL_URL = "CANCEL_URL";
  private static final String MODAL_ID = "MODAL_ID";

  private WebView webView;
  private View progressIndicator;
  private ModalType type;
  private HashMap<String, String> map;
  private String baseUrlExtra;
  private Tracker tracker;

  enum ModalType {
    // @formatter:off
    PRODUCT(R.raw.modal_template, PRODUCT_WEBVIEW_FAIL),
    SITE(R.raw.modal_template, SITE_WEBVIEW_FAIL);
    // @formatter:on

    @RawRes final int templateRes;
    final Tracker.TrackingEvent failureEvent;

    ModalType(int templateRes, Tracker.TrackingEvent failureEvent) {
      this.templateRes = templateRes;
      this.failureEvent = failureEvent;
    }
  }

  static void launch(@NonNull Context context, @NonNull String apiKey, float amount,
      @NonNull String baseUrl, ModalType type, @Nullable String modalId) {

    final Intent intent = new Intent(context, ModalActivity.class);
    final String stringAmount = String.valueOf(AffirmUtils.decimalDollarsToIntegerCents(amount));
    final String fullPath = PROTOCOL + baseUrl + JS_PATH;

    final HashMap<String, String> map = new HashMap<>();
    map.put(AMOUNT, stringAmount);
    map.put(API_KEY, apiKey);
    map.put(JAVASCRIPT, fullPath);
    map.put(CANCEL_URL, AffirmWebViewClient.AFFIRM_CANCELLATION_URL);
    map.put(MODAL_ID, modalId == null ? "" : modalId);

    intent.putExtra(BASE_URL_EXTRA, baseUrl);
    intent.putExtra(TYPE_EXTRA, type);
    intent.putExtra(MAP_EXTRA, map);

    context.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ViewUtils.hideActionBar(this);

    tracker = AffirmInjector.instance().provideTracking();

    if (savedInstanceState != null) {
      map = (HashMap<String, String>) savedInstanceState.getSerializable(MAP_EXTRA);
      type = (ModalType) savedInstanceState.getSerializable(TYPE_EXTRA);
      baseUrlExtra = savedInstanceState.getString(BASE_URL_EXTRA);
    } else {
      map = (HashMap<String, String>) getIntent().getSerializableExtra(MAP_EXTRA);
      type = (ModalType) getIntent().getSerializableExtra(TYPE_EXTRA);
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

    outState.putInt(TYPE_EXTRA, type.templateRes);
    outState.putSerializable(MAP_EXTRA, map);
  }

  private void setupWebview() {
    AffirmUtils.debuggableWebView(this);
    webView.setWebViewClient(new ModalWebViewClient(this));
    webView.setWebChromeClient(new PopUpWebChromeClient(this));
  }

  private String initialHtml() {
    String html;
    try {
      final InputStream ins = getResources().openRawResource(type.templateRes);
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

  @Override public void onWebViewError(@NonNull Throwable error) {
    tracker.track(type.failureEvent, ERROR, null);
    finish();
  }

  @Override public void onWebViewPageLoaded() {

  }

  // -- PopUpWebChromeClient.Callbacks

  @Override public void chromeLoadCompleted() {
    progressIndicator.setVisibility(View.GONE);
  }
}

