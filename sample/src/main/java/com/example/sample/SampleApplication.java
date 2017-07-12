package com.example.sample;

import android.app.Application;
import com.affirm.affirmsdk.Affirm;

public class SampleApplication extends Application {

  @Override public void onCreate() {
    super.onCreate();

    Affirm.builder()
        .setEnvironment(Affirm.Environment.SANDBOX)
        .setMerchantPublicKey("Y8CQXFF044903JC0")
        .build();
  }
}
