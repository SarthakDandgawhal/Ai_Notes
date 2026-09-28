package com.ainotes.app.data

import com.ainotes.app.data.local.NoteDao
import com.ainotes.app.data.local.NoteEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class NoteRepository @Inject constructor(
    private val dao: NoteDao,
) {
    fun observe(query: String): Flow<List<NoteEntity>> = dao.observeActive(query)

    suspend fun get(id: Long): NoteEntity? = dao.getById(id)

    suspend fun save(note: NoteEntity): Long {
        return if (note.id == 0L) dao.insert(note) else {
            dao.update(note)
            note.id
        }
    }
}
