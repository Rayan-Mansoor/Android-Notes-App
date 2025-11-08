package com.android.app.notesapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.app.notesapp.databinding.ActivityAddNoteBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

class AddNoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var document: DocumentReference
    private var isEditMode: Boolean = false
    private var recieveID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recieveTitle = intent.getStringExtra("title")
        val recieveContent = intent.getStringExtra("content")
        recieveID = intent.getStringExtra("id")

        if (recieveID != null) {
            isEditMode = true
            binding.titleET.setText(recieveTitle)
            binding.contentET.setText(recieveContent)
            binding.addEdit.text = "Update Note"
            binding.delNoteBtn.visibility = View.VISIBLE
        }

        binding.saveNoteBtn.setOnClickListener {
            if (binding.titleET.text.isNullOrEmpty()) {
                binding.titleET.error = "Title not given"
            } else {
                saveNote()
            }
        }

        binding.delNoteBtn.setOnClickListener { deleteNote() }

        binding.titleET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.titleET.error = if (binding.titleET.text.isNullOrEmpty()) "Title is required" else null
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun deleteNote() {
        val id = recieveID ?: return
        document = FirebaseRefs.getNoteDoc(id)
        document.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "Note deleted successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(applicationContext, "Error occurred while deleting the note", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveNote() {
        val title = binding.titleET.text.toString()
        val content = binding.contentET.text.toString()
        val note = Note(title, content, Timestamp.now())
        saveNoteToFirebase(note)
    }

    private fun saveNoteToFirebase(note: Note) {
        document = if (isEditMode) {
            FirebaseRefs.getNoteDoc(recieveID!!)
        } else {
            FirebaseRefs.addNoteDoc()
        }

        document.set(note).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "Note saved successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(applicationContext, "Note was not saved", Toast.LENGTH_SHORT).show()
            }
        }
    }
}