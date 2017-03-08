package com.affirm.affirmsdk;

import com.affirm.affirmsdk.models.PromoResponse;
import okhttp3.Request;

class PricingRequest implements AffirmRequest.Endpoint {
  private final String publicKey;
  private final float amount;
  private final PromoResponse promoResponse;

  public PricingRequest(String publicKey, float amount, PromoResponse promoResponse) {
    this.publicKey = publicKey;
    this.amount = amount;
    this.promoResponse = promoResponse;
  }

  @Override public String getPath() {
    return String.format("/promos/payment_estimate_path/%s/%s/%s/%s", publicKey,
        promoResponse.apr().toString(), AffirmUtils.decimalDollarsToIntegerCents(amount),
        promoResponse.termLength().toString());
  }

  @Override public Request completeBuilder(Request.Builder builder) {
    return builder.get().build();
  }
}
