package com.moez.QKSMS.feature.customize

import android.content.Context
import com.moez.QKSMS.util.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
        private val context: Context,
        private val prefs: Preferences
) {

    private val name = ""

    enum class ColorType {
        PRIMARY, LIST_TITLE, LIST_SUBTITLE
    }

    fun applyTheme(themeInfo: ThemeInfo) {
        prefs.themeId.set(themeInfo.themeId!!)
        prefs.fontFamily.set(themeInfo.fontName!!)
    }

    fun getColor(type: ColorType?): Int {
        return when (type) {
            ColorType.PRIMARY -> currentTheme!!.primaryColor
            ColorType.LIST_TITLE -> currentTheme!!.listTitleColor
            ColorType.LIST_SUBTITLE -> currentTheme!!.listSubtitleColor
            else -> currentTheme!!.primaryColor
        }
    }

    val currentThemeName: String
        get() = prefs.themeId.get()


    val isThemeApplied: Boolean
        get() = !(currentThemeName == DEFAULT_THEME_NAME_LIGHT || currentThemeName == DEFAULT_THEME_NAME_DARK)


    val currentTheme: ThemeInfo?
        get() {
            val themes = ThemeProvider.allThemes
            for (theme in themes) {
                if (theme.themeId == currentThemeName) {
                    return theme
                }
            }
            return null
        }

    companion object {
        const val DEFAULT_THEME_NAME_LIGHT = "light"
        const val DEFAULT_THEME_NAME_DARK = "dark"
    }
}