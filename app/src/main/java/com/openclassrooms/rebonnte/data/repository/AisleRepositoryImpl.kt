package com.openclassrooms.rebonnte.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.openclassrooms.rebonnte.model.Aisle
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AisleRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : AisleRepository {

    private val aislesCollection = db.collection("aisles")

    override fun getAisles(): Flow<List<Aisle>> = callbackFlow {
        val listener = aislesCollection
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val aisles = snapshot?.documents
                    ?.mapNotNull { it.toObject(Aisle::class.java) }
                    ?: emptyList()
                trySend(aisles)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addAisle(aisle: Aisle) {
        aislesCollection.add(aisle).await()
    }

    override suspend fun deleteAisle(aisleId: String) {
        aislesCollection.document(aisleId).delete().await()
    }
}
