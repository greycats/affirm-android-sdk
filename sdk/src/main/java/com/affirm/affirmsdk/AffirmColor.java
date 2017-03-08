package com.affirm.affirmsdk;

import android.support.annotation.ColorRes;

public enum AffirmColor {
  AffirmColorTypeBlue, AffirmColorTypeBlack, AffirmColorTypeWhite;

  @ColorRes int getColorRes() {
    switch (this) {
      case AffirmColorTypeBlack:
        return R.color.black100;
      case AffirmColorTypeBlue:
        return R.color.blue;
      default:
        return R.color.white;
    }
  }
}
