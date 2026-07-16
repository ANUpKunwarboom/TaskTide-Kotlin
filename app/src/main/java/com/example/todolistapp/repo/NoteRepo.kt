package com.example.todolistapp.repo

import com.example.todolistapp.model.NoteModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NoteRepo {
    private val db = FirebaseFirestore.getInstance()
    private val notesRef = db.collection("notes")

    suspend fun getNotes(userId: String): List<NoteModel> {
        return try {
            val snapshot = notesRef
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(NoteModel::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addNote(note: NoteModel): Result<String> {
        return try {
            val docRef = notesRef.add(note).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNote(noteId: String, updates: Map<String, Any?>): Result<Unit> {
        return try {
            notesRef.document(noteId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNote(noteId: String): Result<Unit> {
        return try {
            notesRef.document(noteId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
