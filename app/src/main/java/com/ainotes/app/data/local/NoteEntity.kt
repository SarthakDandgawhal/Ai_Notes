package com.ainotes.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val isArchived: Boolean = false,
    val isTrashed: Boolean = false,
)
