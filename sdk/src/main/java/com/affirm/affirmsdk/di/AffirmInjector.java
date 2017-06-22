package com.affirm.affirmsdk.di;

import android.support.annotation.NonNull;
import com.affirm.affirmsdk.Affirm;
import com.affirm.affirmsdk.Clock;
import com.affirm.affirmsdk.HeaderInterceptor;
import com.affirm.affirmsdk.Tracker;
import com.affirm.affirmsdk.models.MyAdapterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;

public final class AffirmInjector {

  private static AffirmInjector instance;

  private Gson gson;
  private OkHttpClient client;
  private Tracker tracking;
  private final Affirm.Environment env;
  private final String merchantKey;

  public Affirm.Environment getEnv() {
    return env;
  }

  public String getMerchantKey() {
    return merchantKey;
  }

  public static @NonNull AffirmInjector instance() {
    if (instance == null) {
      throw new RuntimeException("AffirmInjector not initialized");
    }

    return instance;
  }

  private AffirmInjector(Affirm.Environment env, String merchantKey) {
    this.env = env;
    this.merchantKey = merchantKey;
    instance = this;
  }

  public Gson provideGson() {
    if (gson == null) {
      gson = new GsonBuilder().registerTypeAdapterFactory(MyAdapterFactory.create()).create();
    }

    return gson;
  }

  public OkHttpClient provideOkHttpClient() {
    if (client == null) {
      final HeaderInterceptor headerInterceptor = new HeaderInterceptor();

      client = new OkHttpClient().newBuilder()
          .connectTimeout(5, TimeUnit.SECONDS)
          .readTimeout(30, TimeUnit.SECONDS)
          .followRedirects(false)
          .addInterceptor(headerInterceptor)
          .build();
    }

    return client;
  }

  public Clock provideClock() {
    return new Clock();
  }

  public Tracker provideTracking() {
    if (tracking == null) {
      tracking = new Tracker(provideOkHttpClient(), getEnv(), getMerchantKey(), provideClock());
    }

    return tracking;
  }

  public static class Builder {

    private Affirm.Environment env;
    private String merchantKey;

    public Builder setEnv(@NonNull Affirm.Environment env) {
      this.env = env;
      return this;
    }

    public Builder setMerchantKey(@NonNull String merchantKey) {
      this.merchantKey = merchantKey;
      return this;
    }

    public AffirmInjector build() {
      if (env == null) {
        throw new IllegalArgumentException("env not set");
      }

      if (merchantKey == null) {
        throw new IllegalArgumentException("merchantKey not set");
      }

      return new AffirmInjector(env, merchantKey);
    }
  }
}