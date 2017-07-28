package com.affirm.affirmsdk;

import com.affirm.affirmsdk.AffirmRequest.Endpoint;
import okhttp3.Request;

final class NewPromoEndpoint implements Endpoint {
  private final String promoId;
  private final String publicKey;

  NewPromoEndpoint(String promoId, String publicKey) {
    this.promoId = promoId;
    this.publicKey = publicKey;
  }

  @Override public String getPath() {
    return String.format("/platform/public/promos/promo_set/%s/%s.json", publicKey, promoId);
  }

  @Override public Request completeBuilder(Request.Builder builder) {
    return builder.get().build();
  }
}
