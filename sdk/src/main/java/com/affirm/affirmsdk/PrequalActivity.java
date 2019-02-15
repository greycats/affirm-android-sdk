package com.affirm.affirmsdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;

public class PrequalActivity extends AppCompatActivity
    implements AffirmWebViewClient.Callbacks, PopUpWebChromeClient.Callbacks {
  private static final String EXTRA_PATH = "EXTRA_PATH";
  private static final String BASE_URL_EXTRA = "BASE_URL_EXTRA";
  private static final String REFERRING_URL = "REFERRING_URL";
  private static final String PROTOCOL = "https://";

  private WebView webView;
  private View progressIndicator;

  private String path;
  private String referringUrl;
  private String baseUrlExtra;

  static void launch(@NonNull Context context, @NonNull String baseUrl, @NonNull String path, @NonNull String referringUrl) {
    final Intent intent = new Intent(context, PrequalActivity.class);
    intent.putExtra(EXTRA_PATH, path);
    intent.putExtra(REFERRING_URL, referringUrl);
    intent.putExtra(BASE_URL_EXTRA, baseUrl);
    context.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ViewUtils.hideActionBar(this);

    if (savedInstanceState != null) {
      path = savedInstanceState.getString(EXTRA_PATH);
      referringUrl = savedInstanceState.getString(REFERRING_URL);
      baseUrlExtra = savedInstanceState.getString(BASE_URL_EXTRA);
    } else {
      path = getIntent().getStringExtra(EXTRA_PATH);
      referringUrl = getIntent().getStringExtra(REFERRING_URL);
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

    outState.putString(EXTRA_PATH, path);
    outState.putString(REFERRING_URL, referringUrl);
    outState.putString(BASE_URL_EXTRA, baseUrlExtra);
  }

  private void setupWebview() {
    AffirmUtils.debuggableWebView(this);
    webView.setWebViewClient(new AffirmWebViewClient(this) {
      @Override
      boolean hasCallbackUrl(WebView view, String url) {
        if (url.equals(referringUrl)) {
          finish();
          return true;
        }
        return false;
      }
    });
    webView.setWebChromeClient(new PopUpWebChromeClient(this));
  }

  private void loadWebview() {
    runOnUiThread(new Runnable() {
      @Override public void run() {
        webView.loadUrl(PROTOCOL + baseUrlExtra + path);
      }
    });
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

