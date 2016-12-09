package com.affirm.affirmsdk.models;

import android.os.Parcelable;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue public abstract class Item implements Parcelable {
  public static Builder builder() {
    return new AutoValue_Item.Builder();
  }

  public static TypeAdapter<Item> typeAdapter(Gson gson) {
    return new AutoValue_Item.GsonTypeAdapter(gson);
  }

  @SerializedName("display_name") public abstract String displayName();

  public abstract String sku();

  @SerializedName("unit_price") public abstract Integer unitPrice();

  public abstract Integer qty();

  @SerializedName("item_url") public abstract String url();

  @SerializedName("item_image_url") public abstract String imageUrl();

  @AutoValue.Builder public abstract static class Builder {
    public abstract Builder setDisplayName(String value);

    public abstract Builder setSku(String value);

    public abstract Builder setUnitPrice(Integer value);

    public abstract Builder setQty(Integer value);

    public abstract Builder setUrl(String value);

    public abstract Builder setImageUrl(String value);

    public abstract Item build();
  }
}
