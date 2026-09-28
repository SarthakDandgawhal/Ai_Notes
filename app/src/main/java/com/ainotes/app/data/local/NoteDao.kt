package com.ainotes.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query(
        "SELECT * FROM notes WHERE isTrashed = 0 AND isArchived = 0 " +
            "AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') " +
            "ORDER BY isPinned DESC, updatedAt DESC",
    )
    fun observeActive(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): NoteEntity?

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)
}
