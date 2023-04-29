package com.android.app.notesapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.app.notesapp.databinding.ActivityMainBinding
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var notesAdapter : NotesViewAdapter

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.my_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout){
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this,LoginAccount::class.java))
            finish()
            return true
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null){
            startActivity(Intent(this,LoginAccount::class.java))
            finish()

        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setTheme(R.style.Theme_NotesApp)
        setContentView(binding.root)


        binding.newNote.setOnClickListener {
            startActivity(Intent(this,AddNote::class.java))

        }

        if (firebaseUser != null){
            val query : Query = FirebaseFirestore.getInstance().collection("Notes").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("My Notes").orderBy("timestamp",Query.Direction.DESCENDING)

            val options = FirestoreRecyclerOptions.Builder<Note>().setQuery(query,Note::class.java).build()

            notesAdapter = NotesViewAdapter(options)


            binding.notesView.layoutManager = LinearLayoutManager(this)


            binding.notesView.adapter = notesAdapter


            binding.notesView.addItemDecoration(NotesItemDecorator(20))
        }


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