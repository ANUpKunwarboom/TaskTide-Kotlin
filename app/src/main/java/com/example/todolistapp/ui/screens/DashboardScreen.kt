package com.example.todolistapp.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolistapp.model.CATEGORIES
import com.example.todolistapp.model.TaskModel
import com.example.todolistapp.ui.components.BulkActionBar
import com.example.todolistapp.ui.components.DraggableTaskList
import com.example.todolistapp.ui.components.PomodoroCard
import androidx.compose.foundation.layout.Box as ComposeBox
import com.example.todolistapp.ui.components.TaskDialog
import com.example.todolistapp.ui.components.priorityEmoji
import com.example.todolistapp.ui.theme.*
import com.example.todolistapp.viewmodel.AuthViewModel
import com.example.todolistapp.viewmodel.SortOption
import com.example.todolistapp.viewmodel.TaskFilter
import com.example.todolistapp.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
/**
 * DashboardScreen serves as the main hub of the application.
 * It displays a summary of tasks, statistics, and quick actions.
 */
@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    onLogout: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenNotes: () -> Unit
) {
    val allTasks by taskViewModel.tasks.collectAsState()
    val loading by taskViewModel.loading.collectAsState()
    val filter by taskViewModel.filter.collectAsState()
    val sortOption by taskViewModel.sortOption.collectAsState()
    val searchQuery by taskViewModel.searchQuery.collectAsState()
    val activeCategory by taskViewModel.activeCategory.collectAsState()
    val selectedIds by taskViewModel.selectedTaskIds.collectAsState()
    val pomoSessions by taskViewModel.pomoSessionsToday.collectAsState()
    val errorMsg by taskViewModel.errorMessage.collectAsState()
    val stats by taskViewModel.stats.collectAsState()

    var selectionMode by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var showTaskDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskModel?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<String?>(null) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }

    val currentUser = authViewModel.currentUser
    val displayTasks by taskViewModel.filteredTasks.collectAsState()

    LaunchedEffect(Unit) {
        taskViewModel.loadTasks()
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMsg) {
        errorMsg?.let {
            snackbarHostState.showSnackbar(it)
            taskViewModel.clearError()
        }
    }

    Scaffold(
        containerColor = BgDark,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SurfaceLight,
                    contentColor = TextPrimary,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("TaskTide", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = TextPrimary)
                        Text(
                            "Hi, ${currentUser?.email?.substringBefore("@") ?: "there"}!",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
                    }
                    IconButton(onClick = onOpenNotes) {
                        Icon(Icons.Default.EditNote, contentDescription = "Notes", tint = TextSecondary)
                    }
                    IconButton(onClick = onOpenProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        floatingActionButton = {
            if (!selectionMode) {
                FloatingActionButton(
                    onClick = { editingTask = null; showTaskDialog = true },
                    containerColor = Purple,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // ── Stats row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard("Total", stats.total.toString(), Purple, Modifier.weight(1f))
                StatCard("Done", stats.done.toString(), Green, Modifier.weight(1f))
                StatCard("Pending", stats.pending.toString(), Yellow, Modifier.weight(1f))
                StatCard("Overdue", stats.overdue.toString(), Red, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Search bar (toggle) ──
            if (showSearchBar) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { taskViewModel.setSearchQuery(it) },
                    placeholder = { Text("Search tasks...", color = TextMuted, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { taskViewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        cursorColor = Purple
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Pomodoro Timer ──
            PomodoroCard(
                sessionsToday = pomoSessions,
                onSessionComplete = { taskViewModel.incrementPomoSession() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Quick add ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, RoundedCornerShape(14.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = TextMuted, modifier = Modifier.padding(start = 6.dp))
                OutlinedTextField(
                    value = newTaskTitle,
                    onValueChange = { newTaskTitle = it },
                    placeholder = { Text("Add a new task...", color = TextMuted, fontSize = 14.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Purple
                    )
                )
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            taskViewModel.addTask(newTaskTitle.trim())
                            newTaskTitle = ""
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text("Add", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Filter chips row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    item { FilterChipItem("All", filter == TaskFilter.ALL) { taskViewModel.setFilter(TaskFilter.ALL) } }
                    item { FilterChipItem("Active", filter == TaskFilter.ACTIVE) { taskViewModel.setFilter(TaskFilter.ACTIVE) } }
                    item { FilterChipItem("Done", filter == TaskFilter.DONE) { taskViewModel.setFilter(TaskFilter.DONE) } }
                    item { FilterChipItem("Starred", filter == TaskFilter.STARRED) { taskViewModel.setFilter(TaskFilter.STARRED) } }
                    item { FilterChipItem("Overdue", filter == TaskFilter.OVERDUE) { taskViewModel.setFilter(TaskFilter.OVERDUE) } }
                }

                // Sort button
                ComposeBox {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort", tint = TextSecondary)
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(SurfaceLight)
                    ) {
                        DropdownMenuItem(text = { Text("Newest First", color = TextPrimary) }, onClick = { taskViewModel.setSortOption(SortOption.NEWEST); showSortMenu = false })
                        DropdownMenuItem(text = { Text("Oldest First", color = TextPrimary) }, onClick = { taskViewModel.setSortOption(SortOption.OLDEST); showSortMenu = false })
                        DropdownMenuItem(text = { Text("Due Date", color = TextPrimary) }, onClick = { taskViewModel.setSortOption(SortOption.DUE_DATE); showSortMenu = false })
                        DropdownMenuItem(text = { Text("Priority", color = TextPrimary) }, onClick = { taskViewModel.setSortOption(SortOption.PRIORITY); showSortMenu = false })
                        DropdownMenuItem(text = { Text("Name A-Z", color = TextPrimary) }, onClick = { taskViewModel.setSortOption(SortOption.NAME); showSortMenu = false })
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Category chips ──
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChipItem("All", activeCategory == "All", small = true) { taskViewModel.setActiveCategory("All") } }
                items(CATEGORIES) { cat ->
                    FilterChipItem(cat, activeCategory == cat, small = true) {
                        taskViewModel.setActiveCategory(if (activeCategory == cat) "All" else cat)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Task list ──
            if (loading) {
                ComposeBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Purple)
                }
            } else if (displayTasks.isEmpty()) {
                ComposeBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✦", fontSize = 40.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No tasks here — add one above!", color = TextMuted, fontSize = 14.sp)
                    }
                }
            } else {
                ComposeBox(modifier = Modifier.fillMaxSize()) {
                    DraggableTaskList(
                        tasks = displayTasks,
                        onReorder = { newOrder -> taskViewModel.reorderTasks(newOrder) },
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) { task, _ ->
                        TaskItem(
                            task = task,
                            onToggle = { taskViewModel.toggleDone(task) },
                            onStar = { taskViewModel.toggleStar(task) },
                            onDelete = { taskToDelete = task.id },
                            onEdit = {
                                if (selectionMode) {
                                    taskViewModel.toggleSelect(task.id)
                                } else {
                                    editingTask = task; showTaskDialog = true
                                }
                            },
                            onLongPress = {
                                selectionMode = true
                                taskViewModel.toggleSelect(task.id)
                            },
                            isSelected = selectedIds.contains(task.id),
                            selectionMode = selectionMode
                        )
                    }

                    // Bulk action bar
                    BulkActionBar(
                        visible = selectionMode && selectedIds.isNotEmpty(),
                        selectedCount = selectedIds.size,
                        onComplete = { taskViewModel.bulkComplete(); selectionMode = false },
                        onDelete = { showBulkDeleteConfirm = true },
                        onClear = { taskViewModel.clearSelection(); selectionMode = false },
                        onSelectAll = { taskViewModel.selectAll() },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                    )
                }
            }
        }
    }

    // ── Delete Confirmation Dialog ──
    if (taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Delete Task?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        taskViewModel.deleteTask(taskToDelete!!)
                        taskToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Red)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) { Text("Cancel") }
            },
            containerColor = SurfaceLight,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }

    if (showBulkDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showBulkDeleteConfirm = false },
            title = { Text("Delete Selected Tasks?") },
            text = { Text("Are you sure you want to delete ${selectedIds.size} tasks?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        taskViewModel.bulkDelete()
                        showBulkDeleteConfirm = false
                        selectionMode = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Red)
                ) { Text("Delete All") }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDeleteConfirm = false }) { Text("Cancel") }
            },
            containerColor = SurfaceLight,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }

    // ── Task dialog (create / edit) ──
    if (showTaskDialog) {
        TaskDialog(
            task = editingTask,
            onDismiss = { showTaskDialog = false; editingTask = null },
            onSave = { title, notes, priority, category, dueDate ->
                if (editingTask == null) {
                    taskViewModel.addTask(title, priority, category, dueDate, notes)
                } else {
                    taskViewModel.updateTask(
                        editingTask!!.id,
                        editingTask!!.copy(
                            title = title,
                            notes = notes,
                            priority = priority,
                            category = category,
                            dueDate = dueDate
                        )
                    )
                }
                showTaskDialog = false
                editingTask = null
            }
        )
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(SurfaceDark, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun FilterChipItem(label: String, selected: Boolean, small: Boolean = false, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Purple else SurfaceDark,
        modifier = Modifier.height(if (small) 32.dp else 40.dp)
    ) {
        ComposeBox(
            modifier = Modifier.padding(horizontal = if (small) 12.dp else 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                color = if (selected) Color.White else TextSecondary,
                fontSize = if (small) 12.sp else 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItem(
    task: TaskModel,
    onToggle: () -> Unit,
    onStar: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onLongPress: () -> Unit = {},
    isSelected: Boolean = false,
    selectionMode: Boolean = false
) {
    val priorityColor = when (task.priority) {
        "urgent" -> Orange
        "high"   -> Red
        "low"    -> Green
        else     -> Yellow
    }

    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val now = System.currentTimeMillis()
    val isOverdue = task.dueDate != null && task.dueDate < now && !task.isDone

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Purple.copy(alpha = 0.12f) else SurfaceDark,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) Modifier.border(1.5.dp, Purple, RoundedCornerShape(14.dp))
                else Modifier
            )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Priority bar
            ComposeBox(
                modifier = Modifier
                    .width(3.dp)
                    .height(if (task.dueDate != null || task.category.isNotBlank()) 56.dp else 36.dp)
                    .background(priorityColor, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Checkbox or selection indicator
            if (selectionMode) {
                IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                    ComposeBox(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Purple else Color.Transparent)
                            .border(2.dp, if (isSelected) Purple else BorderColor, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            } else {
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(top = 2.dp)
                ) {
                    ComposeBox(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (task.isDone) Green else Color.Transparent)
                            .border(2.dp, if (task.isDone) Green else BorderColor, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (task.isDone) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content Area (Clickable for Edit)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .combinedClickable(
                        onClick = onEdit,
                        onLongClick = onLongPress
                    )
            ) {
                Text(
                    text = task.title,
                    color = if (task.isDone) TextMuted else TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "📝 ${task.notes}",
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (task.category.isNotBlank() || task.dueDate != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (task.category.isNotBlank()) {
                            ComposeBox(
                                modifier = Modifier
                                    .background(Purple.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("📂 ${task.category}", fontSize = 10.sp, color = PurpleLight)
                            }
                        }
                        if (task.dueDate != null) {
                            ComposeBox(
                                modifier = Modifier
                                    .background(
                                        (if (isOverdue) Red else Blue).copy(alpha = 0.12f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    (if (isOverdue) "⚠️ " else "📅 ") + dateFormat.format(Date(task.dueDate)),
                                    fontSize = 10.sp,
                                    color = if (isOverdue) Red else Blue
                                )
                            }
                        }
                    }
                }
            }

            if (!selectionMode) {
                Row {
                    // Star
                    IconButton(onClick = onStar, modifier = Modifier.size(40.dp)) {
                        Icon(
                            if (task.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (task.isStarred) Yellow else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Delete
                    IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
