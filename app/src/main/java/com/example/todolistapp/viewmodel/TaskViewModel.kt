package com.example.todolistapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolistapp.model.TaskModel
import com.example.todolistapp.repo.TaskRepo
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class TaskFilter { ALL, ACTIVE, DONE, STARRED, OVERDUE }
enum class SortOption { NEWEST, OLDEST, DUE_DATE, PRIORITY, NAME }

data class TaskStats(
    val total: Int = 0,
    val done: Int = 0,
    val overdue: Int = 0,
    val pending: Int = 0
)

/**
 * TaskViewModel manages the state and logic for task-related operations.
 * It provides methods for adding, updating, deleting, and fetching tasks.
 */
class TaskViewModel(
    private val repo: TaskRepo = TaskRepo(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<TaskModel>>(emptyList())
    val tasks: StateFlow<List<TaskModel>> = _tasks

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _filter = MutableStateFlow(TaskFilter.ALL)
    val filter: StateFlow<TaskFilter> = _filter

    private val _sortOption = MutableStateFlow(SortOption.NEWEST)
    val sortOption: StateFlow<SortOption> = _sortOption

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _activeCategory = MutableStateFlow("All")
    val activeCategory: StateFlow<String> = _activeCategory

    private val _selectedTaskIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedTaskIds: StateFlow<Set<String>> = _selectedTaskIds

    private val _pomoSessionsToday = MutableStateFlow(0)
    val pomoSessionsToday: StateFlow<Int> = _pomoSessionsToday

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Reactive filtered and sorted list
    val filteredTasks: StateFlow<List<TaskModel>> = combine(
        _tasks, _filter, _sortOption, _searchQuery, _activeCategory
    ) { tasks, filter, sort, query, category ->
        val now = System.currentTimeMillis()
        
        var result = tasks.filter { task ->
            val matchesFilter = when (filter) {
                TaskFilter.ALL     -> true
                TaskFilter.ACTIVE  -> !task.isDone
                TaskFilter.DONE    -> task.isDone
                TaskFilter.STARRED -> task.isStarred
                TaskFilter.OVERDUE -> task.dueDate != null && task.dueDate < now && !task.isDone
            }
            val matchesCategory = category == "All" || task.category == category
            val matchesSearch = query.isBlank() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.notes.contains(query, ignoreCase = true)

            matchesFilter && matchesCategory && matchesSearch
        }

        result = when (sort) {
            SortOption.NEWEST   -> result.sortedByDescending { it.createdAt }
            SortOption.OLDEST   -> result.sortedBy { it.createdAt }
            SortOption.DUE_DATE -> result.sortedWith(compareBy(nullsLast()) { it.dueDate })
            SortOption.PRIORITY -> {
                val order = mapOf("urgent" to 0, "high" to 1, "medium" to 2, "low" to 3)
                result.sortedBy { order[it.priority] ?: 4 }
            }
            SortOption.NAME -> result.sortedBy { it.title.lowercase() }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<TaskStats> = _tasks.map { tasks ->
        val now = System.currentTimeMillis()
        val total = tasks.size
        val done = tasks.count { it.isDone }
        val overdue = tasks.count { it.dueDate != null && it.dueDate < now && !it.isDone }
        TaskStats(total, done, overdue, total - done)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskStats())

    fun incrementPomoSession() {
        _pomoSessionsToday.value += 1
    }

    // ── Load tasks ──────────────────────────────
    fun loadTasks() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _loading.value = true
            _tasks.value = repo.getTasks(userId)
            _loading.value = false
        }
    }

    // ── Filters / Sort / Search setters ─────────
    fun setFilter(f: TaskFilter) { _filter.value = f }
    fun setSortOption(s: SortOption) { _sortOption.value = s }
    fun setSearchQuery(q: String) { _searchQuery.value = q }
    fun setActiveCategory(c: String) { _activeCategory.value = c }

    // ── CRUD ─────────────────────────────────────
    fun addTask(
        title: String,
        priority: String = "medium",
        category: String = "",
        dueDate: Long? = null,
        notes: String = ""
    ) {
        val userId = auth.currentUser?.uid ?: return
        if (title.isBlank()) return

        val task = TaskModel(
            userId = userId,
            title = title,
            priority = priority,
            category = category,
            dueDate = dueDate,
            notes = notes,
            sortOrder = _tasks.value.size.toLong()
        )
        viewModelScope.launch {
            val result = repo.addTask(task)
            result.onSuccess { newId ->
                _tasks.value = listOf(task.copy(id = newId)) + _tasks.value
            }
        }
    }

    fun updateTask(taskId: String, updatedTask: TaskModel) {
        val updates = mapOf(
            "title" to updatedTask.title,
            "notes" to updatedTask.notes,
            "priority" to updatedTask.priority,
            "category" to updatedTask.category,
            "dueDate" to updatedTask.dueDate
        )
        viewModelScope.launch {
            repo.updateTask(taskId, updates)
            _tasks.value = _tasks.value.map {
                if (it.id == taskId) updatedTask.copy(id = taskId) else it
            }
        }
    }

    fun toggleDone(task: TaskModel) {
        viewModelScope.launch {
            repo.updateTask(task.id, mapOf("isDone" to !task.isDone))
            _tasks.value = _tasks.value.map {
                if (it.id == task.id) it.copy(isDone = !it.isDone) else it
            }
        }
    }

    fun deleteRemaining(id: String) {
        _errorMessage.value = null
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            val result = repo.deleteTask(taskId)
            result.onSuccess {
                _tasks.value = _tasks.value.filter { it.id != taskId }
                _selectedTaskIds.value = _selectedTaskIds.value - taskId
            }.onFailure {
                _errorMessage.value = "Failed to delete task: ${it.message}"
            }
        }
    }

    fun toggleStar(task: TaskModel) {
        viewModelScope.launch {
            repo.updateTask(task.id, mapOf("isStarred" to !task.isStarred))
            _tasks.value = _tasks.value.map {
                if (it.id == task.id) it.copy(isStarred = !it.isStarred) else it
            }
        }
    }

    // ── Reordering (drag and drop) ───────────────
    fun reorderTasks(newOrder: List<TaskModel>) {
        _tasks.value = newOrder
        viewModelScope.launch {
            repo.reorderTasks(newOrder.map { it.id })
        }
    }

    // ── Bulk selection ────────────────────────────
    fun toggleSelect(taskId: String) {
        _selectedTaskIds.value = if (_selectedTaskIds.value.contains(taskId)) {
            _selectedTaskIds.value - taskId
        } else {
            _selectedTaskIds.value + taskId
        }
    }

    fun selectAll() {
        // Select only currently visible/filtered tasks
        _selectedTaskIds.value = filteredTasks.value.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedTaskIds.value = emptySet()
    }

    fun bulkComplete() {
        val ids = _selectedTaskIds.value.toList()
        if (ids.isEmpty()) return
        
        viewModelScope.launch {
            val result = repo.updateTasks(ids, mapOf("isDone" to true))
            result.onSuccess {
                _tasks.value = _tasks.value.map {
                    if (ids.contains(it.id)) it.copy(isDone = true) else it
                }
                clearSelection()
            }.onFailure {
                _errorMessage.value = "Failed to update tasks: ${it.message}"
            }
        }
    }

    fun bulkDelete() {
        val ids = _selectedTaskIds.value.toList()
        if (ids.isEmpty()) return

        viewModelScope.launch {
            val result = repo.deleteTasks(ids)
            result.onSuccess {
                _tasks.value = _tasks.value.filter { !ids.contains(it.id) }
                clearSelection()
            }.onFailure {
                _errorMessage.value = "Failed to delete tasks: ${it.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // ── Stats ─────────────────────────────────────
    fun getStats(): Triple<Int, Int, Int> {
        val tasksList = _tasks.value
        val total = tasksList.size
        val done = tasksList.count { it.isDone }
        val overdue = tasksList.count {
            it.dueDate != null && it.dueDate < System.currentTimeMillis() && !it.isDone
        }
        return Triple(total, done, overdue)
    }
}
