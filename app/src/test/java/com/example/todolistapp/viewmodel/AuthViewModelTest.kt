package com.example.todolistapp.viewmodel

import com.example.todolistapp.model.UserModel
import com.example.todolistapp.repo.AuthRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel
    private val repo: AuthRepo = mock()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Login Tests ──────────────────────────────────────────────

    @Test
    fun `login success sets success state`() = runTest {
        val email = "test@gmail.com"
        val password = "password123"
        val user = UserModel(uid = "123", email = email, name = "Test User")

        whenever(repo.login(email, password)).doReturn(Result.success(user))

        viewModel.login(email, password)
        advanceUntilIdle()

        assertTrue(viewModel.authState.value is AuthState.Success)
        assertEquals(user, (viewModel.authState.value as AuthState.Success).user)
    }

    @Test
    fun `login failure sets error state`() = runTest {
        val email = "test@gmail.com"
        val password = "wrongpassword"
        val errorMessage = "Login failed"

        whenever(repo.login(email, password)).doReturn(Result.failure(Exception(errorMessage)))

        viewModel.login(email, password)
        advanceUntilIdle()

        assertTrue(viewModel.authState.value is AuthState.Error)
        assertEquals(errorMessage, (viewModel.authState.value as AuthState.Error).message)
    }

    // ── Registration Tests ────────────────────────────────────────

    @Test
    fun `register success sets success state`() = runTest {
        val name = "New User"
        val email = "new@gmail.com"
        val password = "password123"
        val user = UserModel(uid = "456", email = email, name = name)

        whenever(repo.register(name, email, password)).doReturn(Result.success(user))

        viewModel.register(name, email, password)
        advanceUntilIdle()

        assertTrue(viewModel.authState.value is AuthState.Success)
        assertEquals(user, (viewModel.authState.value as AuthState.Success).user)
    }

    @Test
    fun `register with empty fields sets error state`() = runTest {
        viewModel.register("", "", "")
        advanceUntilIdle()

        assertTrue(viewModel.authState.value is AuthState.Error)
        assertEquals("All fields are required", (viewModel.authState.value as AuthState.Error).message)
    }

    @Test
    fun `register with short password sets error state`() = runTest {
        viewModel.register("Name", "email@test.com", "123")
        advanceUntilIdle()

        assertTrue(viewModel.authState.value is AuthState.Error)
        assertEquals("Password must be at least 6 characters", (viewModel.authState.value as AuthState.Error).message)
    }

    @Test
    fun `register failure sets error state`() = runTest {
        val name = "Name"
        val email = "test@gmail.com"
        val password = "password123"
        val errorMessage = "Email already in use"

        whenever(repo.register(name, email, password)).doReturn(Result.failure(Exception(errorMessage)))

        viewModel.register(name, email, password)
        advanceUntilIdle()

        assertTrue(viewModel.authState.value is AuthState.Error)
        assertEquals(errorMessage, (viewModel.authState.value as AuthState.Error).message)
    }

    // ── Password Reset Tests ─────────────────────────────────────

    @Test
    fun `sendPasswordReset success calls callback with true`() = runTest {
        val email = "reset@test.com"
        whenever(repo.sendPasswordReset(email)).doReturn(Result.success(Unit))

        var successCalled = false
        viewModel.sendPasswordReset(email) { success, _ ->
            successCalled = success
        }
        advanceUntilIdle()

        assertTrue(successCalled)
    }

    @Test
    fun `sendPasswordReset failure calls callback with false`() = runTest {
        val email = "unknown@test.com"
        whenever(repo.sendPasswordReset(email)).doReturn(Result.failure(Exception("Error")))

        var successCalled = true
        viewModel.sendPasswordReset(email) { success, _ ->
            successCalled = success
        }
        advanceUntilIdle()

        assertTrue(!successCalled)
    }
}
