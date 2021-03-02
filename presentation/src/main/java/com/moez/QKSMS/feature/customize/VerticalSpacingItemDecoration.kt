package com.moez.QKSMS.feature.customize

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class VerticalSpacingItemDecoration(private val mVerticalSpaceHeight: Int) : ItemDecoration() {
    override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.top = mVerticalSpaceHeight
    }

}