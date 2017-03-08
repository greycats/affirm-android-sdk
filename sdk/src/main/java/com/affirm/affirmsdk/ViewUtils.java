package com.affirm.affirmsdk;

import android.support.v7.app.AppCompatActivity;
import android.view.Window;

final class ViewUtils {
  private ViewUtils() {
  }

  static void hideActionBar(AppCompatActivity activity) {
    activity.getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
    if (activity.getActionBar() != null) {
      activity.getActionBar().hide();
    } else if (activity.getSupportActionBar() != null) {
      activity.getSupportActionBar().hide();
    }
  }
}
