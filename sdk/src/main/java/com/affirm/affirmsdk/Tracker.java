package com.affirm.affirmsdk;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static java.sql.DriverManager.println;

public class Tracker {
  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
  private final OkHttpClient client;
  private final AtomicInteger localLogCounter = new AtomicInteger();
  private final String merchantKey;
  private final Affirm.Environment environment;
  private final Clock clock;

  public enum TrackingEvent {
    // @formatter:off
    CHECKOUT_CREATION_FAIL("Checkout creation failed"),
    CHECKOUT_CREATION_SUCCESS("Checkout creation failed"),
    CHECKOUT_WEBVIEW_SUCCESS("CHECKOUT WebView success"),
    CHECKOUT_WEBVIEW_FAIL("Checkout WebView fail"),
    PRODUCT_WEBVIEW_FAIL("Product WebView fail"),
    SITE_WEBVIEW_FAIL("Site WebView fail"),
    NETWORK_ERROR("network error");
    // @formatter:on

    private final String name;

    TrackingEvent(String name) {
      this.name = name;
    }
  }

  enum TrackingLevel {
    INFO("info"), WARNING("warning"), ERROR("error");

    private final String level;

    TrackingLevel(String level) {
      this.level = level;
    }

    protected String getLevel() {
      return this.level;
    }
  }

  public Tracker(@NonNull OkHttpClient client, @NonNull Affirm.Environment environment,
      @NonNull String merchantKey, @NonNull Clock clock) {
    this.client = client;
    this.merchantKey = merchantKey;
    this.environment = environment;
    this.clock = clock;
  }

  void track(@NonNull TrackingEvent event, @NonNull TrackingLevel level,
      @Nullable JsonObject data) {

    final String url = "https://" + environment.kibanaBaseUrl + "/collect";

    final JsonObject json = addTrackingData(event.name, data, level);

    final RequestBody body = RequestBody.create(JSON, json.toString());

    final Request request = new Request.Builder().url(url)
        .addHeader("Content-Type", "application/json")
        .post(body)
        .build();

    final Call call = client.newCall(request);

    call.enqueue(new Callback() {
      @Override public void onFailure(Call call, IOException e) {
        println(e.toString());
      }

      @Override public void onResponse(Call call, Response response) throws IOException {
        println(toString());
      }
    });
  }

  private JsonObject addTrackingData(@NonNull String eventName, @Nullable JsonObject eventData,
      @NonNull TrackingLevel level) {

    final Gson gson = new Gson();
    final JsonObject data = eventData == null ? new JsonObject()
        : gson.fromJson(gson.toJson(eventData, JsonObject.class), JsonObject.class);

    final long timeStamp = clock.now().getTime();
    // Set the log counter and then increment the logCounter
    data.addProperty("local_log_counter", localLogCounter.getAndIncrement());
    data.addProperty("ts", timeStamp);
    data.addProperty("event_name", eventName);
    data.addProperty("app_id", "Android SDK");
    data.addProperty("release", BuildConfig.VERSION_NAME);
    data.addProperty("android_sdk", Build.VERSION.SDK_INT);
    data.addProperty("device_name", Build.MODEL);
    data.addProperty("merchant_key", merchantKey);
    data.addProperty("level", level.getLevel());

    return data;
  }
}
