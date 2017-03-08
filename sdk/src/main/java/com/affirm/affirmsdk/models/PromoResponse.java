package com.affirm.affirmsdk.models;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue public abstract class PromoResponse {
  @SerializedName("pricingTemplate") public abstract String pricingTemplate();

  public abstract Float apr();

  @SerializedName("termLength") public abstract Integer termLength();

  public static TypeAdapter<PromoResponse> typeAdapter(Gson gson) {
    return new AutoValue_PromoResponse.GsonTypeAdapter(gson);
  }
}
