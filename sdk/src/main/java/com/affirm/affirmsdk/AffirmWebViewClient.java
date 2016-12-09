package com.affirm.affirmsdk;

import android.support.annotation.NonNull;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public final class AffirmWebViewClient extends WebViewClient {
  public static final String AFFIRM_CHECKOUT_CONFIRMATION_URL = "affirm://checkout/confirmed";
  public static final String AFFIRM_CHECKOUT_CANCELLATION_URL = "affirm://checkout/cancelled";

  private Callbacks callbacks;

  public AffirmWebViewClient(@NonNull Callbacks callbacks) {
    this.callbacks = callbacks;
  }

  @Override public void onPageFinished(WebView view, String url) {
    super.onPageFinished(view, url);
    callbacks.onCheckoutPageLoaded();
  }

  @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
    if (url.contains(AFFIRM_CHECKOUT_CONFIRMATION_URL)) {
      final String token = url.split("checkout_token=")[1];
      callbacks.onCheckoutConfirmation(token);
      return true;
    } else if (url.contains(AFFIRM_CHECKOUT_CANCELLATION_URL)) {
      callbacks.onCheckoutCancellation();
      return true;
    }

    return false;
  }

  @Override
  public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
    callbacks.onCheckoutError(new Exception(error.toString()));
  }

  public interface Callbacks {
    void onCheckoutConfirmation(@NonNull String checkoutToken);

    void onCheckoutCancellation();

    void onCheckoutError(@NonNull Exception error);

    void onCheckoutPageLoaded();
  }
}
