package com.moez.QKSMS.feature.fonts

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.moez.QKSMS.R
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.dpToPx
import com.moez.QKSMS.common.util.extensions.resolveThemeColor
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.widget.QkTextView
import com.moez.QKSMS.feature.compose.BubbleUtils
import kotlinx.android.synthetic.main.message_list_item_in.view.*

class CustomizePreviewView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyle: Int = 0) : ConstraintLayout(context, attrs, defStyle) {
    private var messageOut: QkTextView? = null
    private var messageIn: QkTextView? = null
    override fun onFinishInflate() {
        super.onFinishInflate()
        messageIn = findViewById(R.id.message_in)
        messageOut = findViewById(R.id.message_out)
    }

    fun init(theme: Colors.Theme) {
        messageOut!!.setTextColor(context.resolveThemeColor(android.R.attr.textColorPrimary))
        messageOut!!.setBackgroundTint(context.resolveThemeColor(R.attr.inMessageBackground))
        messageIn!!.setBackgroundTint(theme.theme)
        messageIn!!.setTextColor(theme.textPrimary)

        refresh()
    }

    fun refresh() {
        messageOut!!.setBackgroundResource(BubbleUtils.getBubble(
                canGroupWithPrevious = false,
                canGroupWithNext = false,
                isMe = true))
        messageIn!!.setBackgroundResource(BubbleUtils.getBubble(
                canGroupWithPrevious = false,
                canGroupWithNext = false,
                isMe = false))
        if (BubbleUtils.hasCustomBubble()) {
            messageOut!!.backgroundTintList = null
            messageIn!!.backgroundTintList = null

            messageOut!!.setTextColor(BubbleUtils.getCustomBubbleInfo()!!.color)
            messageIn!!.setTextColor(BubbleUtils.getCustomBubbleInfo()!!.color)
        } else {
            messageOut!!.setPadding(12.dpToPx(context), 8.dpToPx(context), 12.dpToPx(context), 8.dpToPx(context))
            messageIn!!.setPadding(12.dpToPx(context), 8.dpToPx(context), 12.dpToPx(context), 8.dpToPx(context))
        }

    }
}