package com.openclassrooms.rebonnte.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class History(
    @DocumentId val id: String = "",
    val medicineName: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val date: Timestamp = Timestamp.now(),
    val details: String = "",
    val stockBefore: Int = 0,
    val stockAfter: Int = 0
)
