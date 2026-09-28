package com.ainotes.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [NoteEntity::class], version = 1, exportSchema = true)
abstract class AiNotesDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
