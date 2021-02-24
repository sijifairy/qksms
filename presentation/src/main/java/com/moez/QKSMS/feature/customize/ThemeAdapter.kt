package com.moez.QKSMS.feature.customize

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.moez.QKSMS.R
import com.moez.QKSMS.injection.appComponent
import com.moez.QKSMS.util.Preferences
import java.util.*
import javax.inject.Inject

/** Data provider for local theme gallery.  */
class ThemeAdapter @Inject constructor(
        private val prefs: Preferences,
        private val themeManager: ThemeManager
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), View.OnClickListener {
    private val mData: MutableList<Any> = ArrayList()
    override fun getItemCount(): Int {
        return mData.size
    }

    override fun getItemViewType(position: Int): Int {
        val obj = mData[position]
        return if (obj is ThemeInfo) {
            VIEW_TYPE_THEME
        } else {
            VIEW_TYPE_MORE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_THEME) {
            val grid: View = LayoutInflater.from(parent.context).inflate(R.layout.customize_theme_list_item, parent, false)
            ThemeViewHolder(grid)
        } else {
            val foot: View = LayoutInflater.from(parent.context).inflate(R.layout.customize_theme_list_more, parent, false)
            MoreViewHolder(foot)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        if (viewType == VIEW_TYPE_MORE) {
            (holder as MoreViewHolder).itemView.setOnClickListener { view: View? -> }
        } else {
            val theme = mData[position] as ThemeInfo
            val themeHolder = holder as ThemeViewHolder
            themeHolder.rootView.setOnClickListener(this)
            themeHolder.rootView.tag = position
            themeHolder.banner.setImageResource(
                    holder.rootView
                            .context
                            .resources
                            .getIdentifier(
                                    "theme_" + theme.themeId + "_preview",
                                    "drawable",
                                    holder.rootView.context.packageName))
            themeHolder.firstBinding = false
            themeHolder.name.text = theme.title
            themeHolder.description.visibility = View.VISIBLE
            themeHolder.description.text = theme.description
            if (theme.themeId == themeManager.currentThemeName) {
                themeHolder.actionBtn.text = "Current"
                themeHolder.actionBtn.setTextColor(-0xa1a1a2)
                themeHolder.actionBtn.setOnClickListener(null)
            } else {
                themeHolder.actionBtn.text = "Apply Now"
                themeHolder.actionBtn.setTextColor(-0xc5880b)
                themeHolder.actionBtn.setOnClickListener { v: View? ->
                    (holder.itemView.context as Activity).finish()
                    themeManager.applyTheme(theme)
                }
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            else -> {
            }
        }
    }

    internal class ThemeViewHolder(var rootView: View) : RecyclerView.ViewHolder(rootView) {
        var banner: ImageView
        var icon: ImageView
        var name: TextView
        var description: TextView
        var actionBtn: TextView
        var firstBinding = true

        init {
            banner = rootView.findViewById(R.id.theme_banner)
            icon = rootView.findViewById(R.id.theme_icon)
            name = rootView.findViewById(R.id.theme_name)
            description = rootView.findViewById(R.id.theme_description_short)
            actionBtn = rootView.findViewById(R.id.action_btn)
        }
    }

    private class MoreViewHolder internal constructor(container: View?) : RecyclerView.ViewHolder(container!!)
    companion object {
        private const val VIEW_TYPE_THEME = 0
        private const val VIEW_TYPE_MORE = 2
    }

    init {
        appComponent.inject(this)

        mData.addAll(ThemeProvider.allThemes)
        mData.add(1)
    }
}