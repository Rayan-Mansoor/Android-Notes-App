package com.android.app.notesapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.app.notesapp.databinding.ActivityMainBinding
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var notesAdapter: NotesViewAdapter

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.my_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            FirebaseRefs.auth.signOut()
            startActivity(Intent(this, LoginAccount::class.java))
            finish()
            return true
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseRefs.uid == null) {
            startActivity(Intent(this, LoginAccount::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setTheme(R.style.Theme_NotesApp)
        setContentView(binding.root)

        binding.newNote.setOnClickListener {
            startActivity(Intent(this, AddNoteActivity::class.java))
        }

        val options = FirestoreRecyclerOptions.Builder<Note>()
            .setQuery(FirebaseRefs.userNotesDesc(), Note::class.java)
            .build()

        notesAdapter = NotesViewAdapter(options)
        binding.notesView.layoutManager = LinearLayoutManager(this)
        binding.notesView.adapter = notesAdapter
        binding.notesView.addItemDecoration(NotesItemDecorator(20))
    }

    override fun onStart() {
        super.onStart()
        notesAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        notesAdapter.stopListening()
    }

    override fun onResume() {
        super.onResume()
        notesAdapter.notifyDataSetChanged()
    }
}