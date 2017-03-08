package com.affirm.affirmsdk;

import com.affirm.affirmsdk.di.AffirmInjector;
import com.affirm.affirmsdk.models.CheckoutResponse;
import com.affirm.affirmsdk.utils.ResponseFactory;
import com.google.gson.Gson;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;

@RunWith(MockitoJUnitRunner.class) public class AffirmRequestTest {

  @Mock OkHttpClient client;
  @Mock AffirmRequest.Callback<CheckoutResponse> callback;
  @Mock Call call;

  Gson gson;
  AffirmRequest.Endpoint endpoint;

  @Before public void setUp() {
    AffirmInjector component = new AffirmInjector();
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
        new AffirmRequest<>(CheckoutResponse.class, "api.affirm.com", client, gson, endpoint);

    request.create(callback);

    Mockito.verify(callback).onSuccess(any(CheckoutResponse.class));
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
        new AffirmRequest<>(CheckoutResponse.class, "api.affirm.com", client, gson, endpoint);

    request.create(callback);

    Mockito.verify(callback).onFailure(any(ServerError.class));
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
        new AffirmRequest<>(CheckoutResponse.class, "api.affirm.com", client, gson, endpoint);

    request.create(callback);

    Mockito.verify(callback).onFailure(any(Exception.class));
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
        new AffirmRequest<>(CheckoutResponse.class, "api.affirm.com", client, gson, endpoint);

    request.create(callback);

    Mockito.verify(callback).onFailure(any(Exception.class));
  }
}
