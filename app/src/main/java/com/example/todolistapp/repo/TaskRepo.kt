package com.example.todolistapp.repo

import com.example.todolistapp.model.TaskModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * TaskRepo provides an abstraction layer over Firestore for task operations.
 * It handles fetching, creating, updating, and deleting tasks for a specific user.
 */
class TaskRepo {
    private val db = FirebaseFirestore.getInstance()
    private val tasksRef = db.collection("tasks")

    suspend fun getTasks(userId: String): List<TaskModel> {
        return try {
            val snapshot = tasksRef
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(TaskModel::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addTask(task: TaskModel): Result<String> {
        return try {
            val docRef = tasksRef.add(task).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTask(taskId: String, updates: Map<String, Any?>): Result<Unit> {
        return try {
            tasksRef.document(taskId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            tasksRef.document(taskId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTasks(taskIds: List<String>): Result<Unit> {
        return try {
            val batch = db.batch()
            taskIds.forEach { id -> batch.delete(tasksRef.document(id)) }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTasks(taskIds: List<String>, updates: Map<String, Any?>): Result<Unit> {
        return try {
            val batch = db.batch()
            taskIds.forEach { id -> 
                batch.update(tasksRef.document(id), updates)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reorderTasks(orderedIds: List<String>): Result<Unit> {
        return try {
            val batch = db.batch()
            orderedIds.forEachIndexed { index, id ->
                batch.update(tasksRef.document(id), "sortOrder", index)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}