package com.affirm.affirmsdk;

import android.support.annotation.NonNull;
import com.affirm.affirmsdk.models.Checkout;
import com.affirm.affirmsdk.models.Merchant;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public final class CheckoutRequest {
  private static final String APPLICATION_JSON = "application/json";
  private static final MediaType JSON = MediaType.parse(APPLICATION_JSON + "; charset=utf-8");

  private Merchant merchant;
  private String financialProductKey;
  private String baseUrl;
  private OkHttpClient client;
  private Gson gson;

  private Call checkoutCall;

  public CheckoutRequest(Merchant merchant, String financialProductKey, String baseUrl,
      OkHttpClient client, Gson gson) {
    this.merchant = merchant;
    this.financialProductKey = financialProductKey;
    this.baseUrl = baseUrl;
    this.client = client;
    this.gson = gson;
  }

  public void create(@NonNull Checkout checkout, @NonNull Callback callback) {
    final JsonObject jsonRequest = buildJsonRequest(checkout);
    final RequestBody body = RequestBody.create(JSON, jsonRequest.toString());

    final Request request = new Request.Builder().url("https://" + baseUrl + "/api/v2/checkout/")
        .addHeader("Accept", APPLICATION_JSON)
        .addHeader("Content-Type", APPLICATION_JSON)
        .addHeader("Affirm-User-Agent", "Affirm-Android-SDK")
        .addHeader("Affirm-User-Agent-Version", BuildConfig.VERSION_NAME)
        .post(body)
        .build();

    checkoutCall = client.newCall(request);
    checkoutCall.enqueue(callback);
  }

  public void cancel() {
    if (checkoutCall != null) {
      checkoutCall.cancel();
    }
  }

  private JsonObject buildJsonRequest(Checkout checkout) {
    final JsonObject configJson = new JsonObject();
    final JsonObject jsonRequest = new JsonObject();
    final JsonParser jsonParser = new JsonParser();
    final JsonObject checkoutJson = jsonParser.parse(gson.toJson(checkout)).getAsJsonObject();
    final JsonObject merchantJson = jsonParser.parse(gson.toJson(merchant)).getAsJsonObject();

    configJson.addProperty("user_confirmation_url_action", "GET");
    configJson.addProperty("financial_product_key", financialProductKey);

    checkoutJson.add("merchant", merchantJson);
    checkoutJson.add("config", configJson);
    checkoutJson.addProperty("api_version", "v2");

    jsonRequest.add("checkout", checkoutJson);

    return jsonRequest;
  }
}
