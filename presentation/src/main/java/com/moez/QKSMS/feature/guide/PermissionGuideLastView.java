package com.moez.QKSMS.feature.guide;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.moez.QKSMS.R;

public class PermissionGuideLastView extends FrameLayout {

    private ToggleView mToggleView;
    private TextView mLastTitleTv;

    public PermissionGuideLastView(Context context) {
        this(context, null);
    }

    public PermissionGuideLastView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public PermissionGuideLastView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.permission_guide_last_view, this, true);
        this.mToggleView = (ToggleView) findViewById(R.id.last_view_toggle_view);
        mLastTitleTv = findViewById(R.id.permission_guide_last_title_tv);
    }

    public void initToggle() {
        setTranslationY(0.0f);
        this.mToggleView.init();
    }

    public void setLastTitleText(String lastTitle) {
        if (null != mLastTitleTv) {
            mLastTitleTv.setText(lastTitle);
        }
    }

}
