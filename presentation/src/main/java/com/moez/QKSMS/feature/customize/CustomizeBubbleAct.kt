package com.moez.QKSMS.feature.customize

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.moez.QKSMS.R
import com.moez.QKSMS.common.androidxcompat.scope
import com.moez.QKSMS.common.base.QkThemedActivity
import com.moez.QKSMS.common.util.Preferences
import com.moez.QKSMS.common.util.extensions.resolveThemeColor
import com.moez.QKSMS.common.widget.QkTextView
import com.moez.QKSMS.feature.compose.BubbleUtils
import com.uber.autodispose.kotlin.autoDisposable
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_customize_bubble.*

class CustomizeBubbleAct : QkThemedActivity() {

    private lateinit var layoutPreview: CustomizePreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customize_bubble)
        layoutPreview = findViewById<CustomizePreviewView>(R.id.layout_preview)

        setTitle(R.string.bubble_title)
        showBackButton(true)

        theme
                .autoDisposable(scope())
                .subscribe { theme ->
                    val toolbarColor = resolveThemeColor(R.attr.toolbarBg)
                    toolbar.setBackgroundColor(toolbarColor)
                    val toolbarTextColor = resolveThemeColor(android.R.attr.textColorPrimary)
                    toolbarTitle.setTextColor(toolbarTextColor)
                    layoutPreview.init(theme)
                }

        val recyclerView = findViewById<RecyclerView>(R.id.bubble_list)
        recyclerView.adapter = BubbleListAdapter()
        recyclerView.layoutManager = GridLayoutManager(this, 2)
    }

    private inner class BubbleListAdapter : RecyclerView.Adapter<FontListHolder>() {

        var bubbles = BubbleUtils.BUBBLE_INFOS

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FontListHolder {
            return FontListHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.bubble_item, parent, false))
        }

        override fun onBindViewHolder(holder: FontListHolder, position: Int) {
            holder.ivSelected.visibility = if (bubbles[position].id ==
                    Preferences.getDefault().getString(BubbleUtils.PREF_KEY_BUBBLE_INDEX, BubbleUtils.DEFAULT_BUBBLE_INDEX))
                View.VISIBLE
            else View.GONE

            holder.itemView
                    .setOnClickListener { v: View? ->
                        Preferences.getDefault().putString(BubbleUtils.PREF_KEY_BUBBLE_INDEX, bubbles[position].id)
                        notifyDataSetChanged()

                        layoutPreview.refresh()
                    }
            holder.preview.setImageResource(BubbleUtils.getPreviewDrawable(bubbles[position].id))
            holder.name.text = bubbles[position].name
            holder.name.setTextColor(bubbles[position].color)
        }

        override fun getItemCount(): Int {
            return bubbles.size
        }
    }

    private inner class FontListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivSelected: ImageView = itemView.findViewById(R.id.check_mark)
        val preview: ImageView = itemView.findViewById(R.id.bubble_item)
        val name: QkTextView = itemView.findViewById(R.id.name)
    }
}