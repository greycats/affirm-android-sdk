package com.affirm.affirmsdk;

import com.affirm.affirmsdk.models.CheckoutResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static com.affirm.affirmsdk.Tracker.TrackingEvent.CHECKOUT_CREATION_FAIL;
import static com.affirm.affirmsdk.Tracker.TrackingEvent.CHECKOUT_CREATION_SUCCESS;
import static com.affirm.affirmsdk.Tracker.TrackingEvent.CHECKOUT_WEBVIEW_FAIL;
import static com.affirm.affirmsdk.Tracker.TrackingEvent.CHECKOUT_WEBVIEW_SUCCESS;
import static com.affirm.affirmsdk.Tracker.TrackingLevel.ERROR;
import static com.affirm.affirmsdk.Tracker.TrackingLevel.INFO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class) public class CheckoutPresenterTest {

  @Mock Tracker tracker;
  @Mock AffirmRequest<CheckoutResponse> checkoutRequest;
  @Mock CheckoutPresenter.Interface page;

  CheckoutPresenter presenter;

  @Before public void setUp() {
    presenter = new CheckoutPresenter(tracker, checkoutRequest);
  }

  @Test public void onWebViewError() throws Exception {
    Exception error = new Exception("error");
    presenter.onAttach(page);
    presenter.onWebViewError(error);

    verify(tracker).track(CHECKOUT_WEBVIEW_FAIL, ERROR, null);
  }

  @Test public void onWebViewConfirmation() throws Exception {
    presenter.onAttach(page);
    presenter.onWebViewConfirmation("1234");

    verify(tracker).track(CHECKOUT_WEBVIEW_SUCCESS, INFO, null);
  }

  @Test public void onStartCheckout_Success() throws Exception {
    final CheckoutResponse response = CheckoutResponse.builder().setRedirectUrl("1234").build();

    doAnswer(new Answer() {
      @Override public Object answer(InvocationOnMock invocation) throws Throwable {
        ((AffirmRequest.Callback<CheckoutResponse>) invocation.getArgument(0)).onSuccess(response);
        return null;
      }
    }).when(checkoutRequest).create(any(AffirmRequest.Callback.class));

    presenter.onAttach(page);

    verify(tracker).track(CHECKOUT_CREATION_SUCCESS, INFO, null);
    verify(page).loadWebView("1234");
  }

  @Test public void onStartCheckout_Fail() throws Exception {
    final Exception exception = new Exception("abc");

    doAnswer(new Answer() {
      @Override public Object answer(InvocationOnMock invocation) throws Throwable {
        ((AffirmRequest.Callback<CheckoutResponse>) invocation.getArgument(0)).onFailure(exception);
        return null;
      }
    }).when(checkoutRequest).create(any(AffirmRequest.Callback.class));

    presenter.onAttach(page);

    verify(tracker).track(CHECKOUT_CREATION_FAIL, ERROR, null);
    verify(page).finishWithError("abc");
  }
}