package com.affirm.affirmsdk;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import com.affirm.affirmsdk.models.Checkout;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public final class Affirm {
  private static final int CHECKOUT_REQUEST = 8076;
  private static final String TAG = Affirm.class.getCanonicalName();
  private String merchant;
  private Environment environment;
  private String financialProductKey;
  private String name;

  public interface CheckoutCallbacks {
    void onAffirmCheckoutError(String message);

    void onAffirmCheckoutCancelled();

    void onAffirmCheckoutSuccess(String token);
  }

  private Affirm(String merchant, Environment environment, String financialProductKey,
      String name) {
    this.merchant = merchant;
    this.environment = environment;
    this.financialProductKey = financialProductKey;
    this.name = name;
  }

  public static Builder builder() {
    return new Builder();
  }

  public void launchCheckout(@NonNull Activity activity, @NonNull Checkout checkout) {
    Log.d(TAG, "affirm launchCheckout");
    CheckoutActivity.launchCheckout(activity, CHECKOUT_REQUEST, merchant, checkout,
        financialProductKey, environment.name, name);
    Log.d(TAG, "end affirm launchCheckout");
  }

  public enum Environment {
    SANDBOX("sandbox.affirm-stage.com"),
    STAGE("www.affirm-stage.com"),
    PRODUCTION("api-cf.affirm.com");

    private final String name;

    Environment(String s) {
      name = s;
    }

    @Override public String toString() {
      return "Environment{name=" + name + '}';
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

  public static final class Builder {
    private String publicKey;
    private Environment environment = Environment.SANDBOX;
    private String financialProductKey;
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

    public Builder setFinancialProductKey(@NonNull String financialProductKey) {
      this.financialProductKey = financialProductKey;
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

      if (financialProductKey == null) {
        throw new IllegalArgumentException("financial product key cannot be null");
      }
      return new Affirm(publicKey, environment, financialProductKey, name);
    }
  }
}
