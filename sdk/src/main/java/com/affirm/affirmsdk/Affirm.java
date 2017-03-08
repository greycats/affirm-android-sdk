package com.affirm.affirmsdk;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.TextView;
import com.affirm.affirmsdk.di.AffirmInjector;
import com.affirm.affirmsdk.models.Checkout;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public final class Affirm {
  private static final int CHECKOUT_REQUEST = 8076;
  //private static final String TAG = Affirm.class.getCanonicalName();
  private String merchant;
  private Environment environment;
  private String name;

  private final AffirmInjector component;

  public interface CheckoutCallbacks {
    void onAffirmCheckoutError(String message);

    void onAffirmCheckoutCancelled();

    void onAffirmCheckoutSuccess(String token);
  }

  private Affirm(String merchant, Environment environment, String name) {
    this.merchant = merchant;
    this.environment = environment;
    this.name = name;

    component = new AffirmInjector();
  }

  public static Builder builder() {
    return new Builder();
  }

  public void launchCheckout(@NonNull Activity activity, @NonNull Checkout checkout) {
    CheckoutActivity.launchCheckout(activity, CHECKOUT_REQUEST, merchant, checkout,
        environment.baseUrl1, name);
  }

  public void launchProductModal(@NonNull Activity activity, float amount, String modalId) {
    ModalActivity.launch(activity, merchant, amount, environment.baseUrl1, true, modalId);
  }

  public void launchSiteModal(@NonNull Activity activity, String modalId) {
    ModalActivity.launch(activity, merchant, 0f, environment.baseUrl1, false, modalId);
  }

  public enum Environment {
    SANDBOX("sandbox.affirm.com", "cdn1-sandbox.affirm.com"),
    PRODUCTION("api-cf.affirm.com", "cdn1.affirm.com");

    private final String baseUrl1;
    private final String baseUrl2;

    Environment(String baseUrl1, String baseUrl2) {
      this.baseUrl1 = baseUrl1;
      this.baseUrl2 = baseUrl2;
    }

    @Override public String toString() {
      return "Environment{" + baseUrl1 + ", " + baseUrl2 + '}';
    }
  }

  public boolean onActivityResult(CheckoutCallbacks callbacks, int requestCode, int resultCode,
      Intent data) {
    if (requestCode != CHECKOUT_REQUEST) {
      return false;
    }

    switch (resultCode) {
      case RESULT_OK:
        callbacks.onAffirmCheckoutSuccess(data.getStringExtra(CheckoutActivity.CHECKOUT_TOKEN));
        break;
      case RESULT_CANCELED:
        callbacks.onAffirmCheckoutCancelled();
        break;
      case CheckoutActivity.RESULT_ERROR:
        callbacks.onAffirmCheckoutError(data.getStringExtra(CheckoutActivity.CHECKOUT_ERROR));
        break;
      default:
    }

    return true;
  }

  public CancellableRequest writePromoToTextView(@NonNull TextView textView,
      @NonNull String promoId, float amount, @NonNull AffirmLogoType logoType,
      @NonNull AffirmColor affirmColor, @NonNull PromoCallback promoCallback) {

    final PromoJob promoJob =
        new PromoJob(component, merchant, environment.baseUrl2, textView, promoId, amount, logoType,
            affirmColor, promoCallback);
    return promoJob.getPromo();
  }

  public static final class Builder {
    private String publicKey;
    private Environment environment = Environment.SANDBOX;
    private String name;

    private Builder() {
    }

    public Builder setMerchantPublicKey(@NonNull String publicKey) {
      this.publicKey = publicKey;
      return this;
    }

    public Builder setEnvironment(@NonNull Environment environment) {
      this.environment = environment;
      return this;
    }

    @Deprecated public Builder setFinancialProductKey(@NonNull String financialProductKey) {
      return this;
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Affirm build() {
      if (publicKey == null) {
        throw new IllegalArgumentException("public key cannot be null");
      }

      return new Affirm(publicKey, environment, name);
    }
  }
}
