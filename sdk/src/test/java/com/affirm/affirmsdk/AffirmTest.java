package com.affirm.affirmsdk;

import android.app.Activity;
import android.content.Intent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class AffirmTest {

  Affirm affirm;

  @Before public void setup() {
    if (Affirm.getInstance() == null) {
      Affirm.builder().setMerchantPublicKey("sdf").build();
    }

    affirm = Affirm.getInstance();
  }

  @Test(expected = IllegalStateException.class) public void builder_FailsWhenInstanceExists()
      throws Exception {
    Affirm.builder().setMerchantPublicKey("sdf").build();
  }

  @Test(expected = IllegalArgumentException.class) public void builder_PublicKey_NotPassed()
      throws Exception {
    Affirm.builder().build();
  }

  @Test public void onActivityResult_Success() {
    Affirm.CheckoutCallbacks callbacks = Mockito.mock(Affirm.CheckoutCallbacks.class);

    Intent intent = Mockito.mock(Intent.class);

    Mockito.when(intent.getStringExtra(Mockito.any(String.class))).thenReturn("1234");

    affirm.onActivityResult(callbacks, 8076, Activity.RESULT_OK, intent);

    Mockito.verify(callbacks).onAffirmCheckoutSuccess("1234");
  }

  @Test public void onActivityResult_Cancelled() {
    Affirm.CheckoutCallbacks callbacks = Mockito.mock(Affirm.CheckoutCallbacks.class);

    affirm.onActivityResult(callbacks, 8076, Activity.RESULT_CANCELED, Mockito.mock(Intent.class));

    Mockito.verify(callbacks).onAffirmCheckoutCancelled();
  }

  @Test public void onActivityResult_Error() {
    Affirm.CheckoutCallbacks callbacks = Mockito.mock(Affirm.CheckoutCallbacks.class);

    Intent intent = Mockito.mock(Intent.class);

    Mockito.when(intent.getStringExtra(Mockito.any(String.class))).thenReturn("error");

    affirm.onActivityResult(callbacks, 8076, CheckoutActivity.RESULT_ERROR, intent);

    Mockito.verify(callbacks).onAffirmCheckoutError("error");
  }
}