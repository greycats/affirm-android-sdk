package com.affirm.affirmsdk.models;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue public abstract class PromoTerm {
  public abstract Integer minimumLoanAmount();

  public abstract Float apr();

  public abstract Integer termLength();

  public static TypeAdapter<PromoTerm> typeAdapter(Gson gson) {
    return new AutoValue_PromoTerm.GsonTypeAdapter(gson);
  }

  public static Builder builder() {
    return new AutoValue_PromoTerm.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder public abstract static class Builder {
    public abstract Builder setApr(Float value);

    public abstract Builder setTermLength(Integer value);

    public abstract Builder setMinimumLoanAmount(Integer value);

    public abstract PromoTerm build();
  }
}
