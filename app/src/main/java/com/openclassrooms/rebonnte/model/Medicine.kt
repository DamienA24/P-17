package com.openclassrooms.rebonnte.model

import com.google.firebase.firestore.DocumentId

data class Medicine(
    @DocumentId val id: String = "",
    val name: String = "",
    val stock: Int = 0,
    val aisleId: String = "",
    val aisleName: String = ""
)
