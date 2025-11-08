package com.android.app.notesapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.app.notesapp.databinding.ActivityAddNoteBinding
import com.google.firebase.Timestamp

class AddNoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddNoteBinding

    private var isEditMode = false
    private var receiveID: String? = null
    private var selectedCategory: NoteCategory = NoteCategory.OTHER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // incoming extras (for edit)
        val titleIn = intent.getStringExtra("title")
        val contentIn = intent.getStringExtra("content")
        val catIn = intent.getStringExtra("category")
        receiveID = intent.getStringExtra("id")

        if (!catIn.isNullOrBlank()) {
            selectedCategory = runCatching { NoteCategory.valueOf(catIn) }
                .getOrDefault(NoteCategory.OTHER)
        }

        if (receiveID != null) {
            isEditMode = true
            binding.titleET.setText(titleIn)
            binding.contentET.setText(contentIn)
            binding.addEdit.text = "Update Note"
        } else {
            binding.addEdit.text = "New Note"
        }

        // Color picker highlight
        reflectCategoryUi()

        // Clicks for color dots
        binding.colorPersonal.setOnClickListener { setCategory(NoteCategory.PERSONAL) }
        binding.colorWork.setOnClickListener     { setCategory(NoteCategory.WORK) }
        binding.colorStudy.setOnClickListener    { setCategory(NoteCategory.STUDY) }
        binding.colorIdeas.setOnClickListener    { setCategory(NoteCategory.IDEAS) }
        binding.colorOther.setOnClickListener    { setCategory(NoteCategory.OTHER) }

        binding.saveNoteBtn.setOnClickListener {
            if (binding.titleET.text.isNullOrEmpty()) {
                binding.titleET.error = "Title is required"
            } else {
                saveNote()
            }
        }

        binding.cancelBtn.setOnClickListener { finish() }

        binding.titleET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.titleET.error = if (binding.titleET.text.isNullOrEmpty()) "Title is required" else null
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun setCategory(cat: NoteCategory) {
        selectedCategory = cat
        reflectCategoryUi()
    }

    private fun reflectCategoryUi() {
        // simple ring highlight – show only on selected
        val views = listOf(
            binding.colorPersonal to NoteCategory.PERSONAL,
            binding.colorWork to NoteCategory.WORK,
            binding.colorStudy to NoteCategory.STUDY,
            binding.colorIdeas to NoteCategory.IDEAS,
            binding.colorOther to NoteCategory.OTHER
        )
        views.forEach { (v, cat) ->
            v.foreground = if (cat == selectedCategory)
                getDrawable(R.drawable.color_selected_ring) else null
        }
    }

    private fun saveNote() {
        val note = Note(
            title = binding.titleET.text.toString(),
            content = binding.contentET.text.toString(),
            timestamp = Timestamp.now(),
            category = selectedCategory.name
        )

        val docRef = if (isEditMode) {
            FirebaseRefs.getNoteDoc(receiveID!!)
        } else {
            FirebaseRefs.addNoteDoc()
        }

        docRef.set(note).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "Note saved", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(applicationContext, "Failed to save", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
