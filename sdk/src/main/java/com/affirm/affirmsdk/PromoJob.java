package com.affirm.affirmsdk;

import android.text.SpannableString;
import android.widget.TextView;
import com.affirm.affirmsdk.di.AffirmInjector;
import com.affirm.affirmsdk.models.PricingResponse;
import com.affirm.affirmsdk.models.PromoResponse;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;

class PromoJob {

  private final String baseUrl;
  private final TextView textView;
  private final String publicKey;
  private final String promoId;
  private final float amount;
  private final AffirmLogoType logoType;
  private final AffirmColor affirmColor;
  private final PromoCallback callback;
  private final Gson gson;
  private final OkHttpClient okHttpClient;

  private AffirmRequest currentRequest;
  private boolean isCancelled = false;

  public PromoJob(AffirmInjector component, String publicKey, String baseUrl, TextView textView,
      String promoId, float amount, AffirmLogoType logoType, AffirmColor affirmColor,
      PromoCallback callback) {
    this.baseUrl = baseUrl;
    this.textView = textView;
    this.publicKey = publicKey;
    this.promoId = promoId;
    this.amount = amount;
    this.logoType = logoType;
    this.affirmColor = affirmColor;
    this.callback = callback;
    this.gson = component.provideGson();
    this.okHttpClient = component.provideOkHttpClient();
  }

  CancellableRequest getPromo() {

    final AffirmRequest.Endpoint endpoint = new PromoEndpoint(promoId, publicKey);
    currentRequest =
        new AffirmRequest<>(PromoResponse.class, baseUrl, okHttpClient, gson, endpoint);

    currentRequest.create(new AffirmRequest.Callback<PromoResponse>() {
      @Override public void onSuccess(PromoResponse result) {
        if (!isCancelled) {
          getPricing(result);
        }
      }

      @Override public void onFailure(Throwable throwable) {
        returnError(throwable);
      }
    });

    return new CancellableRequest() {
      @Override public void cancelRequest() {
        isCancelled = true;
        if (currentRequest != null) {
          currentRequest.cancel();
        }
      }
    };
  }

  private void getPricing(final PromoResponse promoResponse) {

    final AffirmRequest.Endpoint endpoint = new PricingRequest(publicKey, amount, promoResponse);
    currentRequest =
        new AffirmRequest<>(PricingResponse.class, baseUrl, okHttpClient, gson, endpoint);

    currentRequest.create(new AffirmRequest.Callback<PricingResponse>() {
      @Override public void onSuccess(PricingResponse result) {
        updateSpan(promoResponse, result);

        callback.onPromoWritten(textView);
      }

      @Override public void onFailure(Throwable throwable) {
        returnError(throwable);
      }
    });
  }

  private void returnError(final Throwable e) {
    textView.post(new Runnable() {
      @Override public void run() {
        callback.onFailure(textView, e);
      }
    });
  }

  private void updateSpan(PromoResponse promoResponse, PricingResponse pricingResponse) {
    final PromoSpannable promoSpannable = new PromoSpannable();
    final SpannableString spannableString =
        promoSpannable.spannableFromEditText(textView, promoResponse.pricingTemplate(),
            pricingResponse.paymentString(), logoType, affirmColor);

    textView.post(new Runnable() {
      @Override public void run() {
        textView.setText(spannableString);
      }
    });
  }
}
