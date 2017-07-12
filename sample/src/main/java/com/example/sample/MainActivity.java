package com.example.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.affirm.affirmsdk.Affirm;
import com.affirm.affirmsdk.CancellableRequest;
import com.affirm.affirmsdk.PromoCallback;
import com.affirm.affirmsdk.models.Address;
import com.affirm.affirmsdk.models.CardDetails;
import com.affirm.affirmsdk.models.Checkout;
import com.affirm.affirmsdk.models.Item;
import com.affirm.affirmsdk.models.Name;
import com.affirm.affirmsdk.models.Shipping;
import java.util.HashMap;
import java.util.Map;

import static com.affirm.affirmsdk.AffirmColor.AffirmColorTypeBlue;
import static com.affirm.affirmsdk.AffirmLogoType.AffirmDisplayTypeLogo;

public class MainActivity extends AppCompatActivity
    implements Affirm.CheckoutCallbacks, Affirm.VcnCheckoutCallbacks {
  private Button checkout;
  private Button vcnCheckout;
  private Button siteModalButton;
  private Button productModalButton;
  private TextView promo;

  private Affirm affirm;
  private CancellableRequest aslowasPromo;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    checkout = (Button) findViewById(R.id.checkout);
    vcnCheckout = (Button) findViewById(R.id.vcnCheckout);
    siteModalButton = (Button) findViewById(R.id.siteModalButton);
    productModalButton = (Button) findViewById(R.id.productModalButton);
    promo = (TextView) findViewById(R.id.promo);

    affirm = Affirm.getInstance();

    checkout.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        proceedToCheckout();
      }
    });

    vcnCheckout.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        proceedToVcnCheckout();
      }
    });

    siteModalButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        affirm.launchSiteModal(MainActivity.this, "5LNMQ33SEUYHLNUC");
      }
    });

    productModalButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        affirm.launchProductModal(MainActivity.this, 1100, "0Q97G0Z4Y4TLGHGB");
      }
    });

    aslowasPromo =
        affirm.writePromoToTextView(promo, "SFCRL4VYS0C78607", 144.5f, AffirmDisplayTypeLogo,
            AffirmColorTypeBlue, new PromoCallback() {
              @Override public void onPromoWritten(TextView textView) {
                aslowasPromo = null;
              }

              @Override public void onFailure(TextView textView, Throwable throwable) {
                Toast.makeText(MainActivity.this, "As low as label : " + throwable.getMessage(),
                    Toast.LENGTH_LONG).show();
                aslowasPromo = null;
              }
            });
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (affirm.onCheckoutActivityResult(this, requestCode, resultCode, data)) {
      return;
    }

    if (affirm.onVcnCheckoutActivityResult(this, requestCode, resultCode, data)) {
      return;
    }

    super.onActivityResult(requestCode, resultCode, data);
  }

  private Checkout checkoutModel() {
    final Item item = Item.builder()
        .setDisplayName("Great Deal Wheel")
        .setImageUrl(
            "http://www.m2motorsportinc.com/media/catalog/product/cache/1/thumbnail/9df78eab33525d08d6e5fb8d27136e95/v/e/velocity-vw125-wheels-rims.jpg")
        .setQty(1)
        .setSku("wheel")
        .setUnitPrice(1000f)
        .setUrl("http://merchant.com/great_deal_wheel")
        .build();

    final Map<String, Item> items = new HashMap<>();
    items.put("wheel", item);

    final Name name = Name.builder().setFull("John Smith").build();
    final Address address = Address.builder()
        .setCity("San Francisco")
        .setCountry("USA")
        .setLine1("333 Kansas st")
        .setState("CA")
        .setZipcode("94107")
        .build();

    final Shipping shipping = Shipping.builder().setAddress(address).setName(name).build();

    return Checkout.builder()
        .setItems(items)
        .setBilling(shipping)
        .setShipping(shipping)
        .setShippingAmount(0f)
        .setTaxAmount(100f)
        .setTotal(1100f)
        .build();
  }

  private void proceedToCheckout() {
    affirm.launchCheckout(this, checkoutModel());
  }

  private void proceedToVcnCheckout() {
    affirm.launchVcnCheckout(this, checkoutModel());
  }

  @Override protected void onPause() {
    super.onPause();
    if (aslowasPromo != null) {
      aslowasPromo.cancelRequest();
    }
  }

  // - Affirm.CheckoutCallbacks
  @Override public void onAffirmCheckoutSuccess(String token) {
    Toast.makeText(this, "Checkout token: " + token, Toast.LENGTH_LONG).show();
  }

  @Override public void onAffirmCheckoutCancelled() {
    Toast.makeText(this, "Checkout Cancelled", Toast.LENGTH_LONG).show();
  }

  @Override public void onAffirmCheckoutError(String message) {
    Toast.makeText(this, "Checkout Error: " + message, Toast.LENGTH_LONG).show();
  }

  // - Affirm.VcnCheckoutCallbacks
  @Override public void onAffirmVcnCheckoutCancelled() {
    Toast.makeText(this, "Vcn Checkout Cancelled", Toast.LENGTH_LONG).show();
  }

  @Override public void onAffirmVcnCheckoutError(@Nullable String message) {
    Toast.makeText(this, "Vcn Checkout Error: " + message, Toast.LENGTH_LONG).show();
  }

  @Override public void onAffirmVcnCheckoutSuccess(@NonNull CardDetails cardDetails) {
    Toast.makeText(this, "Vcn Checkout Card: " + cardDetails.toString(), Toast.LENGTH_LONG).show();
  }
}
