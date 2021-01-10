package com.moez.QKSMS.feature.feedback

import android.accounts.AccountManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.moez.QKSMS.R
import com.moez.QKSMS.common.androidxcompat.scope
import com.moez.QKSMS.common.base.QkThemedActivity
import com.moez.QKSMS.common.util.BackgroundDrawables
import com.moez.QKSMS.common.util.Dimensions
import com.moez.QKSMS.common.util.Toasts
import com.moez.QKSMS.common.util.extensions.resolveThemeColor
import com.uber.autodispose.kotlin.autoDisposable
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.blocked_activity.*
import kotlinx.android.synthetic.main.blocked_activity.toolbar
import kotlinx.android.synthetic.main.container_activity.*
import kotlinx.android.synthetic.main.container_activity.toolbarTitle
import kotlinx.android.synthetic.main.main_activity.*
import java.util.*

class FeedbackActivity : QkThemedActivity() {

    private var userEmail: String? = null
    private var feedbackContent: EditText? = null
    private var feedbackEmail: EditText? = null
    private var sendButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_feedback)

        setTitle(R.string.drawer_help)
        showBackButton(true)

        theme
                .autoDisposable(scope())
                .subscribe { theme ->
                    val toolbarColor = resolveThemeColor(android.R.attr.windowBackground)
                    toolbar.setBackgroundColor(toolbarColor)
                    val toolbarTextColor = resolveThemeColor(android.R.attr.textColorPrimary)
                    toolbarTitle.setTextColor(toolbarTextColor)
                }

        userEmail = getEmail(this)
        feedbackContent = findViewById(R.id.feedback_content)
        feedbackContent!!.setSingleLine(false)
        feedbackContent!!.setHorizontallyScrolling(false)
        feedbackContent!!.setBackgroundDrawable(BackgroundDrawables.createBackgroundDrawable(-0x111112, Dimensions.pxFromDp(3f).toFloat(), false))

        feedbackEmail = findViewById(R.id.email_address)
        feedbackEmail!!.setBackgroundDrawable(BackgroundDrawables.createBackgroundDrawable(-0x111112, Dimensions.pxFromDp(3f).toFloat(), false))
        if (!TextUtils.isEmpty(userEmail)) {
            feedbackEmail!!.setText(userEmail)
        }

        sendButton = findViewById(R.id.feedback_send)
        sendButton!!.setOnClickListener(View.OnClickListener {
            if (!feedbackContent!!.text.toString().trim { it <= ' ' }.isEmpty()) {
                val content = getFeedbackContent()
                try {
                } catch (e: Exception) {
                }

                finish()
            } else {
                Toasts.showToast("Please enter your feedback before submit")
                return@OnClickListener
            }
            Toasts.showToast("Thanks for your feedback!")
            sendButton!!.isClickable = false
        })
    }

    private fun getFeedbackContent(): HashMap<String, Any> {
        val content = HashMap<String, Any>()
        content["Content"] = feedbackContent!!.text.toString()
        content["Email"] = feedbackEmail!!.text.toString()

        val pm = packageManager
        var appVersionName = ""
        try {
            val pInfo = pm.getPackageInfo(packageName, 0)
            appVersionName = pInfo.versionName
        } catch (var2: PackageManager.NameNotFoundException) {
            var2.printStackTrace()
        }

        content["version_name"] = appVersionName
        content["os_version"] = Build.VERSION.RELEASE
        content["device_model"] = if (Build.MODEL == null) "" else Build.MODEL
        content["country"] = Locale.getDefault().country
        content["language"] = Locale.getDefault().language
        content["timestamp"] = System.currentTimeMillis()
        return content
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun getEmail(context: Context): String {
            val accountManager = AccountManager.get(context)
            val accounts = accountManager.getAccountsByType("com.google")
            return if (accounts.size > 0) {
                accounts[0].name
            } else {
                ""
            }
        }
    }
}
