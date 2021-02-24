package com.moez.QKSMS.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.moez.QKSMS.R;

/** ratio = width / height */
public class RatioRoundImageView extends RoundImageView {
  private static final int BASE_WIDTH = 0;
  private static final int BASE_HEIGHT = 1;
  private static final int DEFAULT_BASE = BASE_WIDTH;

  private int mBase;
  private float mRatio;

  public RatioRoundImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RatioRoundAttr);
    mBase = a.getInt(R.styleable.RatioRoundAttr_super_apps_base, DEFAULT_BASE);
    mRatio = a.getFloat(R.styleable.RatioRoundAttr_super_apps_ratio, 0f);
    a.recycle();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = MeasureSpec.getSize(heightMeasureSpec);
    boolean isRatio = ratio(mBase, mRatio, width, height);
    if (!isRatio) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
  }

  private boolean ratio(int base, float ratio, int width, int height) {
    if (ratio > 0f) {
      if (base == BASE_WIDTH) {
        height = (int) Math.ceil(width / ratio);
        setMeasuredDimension(getMeasureSpec(width), getMeasureSpec(height));
      } else {
        width = (int) Math.ceil(height * ratio);
        setMeasuredDimension(getMeasureSpec(width), getMeasureSpec(height));
      }
      return true;
    } else {
      return false;
    }
  }

  private int getMeasureSpec(int size) {
    return MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
  }
}
