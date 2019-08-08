package com.moez.QKSMS.common.util;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;

import com.moez.QKSMS.R;
import com.moez.QKSMS.common.BaseApplication;
import com.moez.QKSMS.common.view.SelectorDrawable;

/**
 * A Drawable with customizable shape, shapeColor, rippleColor, ripple effect.
 */
public class BackgroundDrawables {

    /**
     * @param shapeColor        Basic color for background drawable.
     * @param radius            Specifies the radius for the corners of the gradient. If this is > 0,
     *                          then the drawable is drawn in a round-rectangle, rather than a rectangle.
     * @param ifUseRippleEffect Used to judge if display ripple effect.
     */
    public static Drawable createBackgroundDrawable(int shapeColor, float radius, boolean ifUseRippleEffect) {
        return createBackgroundDrawable(shapeColor, BaseApplication.getContext().getResources().getColor(R.color.ripples_ripple_color), radius,
                false, ifUseRippleEffect);
    }


    /**
     * @param shapeColor                   Basic color for background drawable.
     * @param rippleColor                  Ripple color for ripple effect.
     * @param radius                       Specifies the radius for the corners of the gradient. If this is > 0,
     *                                     then the drawable is drawn in a round-rectangle, rather than a rectangle.
     * @param ifUseRippleColorOnLowVersion Used to judge if show rippleColor instead of ripple effect
     *                                     when drawable pressed on low Android Version. Mainly used
     *                                     when shapeColor is deep colour like Color.DKGRAY
     * @param ifUseRippleEffect            Used to judge if display ripple effect.
     */
    public static Drawable createBackgroundDrawable(int shapeColor, int rippleColor, float radius
            , boolean ifUseRippleColorOnLowVersion, boolean ifUseRippleEffect) {
        return createBackgroundDrawable(shapeColor, rippleColor, radius, null
                , ifUseRippleColorOnLowVersion, ifUseRippleEffect);
    }


    /**
     * @param shapeColor                   Basic color for background drawable.
     * @param rippleColor                  Ripple color for ripple effect.
     * @param leftTopRadius                Specifies radii for leftTop corner.
     * @param rightTopRadius               Specifies radii for rightTop corner.
     * @param leftBottomRadius             Specifies radii for leftBottom corner.
     * @param rightBottomRadius            Specifies radii for rightBottom corner.
     * @param ifUseRippleColorOnLowVersion Used to judge if show rippleColor instead of ripple effect
     *                                     when drawable pressed on low Android Version. Mainly used
     *                                     when shapeColor is deep colour like Color.DKGRAY
     * @param ifUseRippleEffect            Used to judge if display ripple effect.
     */
    public static Drawable createBackgroundDrawable(int shapeColor, int rippleColor
            , float leftTopRadius, float rightTopRadius, float leftBottomRadius, float rightBottomRadius
            , boolean ifUseRippleColorOnLowVersion, boolean ifUseRippleEffect) {
        return createBackgroundDrawable(shapeColor, rippleColor, 0
                , new float[]{leftTopRadius, leftTopRadius, rightTopRadius, rightTopRadius, leftBottomRadius, leftBottomRadius, rightBottomRadius, rightBottomRadius}
                , ifUseRippleColorOnLowVersion, ifUseRippleEffect);
    }

    /**
     * @param shape                        Background drawable.
     * @param rippleColor                  Ripple color for ripple effect.
     * @param ifUseRippleColorOnLowVersion Used to judge if show rippleColor instead of ripple effect
     *                                     when drawable pressed on low Android Version. Mainly used
     *                                     when shapeColor is deep colour like Color.DKGRAY
     * @param ifUseRippleEffect            Used to judge if display ripple effect.
     */
    public static Drawable createBackgroundDrawable(Drawable shape, int rippleColor
            , boolean ifUseRippleColorOnLowVersion, boolean ifUseRippleEffect) {
        if (!ifUseRippleEffect) {
            return shape;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return new RippleDrawable(ColorStateList.valueOf(rippleColor), shape, null);
        } else if (ifUseRippleColorOnLowVersion) {
            return new SelectorDrawable(shape, rippleColor);
        } else {
            return new SelectorDrawable(shape);
        }
    }

    private static Drawable createBackgroundDrawable(int shapeColor, int rippleColor
            , float radius, float[] radii, boolean ifUseRippleColorOnLowVersion, boolean ifUseRippleEffect) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(shapeColor);
        if (radius != 0) {
            shape.setCornerRadius(radius);
        }
        if (radii != null) {
            shape.setCornerRadii(radii);
        }
        shape.setShape(GradientDrawable.RECTANGLE);

        if (!ifUseRippleEffect) {
            return shape;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return new RippleDrawable(ColorStateList.valueOf(rippleColor), shape, null);
        } else if (ifUseRippleColorOnLowVersion) {
            return new SelectorDrawable(shape, rippleColor);
        } else {
            return new SelectorDrawable(shape);
        }
    }
}