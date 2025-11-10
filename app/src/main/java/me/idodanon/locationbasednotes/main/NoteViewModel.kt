package me.idodanon.locationbasednotes.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.idodanon.locationbasednotes.data.Note
import me.idodanon.locationbasednotes.data.NoteFileRepository

class NoteViewModel(
    private val repository: NoteFileRepository
) : ViewModel() {
    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _isMapMode = MutableStateFlow(false)
    val isMapMode: StateFlow<Boolean> = _isMapMode.asStateFlow()

    fun setIsMapMode(mode: Boolean) {
        _isMapMode.value = mode
    }

    fun setUser(email: String) {
        _userEmail.value = email
        viewModelScope.launch {
            email.let { e ->
                val loadedNotes = repository.loadNotes(e)
                _notes.value = loadedNotes
            }
        }
    }

    fun saveNote(note: Note) {
        viewModelScope.launch {
            val email = _userEmail.value ?: return@launch
            repository.addOrUpdateNote(email, note)
            _notes.value = repository.loadNotes(email)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            val email = _userEmail.value ?: return@launch
            repository.deleteNote(email, note.id)
            _notes.value = repository.loadNotes(email)
        }
    }

    suspend fun getNoteById(id: String): Note? {
        val email = _userEmail.value ?: return null
        return repository.loadNotes(email).find { it.id == id }
    }
}
