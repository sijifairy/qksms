package com.moez.QKSMS.feature.guide;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import android.view.View;

import com.klinker.android.send_message.Utils;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.BaseApplication;
import com.moez.QKSMS.common.util.BackgroundDrawables;
import com.moez.QKSMS.common.util.Dimensions;
import com.moez.QKSMS.common.util.Navigations;
import com.moez.QKSMS.common.util.SmsAnalytics;
import com.moez.QKSMS.feature.main.MainActivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SetAsDefaultActivity extends AppCompatActivity {

    private static final int REQUEST_SET_DEFAULT_SMS_APP = 3;
    private static final int EVENT_RETRY_NAVIGATION = 0;

    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == EVENT_RETRY_NAVIGATION) {
                if (OsUtil.hasRequiredPermissions()) {
                    Navigations.startActivitySafely(SetAsDefaultActivity.this,
                            new Intent(SetAsDefaultActivity.this, MainActivity.class));
                    finish();
                } else {
                    sendEmptyMessageDelayed(EVENT_RETRY_NAVIGATION, 100);
                }
            }
        }
    };

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Utils.isDefaultSmsApp(BaseApplication.getContext())) {
            finish();
            Navigations.startActivitySafely(this, new Intent(this, MainActivity.class));
            overridePendingTransition(0, 0);
        }

        setContentView(R.layout.activity_set_as_default);

        findViewById(R.id.btn_start).setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, Dimensions.pxFromDp(8), true));
        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                final Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, SetAsDefaultActivity.this.getPackageName());
                startActivityForResult(intent, REQUEST_SET_DEFAULT_SMS_APP);
                SmsAnalytics.logEvent("Start_Page_Button_Click");
            }
        });

        SmsAnalytics.logEvent("Start_Page_Show");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_SET_DEFAULT_SMS_APP) {
            if (Utils.isDefaultSmsApp(SetAsDefaultActivity.this)) {
                mHandler.sendEmptyMessageDelayed(EVENT_RETRY_NAVIGATION, 100);
                SmsAnalytics.logEvent("Start_Page_SetAsDefault_Success");
            } else {
                SmsAnalytics.logEvent("Start_Page_SetAsDefault_Failed");
            }
        }
    }
}
