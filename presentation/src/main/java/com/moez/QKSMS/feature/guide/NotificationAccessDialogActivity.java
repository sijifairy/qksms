package com.moez.QKSMS.feature.guide;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;

import com.moez.QKSMS.R;


public class NotificationAccessDialogActivity extends Activity {

    private NotificationAccessGuideAnimationDialog mPermissionGuide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_access_dialog);
        mPermissionGuide = findViewById(R.id.notification_access_guide_animation_dialog);
        mPermissionGuide.setShowContentImmediately(false);
        mPermissionGuide.onAddedToWindow();
        mPermissionGuide.setHost(this);
    }


    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mPermissionGuide != null) {
            mPermissionGuide.onRemovedFromWindow();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, R.anim.app_lock_fade_out_long);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return false;
    }
}
