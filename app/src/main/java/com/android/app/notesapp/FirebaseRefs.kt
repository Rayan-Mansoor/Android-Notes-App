package com.android.app.notesapp

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

object FirebaseRefs {
    // Singletons
    val auth: FirebaseAuth by lazy { Firebase.auth }
    val fs: FirebaseFirestore by lazy { Firebase.firestore }

    // Handy getters
    val uid: String? get() = auth.currentUser?.uid
    fun requireUid(): String = uid ?: error("Not signed in")

    // Firestore paths
    private val notesRoot get() = fs.collection("Notes")
    fun userDoc(uid: String = requireUid()) = notesRoot.document(uid)
    fun userNotes(uid: String = requireUid()) = userDoc(uid).collection("My Notes")

    // Common queries
    fun userNotesDesc(uid: String = requireUid()): Query =
        userNotes(uid).orderBy("timestamp", Query.Direction.DESCENDING)

    // Convenient docs
    fun getNoteDoc(id: String, uid: String = requireUid()) = userNotes(uid).document(id)
    fun addNoteDoc(uid: String = requireUid()) = userNotes(uid).document()
}
