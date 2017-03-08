package com.affirm.affirmsdk;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HeaderInterceptor implements Interceptor {
  private static final String APPLICATION_JSON = "application/json";

  @Override public Response intercept(Chain chain) throws IOException {
    final Request request = chain.request()
        .newBuilder()
        .addHeader("Accept", APPLICATION_JSON)
        .addHeader("Content-Type", APPLICATION_JSON)
        .addHeader("Affirm-User-Agent", "Affirm-Android-SDK")
        .addHeader("Affirm-User-Agent-Version", BuildConfig.VERSION_NAME)
        .build();

    return chain.proceed(request);
  }
}
