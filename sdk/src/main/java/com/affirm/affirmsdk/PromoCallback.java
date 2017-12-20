package com.affirm.affirmsdk;

import android.text.SpannableString;
import android.widget.TextView;

public interface PromoCallback {
  void onPromoWritten(SpannableString editable);
  void onPromoWritten(TextView textView);
  void onFailure(Throwable throwable);
  void onFailure(TextView textView, Throwable throwable);
}
