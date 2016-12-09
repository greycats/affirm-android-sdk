package com.affirm.affirmsdk;

import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class) public class AffirmWebViewClientTest {

  @Mock AffirmWebViewClient.Callbacks callbacks;
  @Mock WebView webview;

  @InjectMocks AffirmWebViewClient affirmWebViewClient;

  @Test public void shouldOverrideUrlLoading_Confirmation() {
    affirmWebViewClient.shouldOverrideUrlLoading(webview,
        "affirm://checkout/confirmed?checkout_token=123");
    Mockito.verify(callbacks).onCheckoutConfirmation("123");
  }

  @Test public void shouldOverrideUrlLoading_Cancellation() {
    affirmWebViewClient.shouldOverrideUrlLoading(webview, "affirm://checkout/cancelled");
    Mockito.verify(callbacks).onCheckoutCancellation();
  }

  @Test public void shouldOverrideUrlLoading_Random() {
    affirmWebViewClient.shouldOverrideUrlLoading(webview, "http://www.affirm.com/api/v1/get");
    Mockito.verify(callbacks, never()).onCheckoutConfirmation(any(String.class));
    Mockito.verify(callbacks, never()).onCheckoutCancellation();
  }

  @Test public void onReceivedError() {
    WebResourceRequest resourceRequest = Mockito.mock(WebResourceRequest.class);
    WebResourceError error = Mockito.mock(WebResourceError.class);
    when(error.toString()).thenReturn("error msg");

    affirmWebViewClient.onReceivedError(webview, resourceRequest, error);
    Mockito.verify(callbacks, never()).onCheckoutError(new Exception("error msg"));
  }
}