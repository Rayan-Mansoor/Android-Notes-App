package com.android.app.notesapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import java.util.Date
import java.util.concurrent.TimeUnit

class NotesViewAdapter(options: FirestoreRecyclerOptions<Note>) :
    FirestoreRecyclerAdapter<Note, NotesViewAdapter.NotesViewHolder>(options) {

    class NotesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.note_title_text)
        val content: TextView = itemView.findViewById(R.id.note_content_text)
        val timestamp: TextView = itemView.findViewById(R.id.note_timestamp_text)
        val colorIndicator: View = itemView.findViewById(R.id.color_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return NotesViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int, model: Note) {
        holder.title.text = model.title
        holder.content.text = model.content

        // Format timestamp to human-readable format
        val formattedDate = formatTimestamp(model.timestamp?.toDate())
        holder.timestamp.text = formattedDate

        // Optional: Set random colors for the color indicator
        // You can store color preference in your Note model and use it here
        val colors = listOf(
            R.color.accent_purple,
            R.color.accent_pink,
            R.color.accent_blue
        )
        val randomColor = colors.random()
        holder.colorIndicator.setBackgroundResource(randomColor)

        // Handle item click
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, AddNoteActivity::class.java)
            intent.putExtra("title", model.title)
            intent.putExtra("content", model.content)
            intent.putExtra("id", snapshots.getSnapshot(position).id)
            it.context.startActivity(intent)
        }
    }

    /**
     * Formats timestamp to a human-readable relative time format
     * Examples: "Just now", "5 minutes ago", "2 hours ago", "Yesterday", "3 days ago", "12/25/2024"
     */
    private fun formatTimestamp(date: Date?): String {
        if (date == null) return "Unknown"

        val now = Date()
        val diffInMillis = now.time - date.time

        return when {
            diffInMillis < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diffInMillis < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
                "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
            }
            diffInMillis < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
                "$hours ${if (hours == 1L) "hour" else "hours"} ago"
            }
            diffInMillis < TimeUnit.DAYS.toMillis(2) -> "Yesterday"
            diffInMillis < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)
                "$days ${if (days == 1L) "day" else "days"} ago"
            }
            diffInMillis < TimeUnit.DAYS.toMillis(30) -> {
                val weeks = TimeUnit.MILLISECONDS.toDays(diffInMillis) / 7
                "$weeks ${if (weeks == 1L) "week" else "weeks"} ago"
            }
            else -> {
                // For dates older than a month, show formatted date
                java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(date)
            }
        }
    }
}