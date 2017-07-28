package com.affirm.affirmsdk.models;

import android.support.annotation.NonNull;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@AutoValue public abstract class PromoSet {
  public abstract List<PromoTerm> termLengthIntervals();

  public abstract String pricingTemplate();

  public static TypeAdapter<PromoSet> typeAdapter(Gson gson) {
    return new AutoValue_PromoSet.GsonTypeAdapter(gson);
  }

  public static Builder builder() {
    return new AutoValue_PromoSet.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder public abstract static class Builder {
    public abstract Builder setTermLengthIntervals(List<PromoTerm> value);

    public abstract Builder setPricingTemplate(String value);

    public abstract PromoSet build();
  }

  @NonNull public PromoTerm termForAmount(int targetAmount) {
    List<PromoTerm> terms = new ArrayList<>();
    terms.addAll(termLengthIntervals());
    Collections.sort(terms, new Comparator<PromoTerm>() {
      @Override public int compare(PromoTerm lhs, PromoTerm rhs) {
        return lhs.minimumLoanAmount() > rhs.minimumLoanAmount() ? -1
            : (lhs.minimumLoanAmount() < rhs.minimumLoanAmount()) ? 1 : 0;
      }
    });

    for (PromoTerm term : terms) {
      if (targetAmount >= term.minimumLoanAmount()) {
        return term;
      }
    }

    return terms.get(terms.size() - 1);
  }
}
