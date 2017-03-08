package com.affirm.affirmsdk;

import android.support.annotation.NonNull;
import com.affirm.affirmsdk.models.ErrorResponse;
import com.google.gson.Gson;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class AffirmRequest<T> {
  interface Endpoint {
    String getPath();

    Request completeBuilder(Request.Builder builder);
  }

  interface Callback<T> {
    void onSuccess(T result);

    void onFailure(Throwable throwable);
  }

  private final Class<T> clazz;
  private final String baseUrl;
  private final OkHttpClient client;
  private final Endpoint endpoint;
  private final Gson gson;

  private Call call;

  public AffirmRequest(Class<T> clazz, String baseUrl, OkHttpClient client, Gson gson,
      Endpoint endpoint) {
    this.baseUrl = baseUrl;
    this.client = client;
    this.gson = gson;
    this.endpoint = endpoint;
    this.clazz = clazz;
  }

  void create(@NonNull final Callback<T> callback) {
    final String protocol = baseUrl.contains("http") ? "" : "https://";

    final Request.Builder builder =
        new Request.Builder().url(protocol + baseUrl + endpoint.getPath());

    call = client.newCall(endpoint.completeBuilder(builder));
    call.enqueue(new okhttp3.Callback() {
      @Override public void onFailure(Call call, IOException e) {
        callback.onFailure(e);
      }

      @Override public void onResponse(Call call, Response response) throws IOException {
        if (!response.isSuccessful()) {

          if (response.code() == 403) {
            callback.onFailure(
                new Exception("Got error from checkout request: " + response.code()));
          } else if (response.code() >= 400 && response.code() < 500) {
            final ErrorResponse errorResponse =
                gson.fromJson(response.body().string(), ErrorResponse.class);

            callback.onFailure(new ServerError(errorResponse));
          } else {
            callback.onFailure(
                new Exception("Got error from checkout request: " + response.code()));
          }

          return;
        }
        final T res = gson.fromJson(response.body().string(), clazz);
        callback.onSuccess(res);
      }
    });
  }

  void cancel() {
    if (call != null) {
      call.cancel();
    }
  }
}
