package com.affirm.affirmsdk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.affirm.affirmsdk.AffirmRequest.Endpoint;
import okhttp3.Request;

final class PromoEndpoint implements Endpoint {
  private final String promoId;
  private final String publicKey;
  private final int centAmount;

  PromoEndpoint(@Nullable String promoId, int centAmount, @NonNull String publicKey) {
    this.promoId = promoId;
    this.centAmount = centAmount;
    this.publicKey = publicKey;
  }

  @Override public String getPath() {
    if (TextUtils.isEmpty(promoId)) {
      return String.format(
              "/api/promos/v2/%s?is_sdk=true&field=ala&amount=%d",
              publicKey, centAmount);
    } else {
      return String.format(
              "/api/promos/v2/%s?is_sdk=true&field=ala&amount=%d&promo_external_id=%s",
              publicKey, centAmount, promoId);
    }
  }

  @Override public Request completeBuilder(Request.Builder builder) {
    return builder.get().build();
  }
}
