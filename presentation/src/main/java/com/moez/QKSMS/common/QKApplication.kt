/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.common

import android.app.Activity
import android.app.ActivityManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Process
import android.provider.Settings
import android.text.TextUtils
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import com.flurry.android.FlurryAgent
import com.moez.QKSMS.R
import com.moez.QKSMS.common.util.FileLoggingTree
import com.moez.QKSMS.common.util.Preferences
import com.moez.QKSMS.common.util.SmsAnalytics
import com.moez.QKSMS.feature.guide.topapp.TopAppManager
import com.moez.QKSMS.injection.AppComponentManager
import com.moez.QKSMS.injection.appComponent
import com.moez.QKSMS.manager.AnalyticsManager
import com.moez.QKSMS.migration.QkRealmMigration
import com.moez.QKSMS.util.NightModeManager
import dagger.android.*
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import javax.inject.Inject
import com.google.android.gms.ads.MobileAds;

class QKApplication : BaseApplication(), HasActivityInjector, HasBroadcastReceiverInjector, HasServiceInjector {

    /**
     * Inject this so that it is forced to initialize
     */
    @Suppress("unused")
    @Inject
    lateinit var analyticsManager: AnalyticsManager

    @Inject
    lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var dispatchingBroadcastReceiverInjector: DispatchingAndroidInjector<BroadcastReceiver>

    @Inject
    lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>

    @Inject
    lateinit var fileLoggingTree: FileLoggingTree

    @Inject
    lateinit var nightModeManager: NightModeManager

    companion object {
        lateinit var context: Context
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        context = this
    }

    override fun onCreate() {
        super.onCreate()

        FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, "Z6WVFVX93P2TK2WKYXY4")

        MobileAds.initialize(this) {}

        val packageName = packageName
        val processName = getCurrentProcessName(this)
        val isOnMainProcess = TextUtils.equals(processName, packageName)
        if (isOnMainProcess) {
            Realm.init(this)
            Realm.setDefaultConfiguration(RealmConfiguration.Builder()
                    .compactOnLaunch()
                    .migration(QkRealmMigration())
                    .schemaVersion(QkRealmMigration.SCHEMA_VERSION)
                    .build())

            AppComponentManager.init(this)
            appComponent.inject(this)

            packageManager.getInstallerPackageName(packageName)?.let { installer ->
                analyticsManager.setUserProperty("Installer", installer)
            }

            nightModeManager.updateCurrentTheme()

            val fontRequest = FontRequest(
                    "com.google.android.gms.fonts",
                    "com.google.android.gms",
                    "Noto Color Emoji Compat",
                    R.array.com_google_android_gms_fonts_certs)

            EmojiCompat.init(FontRequestEmojiCompatConfig(this, fontRequest))

            Timber.plant(Timber.DebugTree(), fileLoggingTree)

            val uri = Settings.Secure.getUriFor("sms_default_application")
            val context = applicationContext
            context.contentResolver.registerContentObserver(uri, false, DefaultSmsAppChangeObserver(null))

            TopAppManager.getInstance().startPollingTask()

            if (!Preferences.getDefault().contains("pref_key_install_time")) {
                Preferences.getDefault().putLong("pref_key_install_time", System.currentTimeMillis())
            }

            SmsAnalytics.logEvent("Process_Start")
        }
    }

    override fun activityInjector(): AndroidInjector<Activity> {
        return dispatchingActivityInjector
    }

    override fun broadcastReceiverInjector(): AndroidInjector<BroadcastReceiver> {
        return dispatchingBroadcastReceiverInjector
    }

    override fun serviceInjector(): AndroidInjector<Service> {
        return dispatchingServiceInjector
    }

    fun getCurrentProcessName(context: Context): String? {
        var processName: String? = null

        try {
            val file = File("/proc/" + Process.myPid() + "/cmdline")
            val bufferedReader = BufferedReader(FileReader(file))
            processName = bufferedReader.readLine().trim { it <= ' ' }
            bufferedReader.close()
        } catch (var6: Exception) {
            var6.printStackTrace()
        }

        if (TextUtils.isEmpty(processName)) {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val processInfos = activityManager.runningAppProcesses
            if (processInfos != null) {
                val pid = Process.myPid()
                val var4 = processInfos.iterator()

                while (var4.hasNext()) {
                    val appProcess = var4.next() as ActivityManager.RunningAppProcessInfo
                    if (appProcess.pid == pid) {
                        processName = appProcess.processName
                        break
                    }
                }
            }
        }

        return processName
    }
}