package com.affirm.affirmsdk.utils;

import com.affirm.affirmsdk.AffirmWebViewClient;
import com.affirm.affirmsdk.models.CardDetails;
import com.affirm.affirmsdk.models.Merchant;

public class CardDetailsFactory {
  public static CardDetails create() {
    return CardDetails.builder()
        .setCardholderName("John Smith")
        .setCheckoutToken("1234-1234")
        .setCvv("333")
        .setExpiration("1022")
        .setNumber("4444444444444444")
        .build();
  }
}
