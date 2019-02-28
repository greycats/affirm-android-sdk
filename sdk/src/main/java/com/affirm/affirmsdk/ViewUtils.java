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

  static void showCloseActionBar(AppCompatActivity activity) {
    activity.getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
    if (activity.getActionBar() != null) {
      activity.getActionBar().show();
      activity.getActionBar().setDisplayShowTitleEnabled(false);
      activity.getActionBar().setDisplayHomeAsUpEnabled(true);
      activity.getActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close);
    } else if (activity.getSupportActionBar() != null) {
      activity.getSupportActionBar().show();
      activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
      activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close);
    }
  }
}
