package com.moez.QKSMS.common.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer

//open class QkViewHolder(var containerView: View) : RecyclerView.ViewHolder(containerView)

open class QkViewHolder(view: View) : RecyclerView.ViewHolder(view), LayoutContainer {
    override val containerView: View = view
}

