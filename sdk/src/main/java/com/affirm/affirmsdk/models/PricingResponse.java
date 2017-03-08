package com.affirm.affirmsdk.models;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue public abstract class PricingResponse {
  @SerializedName("payment_string") public abstract String paymentString();

  public static TypeAdapter<PricingResponse> typeAdapter(Gson gson) {
    return new AutoValue_PricingResponse.GsonTypeAdapter(gson);
  }
}
