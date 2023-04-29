package com.android.app.notesapp

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class NotesItemDecorator(val spacing: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.bottom = spacing
    }
}