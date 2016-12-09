package com.affirm.affirmsdk.models;

import android.os.Parcelable;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue public abstract class Discount implements Parcelable {
  public static Builder builder() {
    return new AutoValue_Discount.Builder();
  }

  public static TypeAdapter<Discount> typeAdapter(Gson gson) {
    return new AutoValue_Discount.GsonTypeAdapter(gson);
  }

  @SerializedName("discount_display_name") public abstract String displayName();

  @SerializedName("discount_amount") public abstract Integer amount();

  @AutoValue.Builder public abstract static class Builder {
    public abstract Builder setDisplayName(String value);

    public abstract Builder setAmount(Integer value);

    public abstract Discount build();
  }
}
