package com.affirm.affirmsdk;

import android.support.annotation.DrawableRes;

public enum AffirmLogoType {
  AffirmDisplayTypeLogo, AffirmDisplayTypeText, AffirmDisplayTypeSymbol,
  AffirmDisplayTypeSymbolHollow;

  @DrawableRes int getDrawableRes() {
    switch (this) {
      case AffirmDisplayTypeLogo:
        return R.drawable.black_logo_transparent_bg;
      case AffirmDisplayTypeSymbol:
        return R.drawable.black_solid_circle_transparent_bg;
      case AffirmDisplayTypeSymbolHollow:
        return R.drawable.black_hollow_circle_transparent_bg;
      default:
        return R.drawable.black_logo_transparent_bg;
    }
  }
}
