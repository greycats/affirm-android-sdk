package com.affirm.affirmsdk;

import com.google.common.truth.Truth;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.Date;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okio.Buffer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static com.affirm.affirmsdk.Tracker.TrackingEvent.NETWORK_ERROR;
import static com.affirm.affirmsdk.Tracker.TrackingLevel.ERROR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class) public class TrackerTest {

  @Mock OkHttpClient client;
  @Mock Call call;
  @Mock Clock clock;
  Tracker tracker;
  @Captor ArgumentCaptor<Request> requestCaptor;

  @Before public void setUp() throws Exception {
    tracker = new Tracker(client, Affirm.Environment.SANDBOX, "111", clock);

    when(clock.now()).thenReturn(new Date(1498081546783L));
  }

  @Test public void track() throws Exception {
    Mockito.when(client.newCall(any(Request.class))).thenReturn(call);

    final JsonObject data = new JsonObject();
    data.addProperty("a", 1);
    data.addProperty("b", "b");
    tracker.track(NETWORK_ERROR, ERROR, data);

    Mockito.verify(client, times(1)).newCall(requestCaptor.capture());
    final Request request = requestCaptor.getValue();
    final String body = bodyToString(request);
    Truth.assertThat(body)
        .isEqualTo("{\"a\":1,\"b\":\"b\",\"local_log_counter\":0,\"ts\":1498081546783,"
            + "\"event_name\":\"network error\",\"app_id\":\"Android SDK\",\"release\":\""
            + BuildConfig.VERSION_NAME
            + "\",\"android_sdk\":0,\"device_name\":null,"
            + "\"merchant_key\":\"111\",\"level\":\"error\","
            + "\"environment\":\"sandbox\"}");

    Mockito.verify(call).enqueue(any(Callback.class));
  }

  private static String bodyToString(final Request request) throws IOException {
    final Request copy = request.newBuilder().build();
    final Buffer buffer = new Buffer();
    copy.body().writeTo(buffer);
    return buffer.readUtf8();
  }
}