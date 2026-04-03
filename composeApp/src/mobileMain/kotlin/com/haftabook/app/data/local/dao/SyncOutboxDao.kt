package com.haftabook.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.haftabook.app.data.local.entity.SyncOutboxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncOutboxDao {
    @Insert
    suspend fun insert(row: SyncOutboxEntity): Long

    @Query("SELECT COUNT(*) FROM sync_outbox")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT * FROM sync_outbox ORDER BY id ASC LIMIT :limit")
    suspend fun peek(limit: Int = 50): List<SyncOutboxEntity>

    /** Pending cloud deletes (not yet successfully pushed). Used to avoid RTDB merge undoing local deletes. */
    @Query("SELECT * FROM sync_outbox WHERE operation = 'DELETE' ORDER BY id ASC")
    suspend fun getPendingDeletes(): List<SyncOutboxEntity>

    @Query("DELETE FROM sync_outbox WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE sync_outbox SET attempt_count = attempt_count + 1, last_error = :error WHERE id = :id")
    suspend fun markFailure(id: Long, error: String)
}
