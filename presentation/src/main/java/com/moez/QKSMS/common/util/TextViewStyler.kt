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
package com.moez.QKSMS.common.util

import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.TextView
import com.moez.QKSMS.R
import com.moez.QKSMS.common.util.TextViewStyler.Companion.SIZE_PRIMARY
import com.moez.QKSMS.common.util.TextViewStyler.Companion.SIZE_SECONDARY
import com.moez.QKSMS.common.util.TextViewStyler.Companion.SIZE_TERTIARY
import com.moez.QKSMS.common.util.TextViewStyler.Companion.SIZE_TOOLBAR
import com.moez.QKSMS.common.util.extensions.getColorCompat
import com.moez.QKSMS.common.util.extensions.resolveThemeColor
import com.moez.QKSMS.common.widget.QkEditText
import com.moez.QKSMS.common.widget.QkTextView
import com.moez.QKSMS.customize.Fonts
import com.moez.QKSMS.util.Preferences
import javax.inject.Inject

class TextViewStyler @Inject constructor(
        private val prefs: Preferences,
        private val colors: Colors,
        private val fontProvider: FontProvider
) {

    companion object {
        const val COLOR_PRIMARY = 0
        const val COLOR_SECONDARY = 1
        const val COLOR_TERTIARY = 2
        const val COLOR_PRIMARY_ON_THEME = 3
        const val COLOR_SECONDARY_ON_THEME = 4
        const val COLOR_TERTIARY_ON_THEME = 5
        const val COLOR_THEME = 6

        const val SIZE_PRIMARY = 0
        const val SIZE_SECONDARY = 1
        const val SIZE_TERTIARY = 2
        const val SIZE_TOOLBAR = 3
        const val SIZE_DIALOG = 4

        const val FONT_REGULAR = 0
        const val FONT_MEDIUM = 1
        const val FONT_BOLD = 2

        fun applyEditModeAttributes(textView: TextView, attrs: AttributeSet?) {
            textView.run {
                var colorAttr = 0
                var textSizeAttr = 0

                when (this) {
                    is QkTextView -> context.obtainStyledAttributes(attrs, R.styleable.QkTextView)?.run {
                        colorAttr = getInt(R.styleable.QkTextView_textColor, -1)
                        textSizeAttr = getInt(R.styleable.QkTextView_textSize, -1)
                        recycle()
                    }

                    is QkEditText -> context.obtainStyledAttributes(attrs, R.styleable.QkEditText)?.run {
                        colorAttr = getInt(R.styleable.QkEditText_textColor, -1)
                        textSizeAttr = getInt(R.styleable.QkEditText_textSize, -1)
                        recycle()
                    }

                    else -> return
                }
                setTextColor(when (colorAttr) {
                    COLOR_PRIMARY -> context.getColorCompat(R.color.textPrimary)
                    COLOR_SECONDARY -> context.getColorCompat(R.color.textSecondary)
                    COLOR_TERTIARY -> context.getColorCompat(R.color.textTertiary)
                    COLOR_PRIMARY_ON_THEME -> context.getColorCompat(R.color.textPrimaryDark)
                    COLOR_SECONDARY_ON_THEME -> context.getColorCompat(R.color.textSecondaryDark)
                    COLOR_TERTIARY_ON_THEME -> context.getColorCompat(R.color.textTertiaryDark)
                    COLOR_THEME -> context.getColorCompat(R.color.tools_theme)
                    else -> currentTextColor
                })

                textSize = when (textSizeAttr) {
                    SIZE_PRIMARY -> 16f
                    SIZE_SECONDARY -> 14f
                    SIZE_TERTIARY -> 12f
                    SIZE_TOOLBAR -> 20f
                    SIZE_DIALOG -> 18f
                    else -> textSize / paint.density
                }
            }
        }
    }

    fun applyAttributes(textView: TextView, attrs: AttributeSet?) {
        textView.run {
            var colorAttr = 0
            var textSizeAttr = 0
            var fontStyle = 0
            var fontFamilyChangeable = true

//            if (!prefs.systemFont.get()) {
//                fontProvider.getLato { lato ->
//                    setTypeface(lato, typeface?.style ?: Typeface.NORMAL)
//                }
//            }

            when (this) {
                is QkTextView -> context.obtainStyledAttributes(attrs, R.styleable.QkTextView)?.run {
                    colorAttr = getInt(R.styleable.QkTextView_textColor, -1)
                    textSizeAttr = getInt(R.styleable.QkTextView_textSize, -1)
                    fontStyle = getInt(R.styleable.QkTextView_font_family, -1)
                    fontFamilyChangeable = getBoolean(R.styleable.QkTextView_font_family_changeable, true)
                    recycle()
                }

                is QkEditText -> context.obtainStyledAttributes(attrs, R.styleable.QkEditText)?.run {
                    colorAttr = getInt(R.styleable.QkEditText_textColor, -1)
                    textSizeAttr = getInt(R.styleable.QkEditText_textSize, -1)
                    fontStyle = getInt(R.styleable.QkEditText_font_family, -1)
                    recycle()
                }

                else -> return
            }

            setTextColor(when (colorAttr) {
                COLOR_PRIMARY -> context.resolveThemeColor(android.R.attr.textColorPrimary)
                COLOR_SECONDARY -> context.resolveThemeColor(android.R.attr.textColorSecondary)
                COLOR_TERTIARY -> context.resolveThemeColor(android.R.attr.textColorTertiary)
                COLOR_PRIMARY_ON_THEME -> colors.theme().textPrimary
                COLOR_SECONDARY_ON_THEME -> colors.theme().textSecondary
                COLOR_TERTIARY_ON_THEME -> colors.theme().textTertiary
                COLOR_THEME -> colors.theme().theme
                else -> currentTextColor
            })

            setTextSize(textView, textSizeAttr)
            if (!prefs.systemFont.get() && fontFamilyChangeable) {
                setFontFamily(textView, fontStyle)
            }
        }
    }

    /**
     * @see FONT_REGULAR
     * @see FONT_MEDIUM
     * @see FONT_BOLD
     */
    fun setFontFamily(textView: TextView, fontStyleParam: Int) {
        if (TextUtils.equals(prefs.fontFamily.get(), Fonts.FONT_DEFAULT)) {
            val path = when (fontStyleParam) {
                FONT_REGULAR -> {
                    "fonts/Custom-Regular.ttf"
                }
                FONT_MEDIUM -> {
                    "fonts/Custom-Medium.ttf"
                }
                FONT_BOLD -> {
                    "fonts/Custom-Bold.ttf"
                }
                else -> {
                    "fonts/Custom-Regular.ttf"
                }
            }
            textView.typeface = Typeface.createFromAsset(textView.context.assets, path)
        } else {
            val path = when (fontStyleParam) {
                FONT_REGULAR -> {
                    "fonts/" + prefs.fontFamily.get() + "/Regular.ttf"
                }
                FONT_MEDIUM -> {
                    "fonts/" + prefs.fontFamily.get() + "/Medium.ttf"
                }
                FONT_BOLD -> {
                    "fonts/" + prefs.fontFamily.get() + "/SemiBold.ttf"
                }
                else -> {
                    "fonts/" + prefs.fontFamily.get() + "/Regular.ttf"
                }
            }
            var typeface: Typeface? = null
            try {
                typeface = Typeface.createFromAsset(textView.context.assets, path)
            } catch (e: Exception) {
            }
            if (typeface != null) {
                textView.typeface = typeface
            } else {
                textView.typeface = Typeface.createFromAsset(textView.context.assets,
                        "fonts/" + prefs.fontFamily.get() + "/Regular.ttf")
            }
        }
    }

    fun setFontFamily(textView: TextView, fontFamily: String, fontStyle: Int) {
        if (TextUtils.equals(fontFamily, Fonts.FONT_DEFAULT)) {
            val path = when (fontStyle) {
                FONT_REGULAR -> {
                    "fonts/Custom-Regular.ttf"
                }
                FONT_MEDIUM -> {
                    "fonts/Custom-Medium.ttf"
                }
                FONT_BOLD -> {
                    "fonts/Custom-Bold.ttf"
                }
                else -> {
                    "fonts/Custom-Regular.ttf"
                }
            }
            textView.typeface = Typeface.createFromAsset(textView.context.assets, path)
        } else {
            val path = when (fontStyle) {
                FONT_REGULAR -> {
                    "fonts/$fontFamily/Regular.ttf"
                }
                FONT_MEDIUM -> {
                    "fonts/$fontFamily/Medium.ttf"
                }
                FONT_BOLD -> {
                    "fonts/$fontFamily/SemiBold.ttf"
                }
                else -> {
                    "fonts/$fontFamily/Regular.ttf"
                }
            }
            var typeface: Typeface? = null
            try {
                typeface = Typeface.createFromAsset(textView.context.assets, path)
            } catch (e: Exception) {
            }
            if (typeface != null) {
                textView.typeface = typeface
            } else {
                textView.typeface = Typeface.createFromAsset(textView.context.assets,
                        "fonts/$fontFamily/Regular.ttf")
            }
        }
    }


    /**
     * @see SIZE_PRIMARY
     * @see SIZE_SECONDARY
     * @see SIZE_TERTIARY
     * @see SIZE_TOOLBAR
     */
    fun setTextSize(textView: TextView, textSizeAttr: Int) {
        val textSizePref = prefs.textSize.get()
        when (textSizeAttr) {
            SIZE_PRIMARY -> textView.textSize = when (textSizePref) {
                Preferences.TEXT_SIZE_SMALL -> 13.3f
                Preferences.TEXT_SIZE_NORMAL -> 16f
                Preferences.TEXT_SIZE_LARGE -> 18f
                Preferences.TEXT_SIZE_LARGER -> 20f
                else -> 16f
            }

            SIZE_SECONDARY -> textView.textSize = when (textSizePref) {
                Preferences.TEXT_SIZE_SMALL -> 11f
                Preferences.TEXT_SIZE_NORMAL -> 13.3f
                Preferences.TEXT_SIZE_LARGE -> 15f
                Preferences.TEXT_SIZE_LARGER -> 18f
                else -> 14f
            }

            SIZE_TERTIARY -> textView.textSize = when (textSizePref) {
                Preferences.TEXT_SIZE_SMALL -> 10f
                Preferences.TEXT_SIZE_NORMAL -> 12f
                Preferences.TEXT_SIZE_LARGE -> 14f
                Preferences.TEXT_SIZE_LARGER -> 16f
                else -> 12f
            }

            SIZE_TOOLBAR -> textView.textSize = when (textSizePref) {
                Preferences.TEXT_SIZE_SMALL -> 18f
                Preferences.TEXT_SIZE_NORMAL -> 20f
                Preferences.TEXT_SIZE_LARGE -> 22f
                Preferences.TEXT_SIZE_LARGER -> 26f
                else -> 20f
            }

            SIZE_DIALOG -> textView.textSize = when (textSizePref) {
                Preferences.TEXT_SIZE_SMALL -> 16f
                Preferences.TEXT_SIZE_NORMAL -> 18f
                Preferences.TEXT_SIZE_LARGE -> 20f
                Preferences.TEXT_SIZE_LARGER -> 24f
                else -> 18f
            }
        }
    }

}