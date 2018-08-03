Affirm Android SDK
==================

Easily integrate Affirm checkouts on merchant's native apps

## Download

Download via Gradle:
```groovy
compile "com.affirm:affirm-android-sdk:1.0.10"
```
or Maven:
```xml
<dependency>
  <groupId>com.affirm</groupId>
  <artifactId>affirm-android-sdk</artifactId>
  <version>1.0.10</version>
</dependency>
```
Snapshots of the development version are available in [Sonatype's `snapshots` repository](https://oss.sonatype.org/content/repositories/snapshots/).

## Min SDK

The minimum sdk is 19. If your app supports lower versions you will have to add this to your manifest

```xml
<uses-sdk android:targetSdkVersion="your-target-version" android:minSdkVersion="your-min-sdk-version"
      tools:overrideLibrary="com.affirm.affirmsdk"/>
```

## Basic Usage
Start by creating an affirm instance.

```java
affirm = Affirm.builder()
        .setEnvironment(Affirm.Environment.SANDBOX)
        .setMerchantPublicKey("729JKW3C3DTZDTRY")
        .build();
```

When you are ready to checkout with affirm create a checkout object
and launch the affirm checkout.


```java
...
final Checkout checkout = Checkout.builder()
        .setItems(items)
        .setBilling(shipping)
        .setShipping(shipping)
        .setShippingAmount(0f)
        .setTaxAmount(100f)
        .setTotal(1100f)
        .build();

affirm.launchCheckout(this, checkout);
```

Override onActivityResult so that affirm can handle the result.

```java
@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  if (!affirm.onActivityResult(this, requestCode, resultCode, data)) {
    super.onActivityResult(requestCode, resultCode, data);
  }
}
```

Implement Checkout callbacks.

```java
@Override public void onAffirmCheckoutSuccess(String token) {
    Toast.makeText(this, "Checkout token: " + token, Toast.LENGTH_LONG).show();
  }

@Override public void onAffirmCheckoutCancelled() {
  Toast.makeText(this, "Checkout Cancelled", Toast.LENGTH_LONG).show();
}

@Override public void onAffirmCheckoutError(String message) {
  Toast.makeText(this, "Checkout Error: " + message, Toast.LENGTH_LONG).show();
}
```
