package com.affirm.affirmsdk;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

class PopUpWebChromeClient extends WebChromeClient {
  @Override public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture,
      Message resultMsg) {
    final WebView.HitTestResult result = view.getHitTestResult();
    final String data = result.getExtra();
    final Context context = view.getContext();
    final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
    context.startActivity(browserIntent);
    return false;
  }

  @Override public boolean onConsoleMessage(ConsoleMessage cm) {
    if (BuildConfig.DEBUG && cm.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
      Log.e("Affirm", cm.message() + " -- From line " + cm.lineNumber() + " of " + cm.sourceId());
      return true;
    }
    return false;
  }
}
