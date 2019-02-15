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
  private final boolean showCta;

  PromoEndpoint(@Nullable String promoId, int centAmount, @NonNull String publicKey, boolean showCta) {
    this.promoId = promoId;
    this.centAmount = centAmount;
    this.publicKey = publicKey;
    this.showCta = showCta;
  }

  @Override public String getPath() {
    if (TextUtils.isEmpty(promoId)) {
      return String.format(
              "/api/promos/v2/%s?is_sdk=true&field=ala&amount=%d&show_cta=%s",
              publicKey, centAmount, showCta);
    } else {
      return String.format(
              "/api/promos/v2/%s?is_sdk=true&field=ala&amount=%d&show_cta=%s&promo_external_id=%s",
              publicKey, centAmount, showCta, promoId);
    }
  }

  @Override public Request completeBuilder(Request.Builder builder) {
    return builder.get().build();
  }
}
