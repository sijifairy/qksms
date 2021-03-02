package com.moez.QKSMS.feature.customize

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.moez.QKSMS.R
import com.moez.QKSMS.common.QkDialog
import com.moez.QKSMS.common.androidxcompat.scope
import com.moez.QKSMS.common.base.QkThemedActivity
import com.moez.QKSMS.common.util.Dimensions
import com.moez.QKSMS.common.util.extensions.resolveThemeColor
import com.uber.autodispose.kotlin.autoDisposable
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.container_activity.*
import javax.inject.Inject

class CustomizeThemeAct : QkThemedActivity() {
    @Inject
    lateinit var textSizeDialog: QkDialog

    @Inject
    lateinit var themeAdapter: ThemeAdapter
    
    private var textSizeId: Int = com.moez.QKSMS.util.Preferences.TEXT_SIZE_NORMAL

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customize_theme)

        setTitle(R.string.font_title)
        showBackButton(true)

        theme
                .autoDisposable(scope())
                .subscribe {
                    val toolbarColor = resolveThemeColor(R.attr.toolbarBg)
                    toolbar.setBackgroundColor(toolbarColor)
                    val toolbarTextColor = resolveThemeColor(android.R.attr.textColorPrimary)
                    toolbarTitle.setTextColor(toolbarTextColor)
                }

        val contentView: RecyclerView = findViewById(R.id.theme_list)

        val padding: Int = Dimensions.pxFromDp(8f)
        contentView.setPadding(padding, 0, padding, padding)
        contentView.clipToPadding = false // Stop edge effect from being clipped

        contentView.setHasFixedSize(true)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        contentView.layoutManager = layoutManager
        val spacing: Int = Dimensions.pxFromDp(8f)
        contentView.addItemDecoration(VerticalSpacingItemDecoration(spacing))
        contentView.adapter = themeAdapter
    }
}