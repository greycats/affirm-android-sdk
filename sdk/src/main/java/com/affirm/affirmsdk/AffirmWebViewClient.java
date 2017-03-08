package com.affirm.affirmsdk;

import android.support.annotation.NonNull;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public final class AffirmWebViewClient extends WebViewClient {
  public static final String AFFIRM_CONFIRMATION_URL = "affirm://checkout/confirmed";
  public static final String AFFIRM_CANCELLATION_URL = "affirm://checkout/cancelled";

  private Callbacks callbacks;

  public AffirmWebViewClient(@NonNull Callbacks callbacks) {
    this.callbacks = callbacks;
  }

  @Override public void onPageFinished(WebView view, String url) {
    super.onPageFinished(view, url);
    callbacks.onWebViewPageLoaded();
  }

  @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
    if (url.contains(AFFIRM_CONFIRMATION_URL)) {
      final String token = url.split("checkout_token=")[1];
      callbacks.onWebViewConfirmation(token);
      return true;
    } else if (url.contains(AFFIRM_CANCELLATION_URL)) {
      callbacks.onWebViewCancellation();
      return true;
    }

    return false;
  }

  @Override
  public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
    callbacks.onWebViewError(new Exception(error.toString()));
  }

  public interface Callbacks {
    void onWebViewConfirmation(@NonNull String checkoutToken);

    void onWebViewCancellation();

    void onWebViewError(@NonNull Throwable error);

    void onWebViewPageLoaded();
  }
}
