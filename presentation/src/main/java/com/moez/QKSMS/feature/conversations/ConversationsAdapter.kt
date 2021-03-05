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
package com.moez.QKSMS.feature.conversations

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.moez.QKSMS.R
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.ad.nativead.NmNativeAd
import com.moez.QKSMS.common.ad.nativead.NmNativeAdContainer
import com.moez.QKSMS.common.ad.nativead.NmNativeAdIconView
import com.moez.QKSMS.common.base.QkRealmAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.DateFormatter
import com.moez.QKSMS.common.util.RemoteConfig
import com.moez.QKSMS.common.util.extensions.resolveThemeColor
import com.moez.QKSMS.common.util.extensions.setVisible
import com.moez.QKSMS.feature.customize.ThemeManager
import com.moez.QKSMS.model.Conversation
import kotlinx.android.synthetic.main.conversation_list_ad_container.*
import kotlinx.android.synthetic.main.conversation_list_item.view.*
import javax.inject.Inject

class ConversationsAdapter @Inject constructor(
        private val colors: Colors,
        private val context: Context,
        private val dateFormatter: DateFormatter,
        private val navigator: Navigator,
        private val themeManager: ThemeManager
) : QkRealmAdapter<Conversation>() {

    init {
        setHasStableIds(true)
    }

    lateinit var ad: NmNativeAd
    var hasAd: Boolean = false

    fun onAdLoaded(unifiedNativeAd: NmNativeAd) {
        hasAd = true
        ad = unifiedNativeAd
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        if (viewType == 0 || viewType == 1) {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.conversation_list_item, parent, false)

            if (viewType == 1) {
                val textColorPrimary = parent.context.resolveThemeColor(android.R.attr.textColorPrimary)

                view.title.setTypeface(view.title.typeface, Typeface.BOLD)

                view.snippet.setTypeface(view.snippet.typeface, Typeface.BOLD)
                view.snippet.setTextColor(textColorPrimary)
                view.snippet.maxLines = 5

                view.unread.isVisible = true
//                view.unread.setTint(colors.theme().theme)

                view.date.setTypeface(view.date.typeface, Typeface.BOLD)
                view.date.setTextColor(textColorPrimary)
            }

            return QkViewHolder(view).apply {
                view.setOnClickListener {
                    val conversation = getItem(if (hasAd) adapterPosition - 1 else adapterPosition)
                            ?: return@setOnClickListener
                    when (toggleSelection(conversation.id, false)) {
                        true -> view.isActivated = isSelected(conversation.id)
                        false -> navigator.showConversation(conversation.id)
                    }
                }
                view.setOnLongClickListener {
                    val conversation = getItem(if (hasAd) adapterPosition - 1 else adapterPosition)
                            ?: return@setOnLongClickListener true
                    toggleSelection(conversation.id)
                    view.isActivated = isSelected(conversation.id)
                    true
                }
            }
        } else {
            return QkViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.conversation_list_ad_container, parent, false))
        }
    }

    override fun onBindViewHolder(viewHolder: QkViewHolder, position: Int) {
        if (hasAd && position == 0) {
            val adContainerLayout = FrameLayout(viewHolder.itemView.context)
            val adContent = LayoutInflater.from(viewHolder.itemView.context)
                    .inflate(R.layout.conversation_list_ad, adContainerLayout, false) as ViewGroup
            val adContainer: NmNativeAdContainer = ad.container
            adContainer.setContent(adContainerLayout, adContent)
            val icon: NmNativeAdIconView = adContainerLayout.findViewById(R.id.avatars)
            adContainer.setIconView(icon)

            val title: TextView = adContainerLayout.findViewById(R.id.title)
            title.setTextColor(adContainerLayout.context.resolveThemeColor(R.attr.listItemTitleColor, adContainerLayout.context.resolveThemeColor(android.R.attr.textColorPrimary)))
            adContainer.setTitle(title)

            val description: TextView = adContainerLayout.findViewById(R.id.snippet)
            description.setTextColor(adContainerLayout.context.resolveThemeColor(R.attr.listItemContentColor, adContainerLayout.context.resolveThemeColor(android.R.attr.textColorSecondary)))
            adContainer.setBody(description)

            val actionBtn: TextView = adContainerLayout.findViewById(R.id.cta)
            actionBtn.setTextColor(adContainerLayout.context.resolveThemeColor(R.attr.listItemTitleColor, adContainerLayout.context.resolveThemeColor(android.R.attr.textColorPrimary)))
            adContainer.setCTA(actionBtn)

            adContainer.setAdChoiceView(adContainerLayout.findViewById(R.id.ad_choice))
            adContainer.fillNativeAd(ad)
            if (RemoteConfig.instance.getBoolean("AdHomeNativeBgShow")) {
//                adContent.setBackgroundColor()
            }
//            val ivAdPreview = adContainerLayout.findViewById<ImageView>(R.id.ic_ad)
//            ivAdPreview.drawable.setColorFilter(ConversationColors.get().getListTimeColor(), PorterDuff.Mode.SRC_ATOP)


            viewHolder.container.removeAllViews()
            viewHolder.container.addView(adContainerLayout)
            viewHolder.itemView.tag = 2
        } else {
            val conversation = getItem(position - if (hasAd) 1 else 0) ?: return
            val view = viewHolder.containerView
            view.title.setTextColor(view.context.resolveThemeColor(R.attr.listItemTitleColor, view.context.resolveThemeColor(android.R.attr.textColorPrimary)))
            view.snippet.setTextColor(view.context.resolveThemeColor(R.attr.listItemContentColor, view.context.resolveThemeColor(android.R.attr.textColorSecondary)))
            view.date.setTextColor(view.context.resolveThemeColor(R.attr.listItemTimeColor, view.context.resolveThemeColor(android.R.attr.textColorTertiary)))


            view.isActivated = isSelected(conversation.id)

            if (themeManager.isThemeApplied && themeManager.currentTheme?.avatarsList?.size ?: 0 > 0) {
                view.avatars.setVisible(false)
                view.theme_avatars.setVisible(true)
                view.avatars_t.setImageResource(themeManager.currentTheme!!.avatarsList!![position % themeManager.currentTheme!!.avatarsList!!.size])
                view.alphabet.text = conversation.getTitle().let {
                    if (it.isNotEmpty())
                        it.subSequence(0, 1)
                    else
                        ""
                }
            } else {
                view.avatars.setVisible(true)
                view.theme_avatars.setVisible(false)
                view.avatars.contacts = conversation.recipients
            }

            view.title.collapseEnabled = conversation.recipients.size > 1
            view.title.text = conversation.getTitle()
            view.date.text = dateFormatter.getConversationTimestamp(conversation.date)
            view.snippet.text = when (conversation.me) {
                true -> context.getString(R.string.main_sender_you, conversation.snippet)
                false -> conversation.snippet
            }
            view.pinned.isVisible = conversation.pinned
            viewHolder.itemView.tag = 0
        }
    }

    override fun getItemId(index: Int): Long {
        if (hasAd) {
            if (index == 0) return 32323411118989223L
            return getItem(index - 1)!!.id
        } else {
            return getItem(index)!!.id
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasAd) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        if (hasAd) {
            if (position == 0) return 2
            return if (getItem(position - 1)?.read == true) 0 else 1
        } else {
            return if (getItem(position)?.read == true) 0 else 1
        }
    }
}