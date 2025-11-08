package com.android.app.notesapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class NotesViewAdapter : ListAdapter<Note, NotesViewAdapter.VH>(Diff) {

    /** Notify the Activity when selection state changes (to update the selection bar). */
    var onSelectionChanged: (() -> Unit)? = null

    /** Full unfiltered list to support on-device search. */
    private val allNotes = mutableListOf<Note>()
    private var currentQuery: String = ""

    /** Multi-select state. */
    private val selected = linkedSetOf<String>()
    private var selectionMode = false

    object Diff : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Note, newItem: Note) = oldItem == newItem
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.note_title_text)
        private val content: TextView = itemView.findViewById(R.id.note_content_text)
        private val timestamp: TextView = itemView.findViewById(R.id.note_timestamp_text)
        private val colorIndicator: View = itemView.findViewById(R.id.color_indicator)
        private val checkOverlay: ImageView? = itemView.findViewById(R.id.check_overlay)
        private val selectionScrim: View? = itemView.findViewById(R.id.selection_scrim)

        fun bind(n: Note) {
            title.text = n.title.orEmpty()
            content.text = n.content.orEmpty()
            timestamp.text = formatTimestamp(n.timestamp?.toDate())

            // Category → gradient bar
            colorIndicator.setBackgroundResource(categoryDrawable(n.category))

            // Selection visuals
            val isSelected = selected.contains(n.id)
            checkOverlay?.visibility = if (isSelected) View.VISIBLE else View.GONE
            selectionScrim?.visibility = if (isSelected) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                if (selectionMode) {
                    toggleSelection(n.id)
                } else {
                    // Open note for edit
                    val ctx = it.context
                    val i = Intent(ctx, AddNoteActivity::class.java)
                    i.putExtra("id", n.id)
                    i.putExtra("title", n.title)
                    i.putExtra("content", n.content)
                    i.putExtra("category", n.category)
                    ctx.startActivity(i)
                }
            }
            itemView.setOnLongClickListener {
                if (!selectionMode) enterSelectionMode()
                toggleSelection(n.id)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    // ---------------------------
    // Public API for Activity
    // ---------------------------

    /** Provide fresh data (e.g., from Firestore snapshot listener). */
    fun setData(newData: List<Note>) {
        allNotes.clear()
        allNotes.addAll(newData)
        applyFilter(currentQuery)
    }

    /** Apply local search (title/content). */
    fun applyFilter(query: String) {
        currentQuery = query
        val q = query.trim()
        val filtered = if (q.isEmpty()) {
            allNotes
        } else {
            allNotes.filter {
                (it.title ?: "").contains(q, ignoreCase = true) ||
                        (it.content ?: "").contains(q, ignoreCase = true)
            }
        }
        submitList(filtered)
        notifySelectionChanged()
    }

    /** Selection helpers */
    fun enterSelectionMode() { selectionMode = true; notifySelectionChanged() }
    fun exitSelectionMode() { selectionMode = false; selected.clear(); notifySelectionChanged() }
    fun inSelectionMode() = selectionMode
    fun toggleSelection(id: String) {
        if (selected.contains(id)) selected.remove(id) else selected.add(id)
        notifySelectionChanged()
    }
    fun selectedIds(): List<String> = selected.toList()
    fun selectedCount(): Int = selected.size

    private fun notifySelectionChanged() {
        // Simple approach: refresh; for very large lists, consider payload updates.
        notifyDataSetChanged()
        onSelectionChanged?.invoke()
    }

    // ---------------------------
    // Utils
    // ---------------------------

    private fun categoryDrawable(catRaw: String?): Int {
        val cat = (catRaw ?: "").uppercase(Locale.ROOT)
        return when (cat) {
            "PERSONAL" -> R.drawable.gradient_cat_personal
            "WORK"     -> R.drawable.gradient_cat_work
            "STUDY"    -> R.drawable.gradient_cat_study
            "IDEAS"    -> R.drawable.gradient_cat_ideas
            "OTHER", "" -> R.drawable.gradient_cat_other
            else       -> R.drawable.gradient_cat_other
        }
    }

    /**
     * Formats timestamp to a human-readable relative time format
     * Examples: "Just now", "5 minutes ago", "2 hours ago", "Yesterday", "3 days ago", "Dec 25, 2024"
     */
    private fun formatTimestamp(date: Date?): String {
        if (date == null) return "Unknown"
        val now = Date()
        val diff = now.time - date.time

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val m = TimeUnit.MILLISECONDS.toMinutes(diff)
                "$m ${if (m == 1L) "minute" else "minutes"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val h = TimeUnit.MILLISECONDS.toHours(diff)
                "$h ${if (h == 1L) "hour" else "hours"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(2) -> "Yesterday"
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val d = TimeUnit.MILLISECONDS.toDays(diff)
                "$d ${if (d == 1L) "day" else "days"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(30) -> {
                val w = TimeUnit.MILLISECONDS.toDays(diff) / 7
                "$w ${if (w == 1L) "week" else "weeks"} ago"
            }
            else -> java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(date)
        }
    }
}