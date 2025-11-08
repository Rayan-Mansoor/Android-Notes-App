package com.android.app.notesapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import java.text.SimpleDateFormat

class NotesViewAdapter(options: FirestoreRecyclerOptions<Note>) : FirestoreRecyclerAdapter<Note,NotesViewAdapter.NotesViewHolder>(options){

    class NotesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title  = itemView.findViewById<TextView>(R.id.info_title)
        val discription = itemView.findViewById<TextView>(R.id.info_disc)
        val timestamp = itemView.findViewById<TextView>(R.id.info_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item,parent,false)
        return NotesViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int, model: Note) {
        holder.title.setText(model.title)
        holder.discription.setText(model.content)
        val formatedDate = SimpleDateFormat("MM/dd/yyyy").format(model.timestamp!!.toDate())
        holder.timestamp.setText(formatedDate)

        holder.itemView.setOnClickListener {
            val intent = Intent(it.context,AddNoteActivity::class.java)
            intent.putExtra("title",model.title)
            intent.putExtra("content",model.content)
            intent.putExtra("id",snapshots.getSnapshot(position).id)
            it.context.startActivity(intent)

        }
    }
}