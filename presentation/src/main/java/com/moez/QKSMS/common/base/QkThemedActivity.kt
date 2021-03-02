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
package com.moez.QKSMS.common.base

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.view.iterator
import androidx.lifecycle.Lifecycle
import com.klinker.android.send_message.Utils
import com.moez.QKSMS.R
import com.moez.QKSMS.common.BaseApplication
import com.moez.QKSMS.common.androidxcompat.scope
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.resolveThemeColor
import com.moez.QKSMS.common.util.setSystemButtonsTheme
import com.moez.QKSMS.feature.customize.ThemeManager
import com.moez.QKSMS.feature.guide.SetAsDefaultActivity
import com.moez.QKSMS.util.Preferences
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.toolbar.*
import javax.inject.Inject

/**
 * Base activity that automatically applies any necessary theme theme settings and colors
 *
 * In most cases, this should be used instead of the base QkActivity, except for when
 * an activity does not depend on the theme
 */
abstract class QkThemedActivity : QkActivity() {

    @Inject
    lateinit var colors: Colors

    @Inject
    lateinit var prefs: Preferences

    /**
     * In case the activity should be themed for a specific conversation, the selected conversation
     * can be changed by pushing the threadId to this subject
     */
    val threadId: Subject<Long> = BehaviorSubject.createDefault(0)

    /**
     * Switch the theme if the threadId changes
     */
    val theme = threadId
            .distinctUntilChanged()
            .switchMap { threadId -> colors.themeObservable(threadId) }

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {

        val night = prefs.night.get()
        val black = prefs.black.get()
        setTheme(getActivityThemeRes(night, black))

        if (themeManager.isThemeApplied) {
            setTransparentStatusBar()
        }

        super.onCreate(savedInstanceState)
        if (!themeManager.isThemeApplied) {
            val statusBarColor = resolveThemeColor(R.attr.toolbarBg)
            setStatusBarColor(statusBarColor)
        }
        if (themeManager.isThemeApplied) {
            setSystemButtonsTheme(window, themeManager.currentTheme?.baseDark == false)
        } else {
            setSystemButtonsTheme(window, !prefs.black.get() and !prefs.night.get())
        }
        // When certain preferences change, we need to recreate the activity
        Observable.merge(
                listOf(prefs.night, prefs.black, prefs.textSize, prefs.systemFont, prefs.fontFamily, prefs.themeId).map { it.asObservable().skip(1) })
                .autoDisposable(scope())
                .subscribe {
                    recreate()
                }

        // Set the color for the status bar icons
        // If night mode, or no dark icons supported, use light icons
        // If night mode and only dark status icons supported, use dark status icons
        // If night mode and all dark icons supported, use all dark icons
        // window.decorView.systemUiVisibility = 0

        // Some devices don't let you modify android.R.attr.navigationBarColor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = resolveThemeColor(android.R.attr.windowBackground)
        }

        // Set the color for the recent apps title
        val toolbarColor = resolveThemeColor(R.attr.colorPrimary)
        val icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        val taskDesc = ActivityManager.TaskDescription(getString(R.string.app_name), icon, toolbarColor)
        setTaskDescription(taskDesc)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Set the color for the overflow and navigation icon
        val textColorPrimary = resolveThemeColor(android.R.attr.textColorPrimary)
        toolbar?.overflowIcon = toolbar?.overflowIcon?.apply { setTint(textColorPrimary) }

        // Update the colours of the menu items
        Observables.combineLatest(menu, theme) { menu, theme ->
            menu.iterator().forEach { menuItem ->
                val tint = when (menuItem.itemId) {
                    in getColoredMenuItems() -> theme.theme
                    else -> textColorPrimary
                }

                menuItem.icon = menuItem.icon?.apply { setTint(tint) }
            }
        }.autoDisposable(scope(Lifecycle.Event.ON_DESTROY)).subscribe()
    }

    open fun getColoredMenuItems(): List<Int> {
        return listOf()
    }

    /**
     * This can be overridden in case an activity does not want to use the default themes
     */
    open fun getActivityThemeRes(night: Boolean, black: Boolean) = when {
        themeManager.isThemeApplied -> {
            themeManager.currentTheme?.themeResourceId ?: 0
        }
        night && black -> R.style.AppThemeBlack
        night && !black -> R.style.AppThemeDark
        else -> R.style.AppThemeLight
    }

    @ColorInt
    fun getColorDark(color: Int): Int {
        val blendedRed = Math.floor(0.8 * Color.red(color)).toInt()
        val blendedGreen = Math.floor(0.8 * Color.green(color)).toInt()
        val blendedBlue = Math.floor(0.8 * Color.blue(color)).toInt()
        return Color.rgb(blendedRed, blendedGreen, blendedBlue)
    }

    fun setStatusBarColor(color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // we need statusbar color same as actionbar color
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val decor = window.decorView
                if (color == Color.WHITE) {
                    //decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    // We want to change tint color to white again.
                    // You can also record the flags in advance so that you can turn UI back completely if
                    // you have set other flags before, such as translucent or full screen.
                    decor.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                }
                window.statusBarColor = color
            } else {
                if (color == Color.WHITE) {
                    val blendedRed = Math.floor(0.8 * Color.red(color)).toInt()
                    val blendedGreen = Math.floor(0.8 * Color.green(color)).toInt()
                    val blendedBlue = Math.floor(0.8 * Color.blue(color)).toInt()
                    window.statusBarColor = Color.rgb(blendedRed, blendedGreen, blendedBlue)
                } else {
                    window.statusBarColor = color
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (this !is SetAsDefaultActivity && !Utils.isDefaultSmsApp(this)) {
            val intent = Intent(this, SetAsDefaultActivity::class.java)
            startActivity(intent)
        }
    }
}