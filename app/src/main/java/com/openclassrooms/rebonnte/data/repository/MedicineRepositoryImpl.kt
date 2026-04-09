package com.openclassrooms.rebonnte.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.openclassrooms.rebonnte.model.History
import com.openclassrooms.rebonnte.model.Medicine
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicineRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : MedicineRepository {

    private fun medicinesRef(aisleId: String) =
        db.collection("aisles").document(aisleId).collection("medicines")

    private fun historyRef(aisleId: String, medicineId: String) =
        medicinesRef(aisleId).document(medicineId).collection("history")

    override fun getMedicines(): Flow<List<Medicine>> = callbackFlow {
        val listener = db.collectionGroup("medicines")
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val medicines = snapshot?.documents
                    ?.mapNotNull { it.toObject(Medicine::class.java) }
                    ?: emptyList()
                trySend(medicines)
            }
        awaitClose { listener.remove() }
    }

    override fun getMedicinesByAisle(aisleId: String): Flow<List<Medicine>> = callbackFlow {
        val listener = medicinesRef(aisleId)
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val medicines = snapshot?.documents
                    ?.mapNotNull { it.toObject(Medicine::class.java) }
                    ?: emptyList()
                trySend(medicines)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addMedicine(medicine: Medicine) {
        medicinesRef(medicine.aisleId).add(medicine).await()
    }

    override suspend fun updateStock(medicineId: String, aisleId: String, delta: Int, userEmail: String) {
        val medicineDocRef = medicinesRef(aisleId).document(medicineId)
        db.runTransaction { transaction ->
            val snap = transaction.get(medicineDocRef)
            val currentStock = snap.getLong("stock")?.toInt() ?: 0
            val newStock = currentStock + delta
            transaction.update(medicineDocRef, "stock", newStock)
            val historyDocRef = historyRef(aisleId, medicineId).document()
            transaction.set(
                historyDocRef,
                History(
                    medicineName = snap.getString("name") ?: "",
                    userEmail = userEmail,
                    date = Timestamp.now(),
                    details = if (delta > 0) "+$delta" else "$delta",
                    stockBefore = currentStock,
                    stockAfter = newStock
                )
            )
        }.await()
    }

    override suspend fun deleteMedicine(medicineId: String, aisleId: String) {
        medicinesRef(aisleId).document(medicineId).delete().await()
    }

    override fun getHistory(medicineId: String, aisleId: String): Flow<List<History>> = callbackFlow {
        val listener = historyRef(aisleId, medicineId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val history = snapshot?.documents
                    ?.mapNotNull { it.toObject(History::class.java) }
                    ?: emptyList()
                trySend(history)
            }
        awaitClose { listener.remove() }
    }
}
