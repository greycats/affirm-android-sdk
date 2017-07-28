package com.affirm.affirmsdk;

import android.text.SpannableString;
import android.widget.TextView;
import com.affirm.affirmsdk.models.NewPromoResponse;
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
  private final OkHttpClient client;
  private final Tracker tracker;

  private AffirmRequest currentRequest;
  private boolean isCancelled = false;

  PromoJob(Gson gson, OkHttpClient client, Tracker tracker, String publicKey, String baseUrl,
      TextView textView, String promoId, float amount, AffirmLogoType logoType,
      AffirmColor affirmColor, PromoCallback callback) {
    this.baseUrl = baseUrl;
    this.textView = textView;
    this.publicKey = publicKey;
    this.promoId = promoId;
    this.amount = amount;
    this.logoType = logoType;
    this.affirmColor = affirmColor;
    this.callback = callback;
    this.gson = gson;
    this.client = client;
    this.tracker = tracker;
  }

  CancellableRequest getPromo() {
    if (promoId.startsWith("promo_set")) {
      getNewPromoResponse();
    } else {
      getPromoResponse();
    }

    return new CancellableRequest() {
      @Override public void cancelRequest() {
        isCancelled = true;
        if (currentRequest != null) {
          currentRequest.cancel();
        }
      }
    };
  }

  private void getPromoResponse() {
    final AffirmRequest.Endpoint endpoint = new PromoEndpoint(promoId, publicKey);
    final AffirmRequest<PromoResponse> request =
        new AffirmRequest<>(PromoResponse.class, baseUrl, client, gson, endpoint, tracker);
    currentRequest = request;

    request.create(new AffirmRequest.Callback<PromoResponse>() {
      @Override public void onSuccess(PromoResponse result) {
        if (!isCancelled) {
          getPricing(result);
        }
      }

      @Override public void onFailure(Throwable throwable) {
        returnError(throwable);
      }
    });
  }

  private void getNewPromoResponse() {
    final AffirmRequest.Endpoint endpoint = new NewPromoEndpoint(promoId, publicKey);
    final AffirmRequest<NewPromoResponse> request =
        new AffirmRequest<>(NewPromoResponse.class, baseUrl, client, gson, endpoint, tracker);
    currentRequest = request;

    request.create(new AffirmRequest.Callback<NewPromoResponse>() {
      @Override public void onSuccess(NewPromoResponse result) {
        if (!isCancelled) {
          getPricing(result.toPromoResponse(AffirmUtils.decimalDollarsToIntegerCents(amount)));
        }
      }

      @Override public void onFailure(Throwable throwable) {
        returnError(throwable);
      }
    });
  }

  private void getPricing(final PromoResponse promoResponse) {

    final AffirmRequest.Endpoint endpoint = new PricingEndpoint(publicKey, amount, promoResponse);
    final AffirmRequest<PricingResponse> request =
        new AffirmRequest<>(PricingResponse.class, baseUrl, client, gson, endpoint, tracker);
    currentRequest = request;

    request.create(new AffirmRequest.Callback<PricingResponse>() {
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
