package com.android.app.notesapp

import com.google.firebase.Timestamp

data class Note(
    var title : String? = null,
    var content : String? = null,
    var timestamp: Timestamp? = null
)
