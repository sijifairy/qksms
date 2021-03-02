package com.moez.QKSMS.common.util;

import android.content.Context;
import android.os.Bundle;

import com.moez.QKSMS.R;

import androidx.core.app.ActivityOptionsCompat;

/**
 * Created by lizhe on 2019/6/10.
 */

public class TransitionUtils {

    public static Bundle getTransitionInBundle(Context context) {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
        return options.toBundle();
    }

    public static Bundle getNoAnimationBundle(Context context) {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeCustomAnimation(context, R.anim.anim_null, R.anim.anim_null);
        return options.toBundle();
    }
}
