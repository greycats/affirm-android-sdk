package com.affirm.affirmsdk.model;

import com.affirm.affirmsdk.models.NewPromoResponse;
import com.affirm.affirmsdk.models.PromoSet;
import com.affirm.affirmsdk.models.PromoTerm;
import com.affirm.affirmsdk.utils.PromoTermFactory;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NewPromoResponseTest {

  @Test public void toPromoResponse() {
    List<PromoTerm> promoTerms = new ArrayList<>();
    PromoTerm.Builder baseBuilder = PromoTermFactory.create().toBuilder();

    promoTerms.add(baseBuilder.setTermLength(18).setMinimumLoanAmount(10000).build());
    promoTerms.add(baseBuilder.setTermLength(6).setMinimumLoanAmount(5000).build());
    promoTerms.add(baseBuilder.setTermLength(12).setMinimumLoanAmount(1000).build());

    PromoSet promoSet =
        PromoSet.builder().setTermLengthIntervals(promoTerms).setPricingTemplate("sss").build();

    NewPromoResponse newPromoResponse = NewPromoResponse.builder().setAsLowAs(promoSet).build();
    assertEquals(18, newPromoResponse.toPromoResponse(10000).termLength().intValue());
    assertEquals(18, newPromoResponse.toPromoResponse(20000).termLength().intValue());

    assertEquals(6, newPromoResponse.toPromoResponse(5000).termLength().intValue());
    assertEquals(6, newPromoResponse.toPromoResponse(9999).termLength().intValue());

    assertEquals(12, newPromoResponse.toPromoResponse(1000).termLength().intValue());
    assertEquals(12, newPromoResponse.toPromoResponse(4999).termLength().intValue());
    assertEquals(12l, newPromoResponse.toPromoResponse(2).termLength().intValue());
  }
}
