package com.android.app.notesapp

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.app.notesapp.databinding.ActivityMainBinding
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.toObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NotesViewAdapter   // <-- use NotesViewAdapter

    private val allNotes = mutableListOf<Note>()
    private var queryText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseRefs.uid == null) {
            startActivity(Intent(this, LoginAccount::class.java))
            finish(); return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logoutBtn.setOnClickListener {
            FirebaseRefs.auth.signOut()
            startActivity(Intent(this, LoginAccount::class.java))
            finish()
        }

        binding.newNote.setOnClickListener {
            startActivity(Intent(this, AddNoteActivity::class.java))
        }

        // RecyclerView + Adapter
        adapter = NotesViewAdapter().apply {
            onSelectionChanged = { updateSelectionUi() }  // <- get notified by adapter
        }
        binding.notesView.layoutManager = LinearLayoutManager(this)
        binding.notesView.adapter = adapter
        binding.notesView.addItemDecoration(NotesItemDecorator(20))

        // Search: local filter
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                queryText = s?.toString().orEmpty()
                submitFiltered()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Selection toolbar actions
        binding.deleteSelectedBtn.setOnClickListener { deleteSelected() }
        binding.cancelSelectionBtn.setOnClickListener {
            adapter.exitSelectionMode()
            updateSelectionUi()
        }

        // Firestore listener -> keep allNotes fresh; filter locally
        FirebaseRefs.userNotesDesc().addSnapshotListener { snap, err ->
            if (err != null || snap == null) return@addSnapshotListener
            for (dc in snap.documentChanges) {
                val doc = dc.document
                val note = doc.toObject<Note>().copy(id = doc.id)
                when (dc.type) {
                    com.google.firebase.firestore.DocumentChange.Type.ADDED -> allNotes.add(note)
                    com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                        val idx = allNotes.indexOfFirst { it.id == note.id }
                        if (idx >= 0) allNotes[idx] = note
                    }
                    com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                        val idx = allNotes.indexOfFirst { it.id == note.id }
                        if (idx >= 0) allNotes.removeAt(idx)
                    }
                }
            }
            submitFiltered()
        }
    }

    private fun submitFiltered() {
        val q = queryText.trim()
        val filtered = if (q.isEmpty()) {
            allNotes.toList() // Create NEW list instance
        } else {
            allNotes.filter {
                (it.title ?: "").contains(q, true) || (it.content ?: "").contains(q, true)
            }
        }
        adapter.submitList(filtered)
        updateSelectionUi()
    }

    private fun deleteSelected() {
        val ids = adapter.selectedIds()
        if (ids.isEmpty()) return
        val batch = FirebaseRefs.fs.batch()
        ids.forEach { id -> batch.delete(FirebaseRefs.getNoteDoc(id)) }
        batch.commit().addOnCompleteListener {
            adapter.exitSelectionMode()
            updateSelectionUi()
        }
    }

    private fun updateSelectionUi() {
        val count = adapter.selectedCount()
        if (adapter.inSelectionMode() && count > 0) {
            binding.selectionCount.text = "$count selected"
            showSelectionBar(true)

            // Hide FAB with animation
            binding.newNote.hide()

            // Fade out blob2
            binding.blob2.animate()
                .alpha(0f)
                .setDuration(120)
                .withEndAction {
                    binding.blob2.visibility = View.GONE
                }
                .start()
        } else {
            showSelectionBar(false)

            // Show FAB with animation
            binding.newNote.show()

            // Fade in blob2
            binding.blob2.visibility = View.VISIBLE
            binding.blob2.animate()
                .alpha(0.12f) // Match the original alpha from XML
                .setDuration(120)
                .start()
        }
    }

    private fun showSelectionBar(show: Boolean) {
        val target = if (show) View.VISIBLE else View.GONE
        if (binding.selectionToolbar.visibility != target) {
            binding.selectionToolbar.visibility = target
            val from = if (show) binding.selectionToolbar.height.toFloat() else 0f
            val to = if (show) 0f else binding.selectionToolbar.height.toFloat()
            ObjectAnimator.ofFloat(binding.selectionToolbar, "translationY", from, to)
                .setDuration(120).start()
        }
    }
}