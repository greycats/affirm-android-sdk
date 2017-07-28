package com.affirm.affirmsdk.utils;

import com.affirm.affirmsdk.models.PromoTerm;

public class PromoTermFactory {
  public static PromoTerm create() {
    return PromoTerm.builder().setTermLength(12).setMinimumLoanAmount(10000).setApr(0.1f).build();
  }
}
