package com.android.app.notesapp

import com.google.firebase.Timestamp

enum class NoteCategory { PERSONAL, WORK, STUDY, IDEAS, OTHER }

data class Note(
    var id: String = "",                 // Firestore doc id (set client-side)
    var title: String = "",
    var content: String = "",
    var timestamp: Timestamp? = null,
    var category: String = NoteCategory.OTHER.name // stored as string in Firestore
)
