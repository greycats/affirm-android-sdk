package com.affirm.affirmsdk;

import com.affirm.affirmsdk.di.AffirmInjector;
import com.affirm.affirmsdk.models.CheckoutResponse;
import com.affirm.affirmsdk.utils.ResponseFactory;
import com.google.common.truth.Truth;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static com.affirm.affirmsdk.Tracker.TrackingEvent.NETWORK_ERROR;
import static com.affirm.affirmsdk.Tracker.TrackingLevel.ERROR;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;

@RunWith(MockitoJUnitRunner.class) public class AffirmRequestTest {

  @Mock OkHttpClient client;
  @Mock AffirmRequest.Callback<CheckoutResponse> callback;
  @Mock Call call;
  @Mock Tracker tracker;

  Gson gson;
  AffirmRequest.Endpoint endpoint;

  @Before public void setUp() {
    AffirmInjector component = new AffirmInjector.Builder().setEnv(Affirm.Environment.PRODUCTION)
        .setMerchantKey("1111")
        .build();
    gson = component.provideGson();

    endpoint = new AffirmRequest.Endpoint() {
      @Override public String getPath() {
        return "";
      }

      @Override public Request completeBuilder(Request.Builder builder) {
        return builder.build();
      }
    };
  }

  @Test public void createSuccessful() throws Exception {

    final Response response = ResponseFactory.success("{\"redirect_url\":\"http://blahbalh\"}");

    Mockito.when(client.newCall(any(Request.class))).thenReturn(call);
    Mockito.doAnswer(new Answer() {
      @Override public Object answer(InvocationOnMock invocation) throws Throwable {
        ((Callback) invocation.getArguments()[0]).onResponse(call, response);
        return null;
      }
    }).when(call).enqueue(any(Callback.class));

    AffirmRequest<CheckoutResponse> request =
        new AffirmRequest<>(CheckoutResponse.class, "api.affirm.com", client, gson, endpoint,
            tracker);

    request.create(callback);

    Mockito.verify(callback).onSuccess(any(CheckoutResponse.class));
    Mockito.verify(tracker, Mockito.never())
        .track(any(Tracker.TrackingEvent.class), any(Tracker.TrackingLevel.class),
            any(JsonObject.class));
  }

  @Test public void createFails() throws Exception {

    final Response response =
        ResponseFactory.error(400, "{\"status_code\":\"400\", \"message\":\"asdf\"}");

    Mockito.when(client.newCall(any(Request.class))).thenReturn(call);
    Mockito.doAnswer(new Answer() {
      @Override public Object answer(InvocationOnMock invocation) throws Throwable {
        ((Callback) invocation.getArguments()[0]).onResponse(call, response);
        return null;
      }
    }).when(call).enqueue(any(Callback.class));

    AffirmRequest<CheckoutResponse> request =
        new AffirmRequest<>(CheckoutResponse.class, "api.affirm.com", client, gson, endpoint,
            tracker);

    request.create(callback);

    Mockito.verify(callback).onFailure(any(ServerError.class));

    ArgumentCaptor<JsonObject> dataCaptor = ArgumentCaptor.forClass(JsonObject.class);
    Mockito.verify(tracker).track(eq(NETWORK_ERROR), eq(ERROR), dataCaptor.capture());

    final JsonObject data = dataCaptor.getValue();
    Truth.assertThat(400).isEqualTo(data.get("status_code").getAsInt());
    Truth.assertThat("requestId").isEqualTo(data.get("X-Affirm-Request-Id").getAsString());
  }

  @Test public void createNetworkFails() throws Exception {

    final Response response = ResponseFactory.error(500, "");

    Mockito.when(client.newCall(any(Request.class))).thenReturn(call);
    Mockito.doAnswer(new Answer() {
      @Override public Object answer(InvocationOnMock invocation) throws Throwable {
        ((Callback) invocation.getArguments()[0]).onResponse(call, response);
        return null;
      }
    }).when(call).enqueue(any(Callback.class));

    AffirmRequest<CheckoutResponse> request =
        new AffirmRequest<>(CheckoutResponse.class, "api.affirm.com", client, gson, endpoint,
            tracker);

    request.create(callback);

    Mockito.verify(callback).onFailure(any(Exception.class));
    Mockito.verify(tracker).track(eq(NETWORK_ERROR), eq(ERROR), any(JsonObject.class));
  }

  @Test public void createNetworkForbidden() throws Exception {

    final Response response = ResponseFactory.error(403, "");

    Mockito.when(client.newCall(any(Request.class))).thenReturn(call);
    Mockito.doAnswer(new Answer() {
      @Override public Object answer(InvocationOnMock invocation) throws Throwable {
        ((Callback) invocation.getArguments()[0]).onResponse(call, response);
        return null;
      }
    }).when(call).enqueue(any(Callback.class));

    AffirmRequest<CheckoutResponse> request =
        new AffirmRequest<>(CheckoutResponse.class, "api.affirm.com", client, gson, endpoint,
            tracker);

    request.create(callback);

    Mockito.verify(callback).onFailure(any(Exception.class));
    Mockito.verify(tracker).track(eq(NETWORK_ERROR), eq(ERROR), any(JsonObject.class));
  }
}
