package com.affirm.affirmsdk;

import android.text.SpannableString;

public interface PromoCallback {
  void onPromoWritten(SpannableString editable);
  void onFailure(Throwable throwable);
}
