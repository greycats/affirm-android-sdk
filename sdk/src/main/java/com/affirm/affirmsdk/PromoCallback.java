package com.affirm.affirmsdk;

import android.widget.TextView;

@Deprecated public interface PromoCallback {
  void onPromoWritten(TextView textView);

  void onFailure(TextView textView, Throwable throwable);
}
