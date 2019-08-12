package com.moez.QKSMS.common.dialog;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.moez.QKSMS.R;
import com.moez.QKSMS.common.util.Dimensions;
import com.moez.QKSMS.common.util.ViewUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public abstract class DefaultButtonDialog2 {

    private static final String TAG = DefaultButtonDialog2.class.getSimpleName();

    private static List<WeakReference<Dialog>> sDialogList = new ArrayList<>();

    private LayoutInflater mLayoutInflater;
    protected View mRootView;
    protected ImageView mTopImage;

    private Dialog mAlertDialog;
    private TextView mTitleTv;
    private TextView mDescTv;
    protected Drawable mTopImageDrawable;

    private int mThemeId;
    protected Activity mActivity;

    private int mDesiredWidth;
    /**
     * Dialog has content view (msg body), if false, we should not show dialog.
     */
    private boolean mContentViewReady;

    private boolean mInited;

    public DefaultButtonDialog2(Activity activity) {
        this(activity, 0);
    }

    public DefaultButtonDialog2(Activity activity, int themeId) {
        mActivity = activity;
        mThemeId = themeId;
    }

    protected void configDialog(AlertDialog.Builder builder) {
        configDialog(builder, true);
    }

    protected void configDialog(AlertDialog.Builder builder, boolean cancelable) {
        builder.setView(mRootView);
        builder.setCancelable(cancelable);
        builder.setOnDismissListener(dialog -> {
            Log.d(TAG, "onDismiss");
            removeDialog();
            onDismissComplete();
        });
        builder.setOnCancelListener(dialog -> {
            Log.d(TAG, "onCancel");
            DefaultButtonDialog2.this.onCanceled();
        });
        mAlertDialog = builder.create();
        mAlertDialog.setOnShowListener(dialog -> {
            Log.d(TAG, "OnShow");
            DefaultButtonDialog2.this.onShow();
        });

        mAlertDialog.setCanceledOnTouchOutside(false);

    }

    private void removeDialog() {
        for (WeakReference<Dialog> dialog : sDialogList) {
            try {
                if (dialog.get() != null && dialog.get() == mAlertDialog) {
                    sDialogList.remove(dialog);
                    return;
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    private void initIfNeed() {
        if (!mInited) {
            mInited = true;
            init();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void init() {
        mLayoutInflater = LayoutInflater.from(mActivity);
        Log.w("FDialog", "topDrawable: " + getTopImageDrawable());
        if (getTopImageDrawable() == null) {
            // No top image: double button style
            mRootView = mLayoutInflater.inflate(R.layout.dialog_compact_button_no_img, null);
        } else {
            // Has top image: big button style
            mRootView = mLayoutInflater.inflate(R.layout.dialog_compact_button, null);
        }
        Log.w("FDialog", "mRootView: " + mRootView);

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, mThemeId != 0 ? mThemeId : R.style.DefaultCompatDialog);
        configDialog(builder);

        int dialogElevation = mActivity.getResources().getDimensionPixelSize(R.dimen.dialog_elevation);
        if (enableElevation()) {
            mRootView.setElevation(dialogElevation);
        }

        final ViewGroup contentView = ViewUtils.findViewById(mRootView, R.id.content_view);

        contentView.addView(createContentView(mLayoutInflater, contentView));

        mTopImage = ViewUtils.findViewById(mRootView, R.id.horizontal_top_image);

        initTopImage(mTopImage);
        initButtons();
        mContentViewReady = true;
    }

    private boolean hasTopImage() {
        if (mTopImage.getBackground() != null) {
            mDesiredWidth = mTopImage.getBackground().getIntrinsicWidth();
            return true;
        }

        if (mTopImage.getDrawable() != null) {
            mDesiredWidth = mTopImage.getDrawable().getIntrinsicWidth();
            return true;
        }

        return false;
    }

    protected void initTopImage(ImageView imageView) {
        mTopImage.setImageDrawable(getTopImageDrawable());
    }

    protected void initButtons() {
        Button okButton = ViewUtils.findViewById(mRootView, R.id.ok_btn);
        View cancelButton = ViewUtils.findViewById(mRootView, R.id.cancel_btn);

        if (null != okButton) {
            okButton.setActivated(true);
            okButton.setText(getPositiveButtonString());
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickPositiveButton(v);
                }
            });
        }

        if (null != cancelButton) {
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickNegativeButton(v);
                    cancel();
                }
            });
        }
    }

    protected View createContentView(LayoutInflater inflater, ViewGroup root) {
        View v = inflater.inflate(R.layout.dialog_content_default, root, false);
        mTitleTv = ViewUtils.findViewById(v, R.id.dialog_title);
        mDescTv = ViewUtils.findViewById(v, R.id.dialog_desc);

        final CharSequence title = getDialogTitle();
        setDialogTitle(title);

        final CharSequence desc = getDialogDesc();
        setDialogDesc(desc);
        return v;
    }

    protected final void setDialogTitle(CharSequence title) {
        if (!TextUtils.isEmpty(title)) {
            mTitleTv.setVisibility(VISIBLE);
            mTitleTv.setText(title);
        } else {
            mTitleTv.setVisibility(GONE);
        }
    }

    protected final void setDialogDesc(CharSequence desc) {
        if (!TextUtils.isEmpty(desc)) {
            mDescTv.setVisibility(VISIBLE);
            mDescTv.setText(desc);
        } else {
            mDescTv.setVisibility(GONE);
        }
    }

    protected boolean enableElevation() {
        return true;
    }

    protected Drawable getTopImageDrawable() {
        return null;
    }

    protected CharSequence getDialogTitle() {
        return "";
    }

    protected CharSequence getDialogDesc() {
        return "";
    }

    protected String getPositiveButtonString() {
        return getResources().getString(getPositiveButtonStringId());
    }

    protected int getPositiveButtonStringId() {
        return android.R.string.ok;
    }

    // Not displayed on UI
    @Deprecated
    protected int getNegativeButtonStringId() {
        return android.R.string.cancel;
    }

    //-------- Dialog life circle callbacks (START)---------

    protected void onClickPositiveButton(View v) {
        dismiss();
    }

    protected void onClickNegativeButton(View v) {

    }


    protected void onShow() {

    }

    protected void onCanceled() {

    }

    protected void onDismissComplete() {
    }

    //-------- Dialog life circle callbacks (END)---------

    /**
     * Dialog width fit image width(If has).
     *
     * @return
     */
    protected boolean fitImageWidth() {
        return true;
    }

    protected final View findViewById(int id) {
        if (id == 0 || mRootView == null) {
            return null;
        }
        return mRootView.findViewById(id);
    }

    public final Resources getResources() {
        return getContext().getResources();
    }

    public final Context getContext() {
        return mActivity.getApplicationContext();
    }

    public final boolean show() {
        if (mActivity == null || mActivity.isFinishing() || mActivity.isDestroyed()) {
            return false;
        }

        initIfNeed();

        if (!mContentViewReady) {
            return false;
        }

        if (hasTopImage()) {
            mTopImage.setVisibility(VISIBLE);
        } else {
            mTopImage.setVisibility(GONE);
        }

        try {
            mAlertDialog.show();
        } catch (Exception e) {
            return false;
        }
        sDialogList.add(new WeakReference<>(mAlertDialog));

        if (mDesiredWidth > 0 && fitImageWidth()) {
            mTopImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

            View rootView = mAlertDialog.getWindow().getDecorView();
            int windowPadding = rootView != null ? rootView.getPaddingLeft() : 0;
            int width = mDesiredWidth + 2 * windowPadding;
            // SM-G3518 or other device may has no padding, so we shrink content.
            float ratio = windowPadding > 0 ? 0.95f : 0.86f;
            int maxWidth = (int) (Dimensions.getPhoneWidth(mActivity) * ratio + 0.5f);
            mAlertDialog.getWindow().setLayout(Math.min(width, maxWidth), ViewGroup.LayoutParams.WRAP_CONTENT);
            Log.d(TAG, "container padding = " + windowPadding);
        }
        return true;
    }

    /**
     * Don not call this when <b>click CANCEL</b> button!
     */
    public final void dismiss() {
        if (!mActivity.isDestroyed()) {
            mAlertDialog.dismiss();
        }
    }

    /**
     * Finally call {@link #dismiss()}
     */
    public final void cancel() {
        mAlertDialog.cancel();
    }

    public static void closeNow() {
        dismissDialogs();
    }

    public static void dismissDialogs() {
        for (WeakReference<Dialog> dialog : sDialogList) {
            try {
                if (dialog.get() != null) {
                    dialog.get().dismiss();
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        sDialogList.clear();
    }
}
