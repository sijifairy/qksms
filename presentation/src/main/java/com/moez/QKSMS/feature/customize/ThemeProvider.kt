package com.moez.QKSMS.feature.customize

import com.moez.QKSMS.R
import com.moez.QKSMS.customize.Fonts
import java.util.*

object ThemeProvider {
    val allThemes: List<ThemeInfo>
        get() {
            val themeInfos: MutableList<ThemeInfo> = ArrayList()
            var themeInfo = ThemeInfo()
            themeInfo.themeId = "dark"
            themeInfo.primaryColor = -0xd39413
            themeInfo.title = "Dark Mode"
            themeInfo.baseDark = true
            themeInfo.description = "Simple optimized default theme for you."
            themeInfo.fontName = Fonts.FONT_DEFAULT
            themeInfo.themeResourceId = R.style.AppThemeRoseDream
            themeInfos.add(themeInfo)

            themeInfo = ThemeInfo()
            themeInfo.themeId = "elegant_rose"
            themeInfo.primaryColor = -0xf8a1ab
            themeInfo.title = "Elegant Rose"
            themeInfo.baseDark = true
            themeInfo.description = "Fancy theme!"
            themeInfo.fontName = "mali"
            themeInfo.listTitleColor = -0x1
            themeInfo.listSubtitleColor = -0x1
            themeInfo.themeResourceId = R.style.AppThemeElegantRose
            themeInfo.avatarsList = mutableListOf<Int>().apply {
                add(R.drawable.avatar_elegant_rose_1)
            }
            themeInfos.add(themeInfo)

            themeInfo = ThemeInfo()
            themeInfo.themeId = "light"
            themeInfo.primaryColor = -0x2fbb1
            themeInfo.title = "Style"
            themeInfo.baseDark = false
            themeInfo.description = "Fancy theme!"
            themeInfo.fontName = "merienda"
            themeInfo.listTitleColor = -0x1
            themeInfo.listSubtitleColor = -0x1
            themeInfo.themeResourceId = R.style.AppThemeRoseDream
            themeInfos.add(themeInfo)

            themeInfo = ThemeInfo()
            themeInfo.themeId = "honey"
            themeInfo.primaryColor = -0xf8a1ab
            themeInfo.title = "Elegant Rose"
            themeInfo.baseDark = true
            themeInfo.description = "Fancy theme!"
            themeInfo.fontName = "mali"
            themeInfo.listTitleColor = -0x1
            themeInfo.listSubtitleColor = -0x1
            themeInfo.themeResourceId = R.style.AppThemeHoneyPark
            themeInfo.avatarsList = mutableListOf<Int>().apply {
                add(R.drawable.avatar_honey_park_1)
                add(R.drawable.avatar_honey_park_2)
                add(R.drawable.avatar_honey_park_3)
                add(R.drawable.avatar_honey_park_4)
            }
            themeInfos.add(themeInfo)

            themeInfo = ThemeInfo()
            themeInfo.themeId = "rose_dream"
            themeInfo.primaryColor = -0xf8a1ab
            themeInfo.title = "Elegant Rose"
            themeInfo.baseDark = true
            themeInfo.description = "Fancy theme!"
            themeInfo.fontName = "mali"
            themeInfo.listTitleColor = -0x1
            themeInfo.listSubtitleColor = -0x1
            themeInfo.themeResourceId = R.style.AppThemeRoseDream
            themeInfo.avatarsList = mutableListOf<Int>().apply {
                add(R.drawable.avatar_rosedream_1)
                add(R.drawable.avatar_rosedream_2)
                add(R.drawable.avatar_rosedream_3)
                add(R.drawable.avatar_rosedream_4)
            }
            themeInfos.add(themeInfo)

            themeInfo = ThemeInfo()
            themeInfo.themeId = "color_ball"
            themeInfo.primaryColor = -0xf8a1ab
            themeInfo.title = "Elegant Rose"
            themeInfo.baseDark = true
            themeInfo.description = "Fancy theme!"
            themeInfo.fontName = "mali"
            themeInfo.listTitleColor = -0x1
            themeInfo.listSubtitleColor = -0x1
            themeInfo.themeResourceId = R.style.AppThemeColorBall
            themeInfo.avatarsList = mutableListOf<Int>().apply {
                add(R.drawable.avatar_color_ball_1)
            }
            themeInfos.add(themeInfo)

            return themeInfos
        }
}