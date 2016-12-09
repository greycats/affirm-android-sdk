package com.affirm.affirmsdk.models;

import android.support.annotation.Nullable;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

// {"field": "merchant.public_api_key", "message": "You have provided an invalid public API key.",
// "code": "public-api-key-invalid", "type": "invalid_request", "status_code": 400}
@AutoValue public abstract class ErrorResponse {
  @Nullable public abstract String field();

  public abstract String message();

  @SerializedName("status_code") public abstract String status();

  public static TypeAdapter<ErrorResponse> typeAdapter(Gson gson) {
    return new AutoValue_ErrorResponse.GsonTypeAdapter(gson);
  }
}
