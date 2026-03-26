package com.openclassrooms.rebonnte.model

import com.google.firebase.firestore.DocumentId

data class Aisle(
    @DocumentId val id: String = "",
    val name: String = ""
)
