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
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import com.akaita.java.rxjava2debug.RxJava2Debug
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.AppsFlyerLibCore.LOG_TAG
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.google.android.gms.ads.MobileAds
import com.moez.QKSMS.BuildConfig
import com.moez.QKSMS.R
import com.moez.QKSMS.common.util.FileLoggingTree
import com.moez.QKSMS.injection.AppComponentManager
import com.moez.QKSMS.injection.appComponent
import com.moez.QKSMS.manager.AnalyticsManager
import com.moez.QKSMS.migration.QkRealmMigration
import com.moez.QKSMS.util.NightModeManager
import dagger.android.*
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber
import javax.inject.Inject

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

        val crashlyticsKit = Crashlytics.Builder()
        crashlyticsKit.core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
        Fabric.with(this@QKApplication, crashlyticsKit.build())
        val conversionDataListener = object : AppsFlyerConversionListener {
            override fun onInstallConversionDataLoaded(conversionData: Map<String, String>) {
                for (attrName in conversionData.keys) {
                    Log.d(LOG_TAG, "conversion_attribute: " + attrName + " = " +
                            conversionData[attrName])
                }
            }

            override fun onInstallConversionFailure(errorMessage: String) {
                Log.d(LOG_TAG, "error onAttributionFailure : $errorMessage")
            }

            override fun onAppOpenAttribution(conversionData: Map<String, String>) {
                for (attrName in conversionData.keys) {
                    Log.d(LOG_TAG, "onAppOpen_attribute: " + attrName + " = " +
                            conversionData[attrName])
                }
            }

            override fun onAttributionFailure(errorMessage: String) {
                Log.d(LOG_TAG, "error onAttributionFailure : $errorMessage")
            }
        }
        AppsFlyerLib.getInstance().init("4N3JVcMXPziVis9ohCYuE", conversionDataListener, applicationContext)
        AppsFlyerLib.getInstance().startTracking(this)

        MobileAds.initialize(this, "ca-app-pub-5061957740026229~4750010097");
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

}