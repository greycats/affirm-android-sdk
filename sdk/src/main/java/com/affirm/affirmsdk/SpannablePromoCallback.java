package com.affirm.affirmsdk;

import android.text.SpannableString;

public interface SpannablePromoCallback {
  void onPromoWritten(final SpannableString editable);

  void onFailure(Throwable throwable);
}
