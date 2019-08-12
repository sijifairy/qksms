package com.moez.QKSMS.feature.feedback;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.util.ActivityUtils;
import com.moez.QKSMS.common.util.BackgroundDrawables;
import com.moez.QKSMS.common.util.Dimensions;
import com.moez.QKSMS.common.util.Toasts;

import java.util.HashMap;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FeedbackActivity extends AppCompatActivity {

    private String userEmail;
    private EditText feedbackContent;
    private EditText feedbackEmail;
    private Button sendButton;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityUtils.configStatusBarColor(this);
        setContentView(R.layout.activity_feedback);
        ActivityUtils.configSimpleAppBar(this, "Feedback", true, Color.WHITE, true);

        userEmail = getEmail(this);
        feedbackContent = findViewById(R.id.feedback_content);
        feedbackContent.setSingleLine(false);
        feedbackContent.setHorizontallyScrolling(false);
        feedbackContent.setBackgroundDrawable(BackgroundDrawables.createBackgroundDrawable(0xffeeeeee, Dimensions.pxFromDp(3), false));

        feedbackEmail = findViewById(R.id.email_address);
        feedbackEmail.setBackgroundDrawable(BackgroundDrawables.createBackgroundDrawable(0xffeeeeee, Dimensions.pxFromDp(3), false));
        if (!TextUtils.isEmpty(userEmail)) {
            feedbackEmail.setText(userEmail);
        }

        sendButton = findViewById(R.id.feedback_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!feedbackContent.getText().toString().trim().isEmpty()) {
                    HashMap<String, Object> content = getFeedbackContent();
                    try {
                        FirebaseDatabase.getInstance().getReference("feedbacks")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .push()
                                .setValue(content);
                    } catch (Exception e) {
                    }
                    finish();
                } else {
                    Toasts.showToast("Please enter your feedback before submit");
                    return;
                }
                Toasts.showToast("Thanks for your feedback!");
                sendButton.setClickable(false);
            }
        });
    }

    private HashMap<String, Object> getFeedbackContent() {
        HashMap<String, Object> content = new HashMap<>();
        content.put("Content", feedbackContent.getText().toString());
        content.put("Email", feedbackEmail.getText().toString());

        PackageManager pm = getPackageManager();
        String appVersionName = "";
        try {
            PackageInfo pInfo = pm.getPackageInfo(getPackageName(), 0);
            appVersionName = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException var2) {
            var2.printStackTrace();
        }
        content.put("version_name", appVersionName);
        content.put("os_version", Build.VERSION.RELEASE);
        content.put("device_model", Build.MODEL == null ? "" : Build.MODEL);
        content.put("country", Locale.getDefault().getCountry());
        content.put("language", Locale.getDefault().getLanguage());
        content.put("timestamp", System.currentTimeMillis());
        return content;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static String getEmail(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType("com.google");
        if (accounts.length > 0) {
            return accounts[0].name;
        } else {
            return "";
        }
    }
}
