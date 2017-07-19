package com.affirm.affirmsdk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.affirm.affirmsdk.models.CardDetails;
import com.affirm.affirmsdk.models.CheckoutResponse;

import static com.affirm.affirmsdk.Tracker.TrackingEvent.VCN_CHECKOUT_CREATION_FAIL;
import static com.affirm.affirmsdk.Tracker.TrackingEvent.VCN_CHECKOUT_CREATION_SUCCESS;
import static com.affirm.affirmsdk.Tracker.TrackingEvent.VCN_CHECKOUT_WEBVIEW_FAIL;
import static com.affirm.affirmsdk.Tracker.TrackingEvent.VCN_CHECKOUT_WEBVIEW_SUCCESS;
import static com.affirm.affirmsdk.Tracker.TrackingLevel.ERROR;
import static com.affirm.affirmsdk.Tracker.TrackingLevel.INFO;

public class VcnCheckoutPresenter implements Presentable<VcnCheckoutPresenter.Interface> {

  private final Tracker tracker;
  private final AffirmRequest<CheckoutResponse> checkoutRequest;

  VcnCheckoutPresenter(Tracker tracker, AffirmRequest<CheckoutResponse> checkoutRequest) {
    this.tracker = tracker;
    this.checkoutRequest = checkoutRequest;
  }

  interface Interface {
    void finishWithError(@Nullable String error);

    void finishWithSuccess(@NonNull CardDetails cardDetails);

    void loadWebView(@NonNull CheckoutResponse response);
  }

  private Interface page;

  @Override public void onAttach(Interface page) {
    this.page = page;
    startCheckout();
  }

  @Override public void onDetach() {
    checkoutRequest.cancel();
    page = null;
  }

  void onWebViewError(@NonNull Throwable error) {
    tracker.track(VCN_CHECKOUT_WEBVIEW_FAIL, ERROR, null);
    if (page != null) {
      page.finishWithError(error.toString());
    }
  }

  void onWebViewConfirmation(@NonNull CardDetails cardDetails) {
    tracker.track(VCN_CHECKOUT_WEBVIEW_SUCCESS, INFO, null);
    if (page != null) {
      page.finishWithSuccess(cardDetails);
    }
  }

  private void startCheckout() {
    checkoutRequest.create(new AffirmRequest.Callback<CheckoutResponse>() {
      @Override public void onFailure(Throwable throwable) {
        tracker.track(VCN_CHECKOUT_CREATION_FAIL, ERROR, null);
        if (page != null) {
          page.finishWithError(throwable.getMessage());
        }
      }

      @Override public void onSuccess(final CheckoutResponse result) {
        tracker.track(VCN_CHECKOUT_CREATION_SUCCESS, INFO, null);
        if (page != null) {
          page.loadWebView(result);
        }
      }
    });
  }
}
