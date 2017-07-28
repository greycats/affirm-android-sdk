package com.affirm.affirmsdk.models;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue public abstract class NewPromoResponse {
  public abstract PromoSet asLowAs();

  public static TypeAdapter<NewPromoResponse> typeAdapter(Gson gson) {
    return new AutoValue_NewPromoResponse.GsonTypeAdapter(gson);
  }

  public PromoResponse toPromoResponse(int amount) {
    final PromoTerm term = asLowAs().termForAmount(amount);
    return PromoResponse.builder()
        .setApr(term.apr())
        .setPricingTemplate(asLowAs().pricingTemplate())
        .setTermLength(term.termLength())
        .build();
  }

  public static Builder builder() {
    return new AutoValue_NewPromoResponse.Builder();
  }

  @AutoValue.Builder public abstract static class Builder {
    public abstract Builder setAsLowAs(PromoSet value);

    public abstract NewPromoResponse build();
  }
}
