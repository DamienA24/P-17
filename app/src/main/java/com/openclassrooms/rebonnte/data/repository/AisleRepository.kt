package com.openclassrooms.rebonnte.data.repository

import com.openclassrooms.rebonnte.model.Aisle
import kotlinx.coroutines.flow.Flow

interface AisleRepository {
    fun getAisles(): Flow<List<Aisle>>
    suspend fun addAisle(aisle: Aisle)
    suspend fun deleteAisle(aisleId: String)
}
