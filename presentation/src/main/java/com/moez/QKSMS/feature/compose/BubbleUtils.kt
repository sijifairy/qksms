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
package com.moez.QKSMS.feature.compose

import androidx.annotation.DrawableRes
import com.moez.QKSMS.R
import com.moez.QKSMS.common.BaseApplication
import com.moez.QKSMS.common.util.Preferences
import com.moez.QKSMS.customize.BubbleInfo
import com.moez.QKSMS.model.Message
import java.util.concurrent.TimeUnit

object BubbleUtils {

    const val TIMESTAMP_THRESHOLD = 10

    const val DEFAULT_BUBBLE_INDEX = "default"

    const val PREF_KEY_BUBBLE_INDEX = "pref_key_bubble_index"

    fun canGroup(message: Message, other: Message?): Boolean {
        if (other == null) return false
        val diff = TimeUnit.MILLISECONDS.toMinutes(Math.abs(message.date - other.date))
        return message.compareSender(other) && diff < TIMESTAMP_THRESHOLD
    }

    fun getBubble(canGroupWithPrevious: Boolean, canGroupWithNext: Boolean, isMe: Boolean): Int {
        return if (!hasCustomBubble()) {
            when {
                !canGroupWithPrevious && canGroupWithNext -> if (isMe) R.drawable.message_out_first else R.drawable.message_in_first
                canGroupWithPrevious && canGroupWithNext -> if (isMe) R.drawable.message_out_middle else R.drawable.message_in_middle
                canGroupWithPrevious && !canGroupWithNext -> if (isMe) R.drawable.message_out_last else R.drawable.message_in_last
                else -> R.drawable.message_only
            }
        } else {
            getSelectedDrawable(Preferences.getDefault().getString(PREF_KEY_BUBBLE_INDEX, DEFAULT_BUBBLE_INDEX), !isMe)
        }
    }

    fun hasCustomBubble(): Boolean {
        return Preferences.getDefault().getString(PREF_KEY_BUBBLE_INDEX, DEFAULT_BUBBLE_INDEX) != DEFAULT_BUBBLE_INDEX
    }

    fun getCustomBubbleInfo(): BubbleInfo? {
        for (info in BUBBLE_INFOS) {
            if (info.id == Preferences.getDefault().getString(PREF_KEY_BUBBLE_INDEX, DEFAULT_BUBBLE_INDEX)) {
                return info
            }
        }

        return null
    }

    val BUBBLE_INFOS = arrayOf(
            BubbleInfo("travel", "Travel", 0xffffffff.toInt()),
            BubbleInfo("cheese", "Cheese", 0xffFA6120.toInt()),
            BubbleInfo("neon", "Neon", 0xffffffff.toInt()),
            BubbleInfo("vegan", "Vegan", 0xff1fa949.toInt()),
            BubbleInfo("quill", "Quill", 0xffffffff.toInt()),
            BubbleInfo("grin", "Grin", 0xff000000.toInt()),
            BubbleInfo("sea", "Sea", 0xffffffff.toInt()),
            BubbleInfo("glass", "Glass", 0xff000000.toInt()),
            BubbleInfo("christmas", "Christmas", 0xffffffff.toInt()),
            BubbleInfo("basketball", "Basketball", 0xffffffff.toInt()),
            BubbleInfo("nature", "Nature", 0xffffffff.toInt()),
            BubbleInfo("football", "Football", 0xffffffff.toInt()),
            BubbleInfo("love", "Love", 0xffffffff.toInt()),
            BubbleInfo("snow", "Snow", 0xff0B64B0.toInt()),
            BubbleInfo("cat", "Cat", 0xffffffff.toInt()),
            BubbleInfo("color_ball", "Color Ball", 0xffffffff.toInt()),
            BubbleInfo("elegant_rose", "Elegant Rose", 0xffffffff.toInt()),
            BubbleInfo("honey", "Honey Park", 0xffffffff.toInt()),
            BubbleInfo("rose_dream", "Rose Dream", 0xffffffff.toInt()),
            BubbleInfo("cat_rainbow", "Cat Rainbow", 0xffffffff.toInt())
    )

    @DrawableRes
    fun getSelectedDrawable(id: String, incoming: Boolean): Int {
        return if (incoming) {
            BaseApplication.getContext().resources.getIdentifier("bubble_" + id + "_incoming", "drawable", BaseApplication.getContext().packageName);
        } else {
            BaseApplication.getContext().resources.getIdentifier("bubble_" + id + "_outgoing", "drawable", BaseApplication.getContext().packageName);
        }
    }

    @DrawableRes
    fun getPreviewDrawable(id: String): Int {
        return BaseApplication.getContext().resources.getIdentifier("bubble_" + id + "_preview", "drawable", BaseApplication.getContext().packageName);
    }
}