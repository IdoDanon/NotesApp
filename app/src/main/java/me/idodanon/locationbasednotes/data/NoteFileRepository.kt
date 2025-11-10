package me.idodanon.locationbasednotes.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class NoteFileRepository(
    private val ctx: Context
) {
    private fun getFileForUser(email: String): File =
        File(ctx.filesDir, "notes_${email.replace("@", "_")}.json")

    suspend fun loadNotes(email: String): List<Note> = withContext(Dispatchers.IO) {
        val file = getFileForUser(email)
        if (!file.exists()) return@withContext emptyList()
        val text = file.readText()
        return@withContext Json.decodeFromString(text)
    }

    suspend fun saveNotes(email: String, notes: List<Note>) = withContext(Dispatchers.IO) {
        val file = getFileForUser(email)
        file.writeText(Json.encodeToString(notes))
    }

    suspend fun addOrUpdateNote(email: String, note: Note) {
        val notes = loadNotes(email).toMutableList()
        val index = notes.indexOfFirst { it.id == note.id }
        if (index >= 0) notes[index] = note else notes.add(note)
        saveNotes(email, notes)
    }

    suspend fun deleteNote(email: String, noteId: String) {
        val notes = loadNotes(email).filter { it.id != noteId }
        saveNotes(email, notes)
    }
}