package com.example.todolistapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolistapp.model.NoteModel
import com.example.todolistapp.repo.NoteRepo
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NoteViewModel(
    private val repo: NoteRepo = NoteRepo(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _notes = MutableStateFlow<List<NoteModel>>(emptyList())
    val notes: StateFlow<List<NoteModel>> = _notes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadNotes()
    }

    fun loadNotes() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _notes.value = repo.getNotes(userId)
            _isLoading.value = false
        }
    }

    fun addNote(title: String, content: String, color: Int = 0) {
        val userId = auth.currentUser?.uid ?: return
        val newNote = NoteModel(
            userId = userId,
            title = title,
            content = content,
            color = color
        )
        viewModelScope.launch {
            repo.addNote(newNote).onSuccess {
                loadNotes()
            }
        }
    }

    fun updateNote(noteId: String, title: String, content: String, color: Int) {
        viewModelScope.launch {
            repo.updateNote(noteId, mapOf("title" to title, "content" to content, "color" to color)).onSuccess {
                loadNotes()
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            repo.deleteNote(noteId).onSuccess {
                loadNotes()
            }
        }
    }
}
