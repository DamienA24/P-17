package com.openclassrooms.rebonnte.data.repository

import com.openclassrooms.rebonnte.model.History
import com.openclassrooms.rebonnte.model.Medicine
import kotlinx.coroutines.flow.Flow

interface MedicineRepository {
    fun getMedicines(): Flow<List<Medicine>>
    fun getMedicinesByAisle(aisleId: String): Flow<List<Medicine>>
    suspend fun addMedicine(medicine: Medicine)
    suspend fun updateStock(medicineId: String, aisleId: String, delta: Int, userEmail: String)
    suspend fun deleteMedicine(medicineId: String, aisleId: String)
    fun getHistory(medicineId: String, aisleId: String): Flow<List<History>>
}
