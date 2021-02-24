package com.moez.QKSMS.feature.customize

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
            themeInfo.baseDark = false
            themeInfo.description = "Simple optimized default theme for you."
            themeInfo.fontName = Fonts.FONT_DEFAULT
            themeInfos.add(themeInfo)

            themeInfo = ThemeInfo()
            themeInfo.themeId = "elegant_rose"
            themeInfo.primaryColor = -0xf8a1ab
            themeInfo.title = "Elegant Rose"
            themeInfo.baseDark = false
            themeInfo.description = "Fancy theme!"
            themeInfo.fontName = "mali"
            themeInfo.listTitleColor = -0x1
            themeInfo.listSubtitleColor = -0x1
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
            themeInfos.add(themeInfo)

            return themeInfos
        }
}