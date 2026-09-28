package com.ainotes.app.di

import android.content.Context
import androidx.room.Room
import com.ainotes.app.data.local.AiNotesDatabase
import com.ainotes.app.data.local.NoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AiNotesDatabase =
        Room.databaseBuilder(context, AiNotesDatabase::class.java, "ai_notes.db")
            .build()

    @Provides
    fun provideNoteDao(database: AiNotesDatabase): NoteDao = database.noteDao()
}
