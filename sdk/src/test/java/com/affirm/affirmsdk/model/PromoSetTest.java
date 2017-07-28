package com.affirm.affirmsdk.model;

import com.affirm.affirmsdk.models.PromoSet;
import com.affirm.affirmsdk.models.PromoTerm;
import com.affirm.affirmsdk.utils.PromoTermFactory;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PromoSetTest {
  @Test public void termForAmount() {
    List<PromoTerm> promoTerms = new ArrayList<>();
    PromoTerm.Builder baseBuilder = PromoTermFactory.create().toBuilder();

    promoTerms.add(baseBuilder.setTermLength(18).setMinimumLoanAmount(10000).build());
    promoTerms.add(baseBuilder.setTermLength(6).setMinimumLoanAmount(5000).build());
    promoTerms.add(baseBuilder.setTermLength(12).setMinimumLoanAmount(1000).build());

    PromoSet promoSet =
        PromoSet.builder().setTermLengthIntervals(promoTerms).setPricingTemplate("sss").build();

    assertEquals(promoTerms.get(0), promoSet.termForAmount(10000));
    assertEquals(promoTerms.get(0), promoSet.termForAmount(20000));

    assertEquals(promoTerms.get(1), promoSet.termForAmount(5000));
    assertEquals(promoTerms.get(1), promoSet.termForAmount(9999));

    assertEquals(promoTerms.get(2), promoSet.termForAmount(1000));
    assertEquals(promoTerms.get(2), promoSet.termForAmount(4999));
    assertEquals(promoTerms.get(2), promoSet.termForAmount(2));
  }
}
