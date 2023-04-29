package com.android.app.notesapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.app.notesapp.databinding.ActivityAddNoteBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class AddNote : AppCompatActivity() {
    private lateinit var binding : ActivityAddNoteBinding
    private lateinit var document : DocumentReference
    private var isEditMode : Boolean = false
    private var recieveID : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recieveTitle = intent.getStringExtra("title")
        val recieveContent = intent.getStringExtra("content")
        recieveID = intent.getStringExtra("id")


        if (recieveID != null){
            isEditMode = true
            binding.titleET.setText(recieveTitle)
            binding.contentET.setText(recieveContent)
            binding.addEdit.setText("Update Note")
            binding.delNoteBtn.visibility = View.VISIBLE
        }


        binding.saveNoteBtn.setOnClickListener {
            if (binding.titleET.text.isEmpty() || binding.titleET.text == null){
                binding.titleET.error = "Title not given"
            }else{
                saveNote()
            }
        }

        binding.delNoteBtn.setOnClickListener {
            deleteNote()
        }

        binding.titleET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.titleET.text.isEmpty() || binding.titleET.text == null){
                    binding.titleET.error = "Title is required"
                }
                else{
                    binding.titleET.error = null
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        })
    }

    private fun deleteNote() {
        document = FirebaseFirestore.getInstance().collection("Notes").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("My Notes").document(recieveID!!)
        document.delete().addOnCompleteListener { task ->
            if (task.isSuccessful){
                Toast.makeText(applicationContext,"Note deleted successfully",Toast.LENGTH_SHORT).show()
                val intent = Intent(this,MainActivity::class.java)
                //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            else{
                Toast.makeText(applicationContext,"Error occurred while deleting the note",Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun saveNote() {
        val title = binding.titleET.text.toString()
        val content = binding.contentET.text.toString()
        val note = Note(title,content, Timestamp.now())
        saveNoteToFirebase(note)


    }

    private fun saveNoteToFirebase(note: Note){
        if (isEditMode){
            document = FirebaseFirestore.getInstance().collection("Notes").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("My Notes").document(recieveID!!)
        }
        else{
            document = FirebaseFirestore.getInstance().collection("Notes").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("My Notes").document()
        }

            Log.d("AddNote",FirebaseAuth.getInstance().currentUser!!.uid)
            Log.d("AddNote",FirebaseFirestore.getInstance().toString())

        document.set(note).addOnCompleteListener { task->
            if (task.isSuccessful){
                Toast.makeText(applicationContext,"Note saved successfully",Toast.LENGTH_SHORT).show()
                val intent = Intent(this,MainActivity::class.java)
                //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            else{
                Toast.makeText(applicationContext,"Note was not saved",Toast.LENGTH_SHORT).show()
            }
        }
    }
}