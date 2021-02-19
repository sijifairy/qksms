package com.moez.QKSMS.feature.fonts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.moez.QKSMS.R
import com.moez.QKSMS.common.QkDialog
import com.moez.QKSMS.common.androidxcompat.scope
import com.moez.QKSMS.common.base.QkThemedActivity
import com.moez.QKSMS.common.util.BackgroundDrawables
import com.moez.QKSMS.common.util.Dimensions
import com.moez.QKSMS.common.util.extensions.resolveThemeColor
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.widget.QkTextView
import com.moez.QKSMS.customize.Fonts
import com.uber.autodispose.kotlin.autoDisposable
import dagger.android.AndroidInjection
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_customize_font.*
import kotlinx.android.synthetic.main.container_activity.toolbar
import kotlinx.android.synthetic.main.container_activity.toolbarTitle
import javax.inject.Inject

class CustomizeFontAct : QkThemedActivity() {
    @Inject
    lateinit var textSizeDialog: QkDialog
    private var textSizeId: Int = com.moez.QKSMS.util.Preferences.TEXT_SIZE_NORMAL

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customize_font)

        setTitle(R.string.font_title)
        showBackButton(true)

        theme
                .autoDisposable(scope())
                .subscribe { theme ->
                    val toolbarColor = resolveThemeColor(R.attr.toolbarBg)
                    toolbar.setBackgroundColor(toolbarColor)
                    val toolbarTextColor = resolveThemeColor(android.R.attr.textColorPrimary)
                    toolbarTitle.setTextColor(toolbarTextColor)

                    message_out.setTextColor(resolveThemeColor(android.R.attr.textColorPrimary))
                    message_out.setBackgroundTint(resolveThemeColor(R.attr.inMessageBackground))
                    message_in.setBackgroundTint(theme.theme)
                    message_in.setTextColor(theme.textPrimary)
                }

        textSizeDialog.adapter.setData(R.array.text_sizes)
        textSizeDialog.adapter.selectedItem = textSizeId
        val textSizeLabels = resources.getStringArray(R.array.text_sizes)
        prefs.textSize.asObservable()
                .subscribe { textSize ->
                    textSizeId = textSize
                    textSizeDialog.adapter.selectedItem = textSizeId
                    text_size.text = textSizeLabels[textSize]
                }
        textSizeSelected()
                .autoDisposable(scope())
                .subscribe(prefs.textSize::set)
        title_size.setOnClickListener { showTextSizePicker() }

        val recyclerView = findViewById<RecyclerView>(R.id.font_list)
        recyclerView.adapter = FontListAdapter()
        recyclerView.layoutManager = GridLayoutManager(this, 2)
    }

    fun textSizeSelected(): Observable<Int> = textSizeDialog.adapter.menuItemClicks
    fun showTextSizePicker() = textSizeDialog.show(this)

    private inner class FontListAdapter : RecyclerView.Adapter<FontListHolder>() {
        var fonts = Fonts.fonts
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FontListHolder {
            return FontListHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.font_item, parent, false))
        }

        override fun onBindViewHolder(holder: FontListHolder, position: Int) {
            holder.name.setFontFamily(fonts.get(position).fontId);
            holder.name.text = fonts[position].fontName
            holder.ex.setFontFamily(fonts.get(position).fontId);
            holder.ivSelected.visibility = if (fonts.get(position).fontId.equals(prefs.fontFamily.get())) View.VISIBLE else View.GONE

            holder.itemView
                    .findViewById<View>(R.id.content)
                    .setOnClickListener { v: View? ->
                        prefs.fontFamily.set(fonts.get(position).fontId)
//                FontStyleManager.getInstance().setFontFamily(fonts.get(position).fontId);
//                FontUtils.onFontTypefaceChanged();
//                loadAndChangeFontTypeface(PageFont.this, fonts.get(position).fontId);
//                EventBus.getDefault().post(new ThemeEvent());
                        notifyDataSetChanged()
                    }
        }

        override fun getItemCount(): Int {
            return fonts.size
        }
    }

    private inner class FontListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: QkTextView = itemView.findViewById(R.id.name)
        val ex: QkTextView = itemView.findViewById(R.id.ex)
        val ivSelected: ImageView = itemView.findViewById(R.id.iv_selected)

        init {
            itemView
                    .findViewById<View>(R.id.content)
                    .setBackgroundDrawable(
                            BackgroundDrawables.createBackgroundDrawable(-0x70706,
                                    Dimensions.pxFromDp(4f).toFloat(),
                                    false))
        }
    }
}