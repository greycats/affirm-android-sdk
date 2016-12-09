package com.affirm.affirmsdk.models;

import android.os.Parcelable;
import android.support.annotation.Nullable;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue public abstract class Merchant implements Parcelable {
  public static Builder builder() {
    return new AutoValue_Merchant.Builder();
  }

  public static TypeAdapter<Merchant> typeAdapter(Gson gson) {
    return new AutoValue_Merchant.GsonTypeAdapter(gson);
  }

  @SerializedName("public_api_key") public abstract String publicApiKey();

  @SerializedName("user_confirmation_url") public abstract String confirmationUrl();

  @SerializedName("user_cancel_url") public abstract String cancelUrl();

  @Nullable @SerializedName("name") public abstract String name();

  @AutoValue.Builder public abstract static class Builder {
    public abstract Builder setPublicApiKey(String value);

    public abstract Builder setConfirmationUrl(String value);

    public abstract Builder setCancelUrl(String value);

    public abstract Builder setName(String value);

    public abstract Merchant build();
  }
}
