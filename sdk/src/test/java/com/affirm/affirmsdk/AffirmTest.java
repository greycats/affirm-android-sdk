package com.affirm.affirmsdk;

import android.app.Activity;
import android.content.Intent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

public class AffirmTest {

  Affirm affirm;

  @Before public void setup() {
    affirm = Affirm.builder().setFinancialProductKey("sdf").setMerchantPublicKey("sdf").build();
  }

  @Test public void builder() throws Exception {
    Affirm.Builder builder =
        Affirm.builder().setFinancialProductKey("sdf").setMerchantPublicKey("sdf");
    builder.build();
  }

  @Test public void builder_FinancialProductKey_NotPassed() throws Exception {
    Affirm.Builder builder = Affirm.builder().setMerchantPublicKey("sdf");

    try {
      builder.build();
      Assert.fail("Should have thrown exception");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test public void builder_PublicKey_NotPassed() throws Exception {
    Affirm.Builder builder = Affirm.builder().setFinancialProductKey("sdf");

    try {
      builder.build();
      Assert.fail("Should have thrown exception");
    } catch (IllegalArgumentException e) {
    }
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