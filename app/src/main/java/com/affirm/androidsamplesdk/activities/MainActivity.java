package com.affirm.androidsamplesdk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.affirm.affirmsdk.Affirm;
import com.affirm.affirmsdk.CancellableRequest;
import com.affirm.affirmsdk.PromoCallback;
import com.affirm.affirmsdk.models.Address;
import com.affirm.affirmsdk.models.Checkout;
import com.affirm.affirmsdk.models.Item;
import com.affirm.affirmsdk.models.Name;
import com.affirm.affirmsdk.models.Shipping;
import com.affirm.androidsamplesdk.R;
import java.util.HashMap;
import java.util.Map;

import static com.affirm.affirmsdk.AffirmColor.AffirmColorTypeBlack;
import static com.affirm.affirmsdk.AffirmLogoType.AffirmDisplayTypeSymbol;

public class MainActivity extends AppCompatActivity implements Affirm.CheckoutCallbacks {
  private Button checkout;
  private Button siteModalButton;
  private Button productModalButton;
  private TextView quantity;
  private TextView promo;

  private Affirm affirm;
  private CancellableRequest cancellablePromo;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    checkout = (Button) findViewById(R.id.checkout);
    siteModalButton = (Button) findViewById(R.id.siteModalButton);
    productModalButton = (Button) findViewById(R.id.productModalButton);
    quantity = (TextView) findViewById(R.id.quantity);
    promo = (TextView) findViewById(R.id.promo);

    affirm = Affirm.builder()
        .setEnvironment(Affirm.Environment.SANDBOX)
        .setMerchantPublicKey("Y8CQXFF044903JC0")
        .build();

    checkout.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        proceedToCheckout();
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

    cancellablePromo =
        affirm.writePromoToTextView(promo, "SFCRL4VYS0C78607", 144.5f, AffirmDisplayTypeSymbol,
            AffirmColorTypeBlack, new PromoCallback() {
              @Override public void onPromoWritten(TextView textView) {
                cancellablePromo = null;
              }

              @Override public void onFailure(TextView textView, Throwable throwable) {
                Toast.makeText(MainActivity.this, "Checkout token: " + throwable.getMessage(),
                    Toast.LENGTH_LONG).show();
                cancellablePromo = null;
              }
            });
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (!affirm.onActivityResult(this, requestCode, resultCode, data)) {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  private void proceedToCheckout() {
    final int qty = Integer.valueOf(quantity.getText().toString());

    final Item item = Item.builder()
        .setDisplayName("Great Deal Wheel")
        .setImageUrl(
            "http://www.m2motorsportinc.com/media/catalog/product/cache/1/thumbnail/9df78eab33525d08d6e5fb8d27136e95/v/e/velocity-vw125-wheels-rims.jpg")
        .setQty(qty)
        .setSku("wheel")
        .setUnitPrice(100000)
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
        .setZipcode("94103")
        .build();

    final Shipping shipping = Shipping.builder().setAddress(address).setName(name).build();

    final Checkout checkout = Checkout.builder()
        .setItems(items)
        .setBilling(shipping)
        .setShipping(shipping)
        .setShippingAmount(100000)
        .setTaxAmount(10000)
        .setTotal(110000)
        .build();

    affirm.launchCheckout(this, checkout);
  }

  @Override protected void onPause() {
    super.onPause();
    if (cancellablePromo != null) {
      cancellablePromo.cancelRequest();
    }
  }

  @Override public void onAffirmCheckoutSuccess(String token) {
    Toast.makeText(this, "Checkout token: " + token, Toast.LENGTH_LONG).show();
  }

  @Override public void onAffirmCheckoutCancelled() {
    Toast.makeText(this, "Checkout Cancelled", Toast.LENGTH_LONG).show();
  }

  @Override public void onAffirmCheckoutError(String message) {
    Toast.makeText(this, "Checkout Error: " + message, Toast.LENGTH_LONG).show();
  }
}
