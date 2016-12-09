package com.affirm.affirmsdk.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import com.affirm.affirmsdk.R;

public class ProgressIndicator extends View {

  private int duration = 8000;
  private int hiddingDuration = 500;
  private Paint paint;
  private float density;
  private float start = .0f;
  private float completed = .0f;
  private int to = 0;
  private boolean isWaitingToAnimation = false;
  private final Handler handler = new Handler();
  private final Runnable tick = new Runnable() {
    public void run() {
      invalidate();
      calculateProgress();
      if (!finished()) {
        handler.postDelayed(this, 20);
      } else if (isWaitingToAnimation) {
        setVisibility(GONE);
      }
    }
  };

  private float animStartTime;

  public ProgressIndicator(Context context) {
    super(context);
    init();
  }

  public ProgressIndicator(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public ProgressIndicator(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  private void init() {
    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    density = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1,
        getResources().getDisplayMetrics());
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    int paddingLeft = getPaddingLeft();
    int paddingTop = getPaddingTop();
    int paddingRight = getPaddingRight();
    int paddingBottom = getPaddingBottom();

    int contentHeight = getHeight() - paddingTop - paddingBottom;
    float contentWidth = getWidth() - paddingLeft - paddingRight;
    int centerLineH = (int) (4 * density);
    to = (int) (paddingLeft + start + contentWidth * completed);
    int center = paddingTop + contentHeight / 2;

    // Draw Bg
    paint.setColor(getResources().getColor(R.color.white));
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(centerLineH * 2);
    canvas.drawLine(0, center, paddingLeft + contentWidth, center, paint);

    // Draw Completed
    if (completed > .0f) {
      paint.setColor(getResources().getColor(R.color.indigo));
      paint.setStrokeWidth(centerLineH);
      canvas.drawLine(0, center, to, center, paint);
    }
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    startAnimation();
  }

  @Override protected void onDetachedFromWindow() {
    stopAnimation();
    super.onDetachedFromWindow();
  }

  private void calculateProgress() {
    completed = (float) (SystemClock.uptimeMillis() - animStartTime) / duration;
  }

  private void startAnimation() {
    animStartTime = SystemClock.uptimeMillis();
    stopAnimation();
    handler.post(tick);
  }

  private void stopAnimation() {
    handler.removeCallbacks(tick);
  }

  private boolean finished() {
    return completed >= 1f;
  }

  public void hideAnimated() {
    if (finished()) {
      setVisibility(GONE);
    } else {
      isWaitingToAnimation = true;
      final float timeLeft = duration - (SystemClock.uptimeMillis() - animStartTime);
      if (timeLeft > hiddingDuration) {
        start = to;
        animStartTime = SystemClock.uptimeMillis();
        duration = hiddingDuration;
      }
    }
  }
}