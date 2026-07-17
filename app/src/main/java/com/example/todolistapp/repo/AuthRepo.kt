package com.example.todolistapp.repo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.todolistapp.model.UserModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

/**
 * AuthRepo handles all authentication-related data operations.
 * It interacts with Firebase Auth and Firestore for user management.
 */
open class AuthRepo {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    // ── Auth ────────────────────────────────────
    suspend fun register(name: String, email: String, password: String): Result<UserModel> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("User creation failed")

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name).build()
            result.user?.updateProfile(profileUpdates)?.await()

            val user = UserModel(uid = uid, name = name, email = email)
            db.collection("users").document(uid).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<UserModel> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Login failed")
            val doc = db.collection("users").document(uid).get().await()
            val user = doc.toObject(UserModel::class.java)
                ?: UserModel(uid = uid, email = email)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() = auth.signOut()
    fun getCurrentUser() = auth.currentUser

    // ── Password Reset ───────────────────────────
    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Update name ──────────────────────────────
    suspend fun updateName(newName: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("Not logged in")
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName).build()
            user.updateProfile(profileUpdates).await()
            db.collection("users").document(user.uid)
                .update("name", newName).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Update email (re-auth required) ──────────
    suspend fun updateEmail(currentPassword: String, newEmail: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("Not logged in")
            val credential = EmailAuthProvider.getCredential(user.email ?: "", currentPassword)
            user.reauthenticate(credential).await()
            user.verifyBeforeUpdateEmail(newEmail).await()
            db.collection("users").document(user.uid)
                .update("email", newEmail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Change password (re-auth required) ───────
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("Not logged in")
            val credential = EmailAuthProvider.getCredential(user.email ?: "", currentPassword)
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Upload avatar as Base64 to Firestore ─────
    suspend fun uploadAvatar(context: Context, uri: Uri): Result<String> {
        return try {
            val user = auth.currentUser ?: throw Exception("Not logged in")

            // Read + compress image
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Resize to max 200x200 to keep Firestore doc small
            val resized = resizeBitmap(originalBitmap, 200)

            // Compress to JPEG and encode as Base64
            val outputStream = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
            val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

            // Save to Firestore (prefixed so we know it's base64)
            val dataUrl = "data:image/jpeg;base64,$base64String"
            db.collection("users").document(user.uid)
                .update("avatarUrl", dataUrl).await()

            Result.success(dataUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val ratio = minOf(maxSize.toFloat() / width, maxSize.toFloat() / height)
        val newW = (width * ratio).toInt()
        val newH = (height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }

    // ── Get user data ────────────────────────────
    suspend fun getUserData(): Result<UserModel> {
        return try {
            val user = auth.currentUser ?: throw Exception("Not logged in")
            val doc = db.collection("users").document(user.uid).get().await()
            val userData = doc.toObject(UserModel::class.java)
                ?: UserModel(uid = user.uid, email = user.email ?: "")
            Result.success(userData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}