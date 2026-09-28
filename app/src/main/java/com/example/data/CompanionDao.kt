package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Unified Companion DAO inheriting all MediaItemDao capabilities
 * along with sovereign Crew Status management.
 */
@Dao
interface CompanionDao : MediaItemDao {
    // --- Crew Status Operations ---
    @Query("SELECT * FROM crew_status WHERE id = 1")
    fun getCrewStatus(): Flow<CrewStatus?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCrewStatus(status: CrewStatus)
}
