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
import com.moez.QKSMS.common.util.Threads
import com.moez.QKSMS.common.util.Toasts
import com.moez.QKSMS.common.util.extensions.resolveThemeColor
import com.uber.autodispose.kotlin.autoDisposable
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.blocked_activity.toolbar
import kotlinx.android.synthetic.main.container_activity.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
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
                    val toolbarColor = resolveThemeColor(R.attr.toolbarBg)
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
                Threads.postOnThreadPoolExecutor(Runnable {
                    val content = getFeedbackContent()
                    try {
                        var reqParam = URLEncoder.encode("content", "UTF-8") + "=" + URLEncoder.encode(content, "UTF-8")

                        val mURL = URL("http://161.117.227.134:8080/feedback?$reqParam")

                        with(mURL.openConnection() as HttpURLConnection) {
                            // optional default is GET
                            requestMethod = "GET"

                            println("URL : $url")
                            println("Response Code : $responseCode")

                            BufferedReader(InputStreamReader(inputStream)).use {
                                val response = StringBuffer()

                                var inputLine = it.readLine()
                                while (inputLine != null) {
                                    response.append(inputLine)
                                    inputLine = it.readLine()
                                }
                                it.close()
                                println("Response : $response")
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                })

                finish()
            } else {
                Toasts.showToast("Please enter your feedback before submit")
                return@OnClickListener
            }
            Toasts.showToast("Thanks for your feedback!")
            sendButton!!.isClickable = false
        })
    }

    private fun getFeedbackContent(): String {
        val response = StringBuffer()
        response.append("content:").append(feedbackContent!!.text.toString()).append(";")
        response.append("email:").append(feedbackEmail!!.text.toString()).append(";")

        val pm = packageManager
        var appVersionName = ""
        try {
            val pInfo = pm.getPackageInfo(packageName, 0)
            appVersionName = pInfo.versionName
        } catch (var2: PackageManager.NameNotFoundException) {
            var2.printStackTrace()
        }
        response.append("version_name:").append(appVersionName).append(";")
        response.append("os_version:").append(Build.VERSION.RELEASE).append(";")
        response.append("device_model:").append(if (Build.MODEL == null) "" else Build.MODEL).append(";")
        response.append("country:").append(Locale.getDefault().country).append(";")
        response.append("language:").append(Locale.getDefault().language).append(";")
        response.append("timestamp:").append(System.currentTimeMillis()).append(";")

        return response.toString()
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
