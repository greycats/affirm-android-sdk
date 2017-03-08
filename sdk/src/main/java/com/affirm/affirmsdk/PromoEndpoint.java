package com.affirm.affirmsdk;

import okhttp3.Request;

class PromoEndpoint implements AffirmRequest.Endpoint {

  private final String promoId;
  private final String publicKey;

  PromoEndpoint(String promoId, String publicKey) {
    this.promoId = promoId;
    this.publicKey = publicKey;
  }

  @Override public String getPath() {
    return String.format("/platform/public/promos/as_low_as/%s/%s.json", publicKey, promoId);
  }

  @Override public Request completeBuilder(Request.Builder builder) {
    return builder.get().build();
  }
}
