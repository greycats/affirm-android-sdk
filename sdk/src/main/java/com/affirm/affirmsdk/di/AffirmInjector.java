package com.affirm.affirmsdk.di;

import com.affirm.affirmsdk.HeaderInterceptor;
import com.affirm.affirmsdk.models.MyAdapterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;

public class AffirmInjector {

  private Gson gson;
  private OkHttpClient client;

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
}
