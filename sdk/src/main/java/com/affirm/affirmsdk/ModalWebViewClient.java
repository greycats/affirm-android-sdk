package com.affirm.affirmsdk;

import android.support.annotation.NonNull;
import android.webkit.WebView;

public final class ModalWebViewClient extends AffirmWebViewClient {

  public ModalWebViewClient(@NonNull Callbacks callbacks) {
    super(callbacks);
  }

  @Override boolean hasCallbackUrl(WebView view, String url) {
    return false;
  }
}
