package com.affirm.androidsamplesdk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.affirm.affirmsdk.Affirm;
import com.affirm.affirmsdk.models.Address;
import com.affirm.affirmsdk.models.Checkout;
import com.affirm.affirmsdk.models.Item;
import com.affirm.affirmsdk.models.Name;
import com.affirm.affirmsdk.models.Shipping;
import com.affirm.androidsamplesdk.R;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements Affirm.CheckoutCallbacks {
  private Button checkout;
  private TextView quantity;

  private Affirm affirm;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    checkout = (Button) findViewById(R.id.checkout);
    quantity = (TextView) findViewById(R.id.quantity);

    checkout.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        proceedToCheckout();
      }
    });
    affirm = Affirm.builder()
        .setEnvironment(Affirm.Environment.SANDBOX)
        .setFinancialProductKey("NMDEGYJV2ZT5D95T")
        .setMerchantPublicKey("729JKW3C3DTZDTRY")
        .build();
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
