package com.affirm.affirmsdk;

import com.affirm.affirmsdk.models.Checkout;
import com.affirm.affirmsdk.models.MyAdapterFactory;
import com.affirm.affirmsdk.utils.CheckoutFactory;
import com.affirm.affirmsdk.utils.MerchantFactory;
import com.google.common.truth.Truth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
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
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;

@RunWith(MockitoJUnitRunner.class) public class CheckoutRequestTest {

  final static String expectedBody =
      "{\"checkout\":{\"items\":{\"wheel\":{\"display_name\":\"Great Deal Wheel\",\"sku\":\"wheel\","
          + "\"unit_price\":100000,\"qty\":1,\"item_url\":\"http://merchant.com/great_deal_wheel\",\"item_image_url\""
          + ":\"http://www.image.com/111\"}},\"shipping\":{\"address\":{\"line1\":\"333 Kansas st\",\"city\""
          + ":\"San Francisco\",\"state\":\"CA\",\"zipcode\":\"94103\",\"country\":\"USA\"},\"name\":"
          + "{\"full\":\"John Smith\"}},\"billing\":{\"address\":{\"line1\":\"333 Kansas st\",\"city\":"
          + "\"San Francisco\",\"state\":\"CA\",\"zipcode\":\"94103\",\"country\":\"USA\"},\"name\":{\"full\""
          + ":\"John Smith\"}},\"shipping_amount\":100000,\"tax_amount\":10000,\"total\":110000,\"merchant\""
          + ":{\"public_api_key\":\"sdf\",\"user_confirmation_url\":\"affirm://checkout/confirmed\","
          + "\"user_cancel_url\":\"affirm://checkout/cancelled\"},\"config\":{\"user_confirmation_url_action"
          + "\":\"GET\",\"financial_product_key\":\"111\"},\"api_version\":\"v2\"}}";

  @Mock Callback callback;
  @Mock OkHttpClient okHttpClient;
  @Mock Call call;

  @Captor ArgumentCaptor<Request> requestArgumentCaptor;

  CheckoutRequest checkoutRequest;

  @Before public void setup() {
    final Gson gson =
        new GsonBuilder().registerTypeAdapterFactory(MyAdapterFactory.create()).create();

    checkoutRequest =
        new CheckoutRequest(MerchantFactory.create(), "111", "api.affirm.com", okHttpClient, gson);
  }

  @Test public void call() throws Exception {
    Mockito.when(okHttpClient.newCall(any(Request.class))).thenReturn(call);

    Checkout checkout = CheckoutFactory.create();

    checkoutRequest.create(checkout, callback);

    Mockito.verify(okHttpClient).newCall(requestArgumentCaptor.capture());

    Request request = requestArgumentCaptor.getValue();
    Truth.assertThat(request.header("Accept")).contains("json");
    Truth.assertThat(request.header("Affirm-User-Agent")).contains("Affirm-Android-SDK");
    Truth.assertThat(request.url().toString()).contains("api.affirm.com");
    Truth.assertThat(bodyToString(request)).contains(expectedBody);
  }

  private static String bodyToString(final Request request) throws Exception {
    final Request copy = request.newBuilder().build();
    final Buffer buffer = new Buffer();
    copy.body().writeTo(buffer);
    return buffer.readUtf8();
  }
}