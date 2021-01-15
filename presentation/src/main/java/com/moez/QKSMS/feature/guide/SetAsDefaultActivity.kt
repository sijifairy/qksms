package com.moez.QKSMS.feature.guide

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Telephony
import android.view.View
import android.widget.TextView
import com.klinker.android.send_message.Utils
import com.moez.QKSMS.R
import com.moez.QKSMS.common.BaseApplication
import com.moez.QKSMS.common.base.QkThemedActivity
import com.moez.QKSMS.common.util.*
import com.moez.QKSMS.feature.main.MainActivity
import dagger.android.AndroidInjection

class SetAsDefaultActivity : QkThemedActivity() {

    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == EVENT_RETRY_NAVIGATION) {
                if (OsUtil.hasRequiredPermissions()) {
                    Navigations.startActivitySafely(
                            this@SetAsDefaultActivity,
                            Intent(this@SetAsDefaultActivity, MainActivity::class.java))
                    finish()
                } else {
                    sendEmptyMessageDelayed(EVENT_RETRY_NAVIGATION, 100)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        if (Utils.isDefaultSmsApp(BaseApplication.getContext())) {
            Navigations.startActivitySafely(this, Intent(this, MainActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        setContentView(R.layout.activity_set_as_default)
        if (prefs.night.get()) {
            findViewById<View>(R.id.content).setBackgroundResource(R.color.backgroundDark)
            findViewById<TextView>(R.id.title1).setTextColor(resources.getColor(R.color.textPrimaryDark))
            findViewById<TextView>(R.id.title2).setTextColor(resources.getColor(R.color.textSecondaryDark))
        } else {
            findViewById<View>(R.id.content).setBackgroundResource(R.color.backgroundLight)
            findViewById<TextView>(R.id.title1).setTextColor(resources.getColor(R.color.textPrimary))
            findViewById<TextView>(R.id.title2).setTextColor(resources.getColor(R.color.textSecondary))
        }
        findViewById<View>(R.id.btn_start).background = BackgroundDrawables.createBackgroundDrawable(-0xc48816, Dimensions.pxFromDp(8f).toFloat(), true)
        findViewById<View>(R.id.btn_start)
                .setOnClickListener {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val roleManager = getSystemService(RoleManager::class.java)
                        // check if the app is having permission to be as default SMS app
                        val isRoleAvailable = roleManager.isRoleAvailable(RoleManager.ROLE_SMS)
                        if (isRoleAvailable) {
                            // check whether your app is already holding the default SMS app role.
                            val isRoleHeld = roleManager.isRoleHeld(RoleManager.ROLE_SMS)
                            if (!isRoleHeld) {
                                val roleRequestIntent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                                startActivityForResult(roleRequestIntent, REQUEST_SET_DEFAULT_SMS_APP)
                            } else {
                                mHandler.sendEmptyMessageDelayed(EVENT_RETRY_NAVIGATION, 100)
                            }
                        }
                    } else {
                        val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                        intent.putExtra(
                                Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                                this@SetAsDefaultActivity.packageName)
                        startActivityForResult(intent, REQUEST_SET_DEFAULT_SMS_APP)
                    }
                    SmsAnalytics.logEvent("Start_Page_Button_Click")
                }
        SmsAnalytics.logEvent("Start_Page_Show")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SET_DEFAULT_SMS_APP) {
            if (Utils.isDefaultSmsApp(this@SetAsDefaultActivity)) {
                mHandler.sendEmptyMessageDelayed(EVENT_RETRY_NAVIGATION, 100)
                SmsAnalytics.logEvent("Start_Page_SetAsDefault_Success")
            } else {
                SmsAnalytics.logEvent("Start_Page_SetAsDefault_Failed")
            }
        }
    }

    override fun onBackPressed() {

    }

    companion object {
        private const val REQUEST_SET_DEFAULT_SMS_APP = 3
        private const val EVENT_RETRY_NAVIGATION = 0
    }
}