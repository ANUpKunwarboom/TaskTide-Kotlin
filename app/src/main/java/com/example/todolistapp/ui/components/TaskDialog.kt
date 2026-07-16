package com.example.todolistapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.todolistapp.model.CATEGORIES
import com.example.todolistapp.model.PRIORITIES
import com.example.todolistapp.model.TaskModel
import com.example.todolistapp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskDialog(
    task: TaskModel?, // null = create new
    onDismiss: () -> Unit,
    onSave: (title: String, notes: String, priority: String, category: String, dueDate: Long?) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var notes by remember { mutableStateOf(task?.notes ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: "medium") }
    var category by remember { mutableStateOf(task?.category ?: "") }
    var dueDate by remember { mutableStateOf(task?.dueDate) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showPriorityMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (task == null) "➕ New Task" else "✏️ Edit Task",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text("TASK TITLE *", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("What needs to be done?", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = dialogFieldColors()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Notes
                Text("NOTES", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Add details...", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = dialogFieldColors(),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Priority + Category row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Priority
                    Column(modifier = Modifier.weight(1f)) {
                        Text("PRIORITY", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box {
                            OutlinedButton(
                                onClick = { showPriorityMenu = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                            ) {
                                Text(priorityEmoji(priority) + " " + priority.replaceFirstChar { it.uppercase() })
                            }
                            DropdownMenu(
                                expanded = showPriorityMenu,
                                onDismissRequest = { showPriorityMenu = false },
                                modifier = Modifier.background(SurfaceLight)
                            ) {
                                PRIORITIES.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text("${priorityEmoji(p)} ${p.replaceFirstChar { it.uppercase() }}", color = TextPrimary) },
                                        onClick = { priority = p; showPriorityMenu = false }
                                    )
                                }
                            }
                        }
                    }

                    // Category
                    Column(modifier = Modifier.weight(1f)) {
                        Text("CATEGORY", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box {
                            OutlinedButton(
                                onClick = { showCategoryMenu = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                            ) {
                                Text(if (category.isBlank()) "None" else category, maxLines = 1)
                            }
                            DropdownMenu(
                                expanded = showCategoryMenu,
                                onDismissRequest = { showCategoryMenu = false },
                                modifier = Modifier.background(SurfaceLight)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None", color = TextPrimary) },
                                    onClick = { category = ""; showCategoryMenu = false }
                                )
                                CATEGORIES.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c, color = TextPrimary) },
                                        onClick = { category = c; showCategoryMenu = false }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Due date
                Text("DUE DATE", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgDark, RoundedCornerShape(12.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            dueDate?.let { dateFormat.format(Date(it)) } ?: "No due date set",
                            color = if (dueDate != null) TextPrimary else TextMuted,
                            fontSize = 14.sp
                        )
                    }
                    if (dueDate != null) {
                        IconButton(onClick = { dueDate = null }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save / Cancel
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                    ) { Text("Cancel") }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(title.trim(), notes.trim(), priority, category, dueDate)
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple)
                    ) {
                        Text(if (task == null) "Add Task" else "Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK", color = Purple) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = TextSecondary) }
            },
            colors = DatePickerDefaults.colors(containerColor = SurfaceDark)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextPrimary,
                    headlineContentColor = TextPrimary,
                    weekdayContentColor = TextSecondary,
                    dayContentColor = TextPrimary,
                    selectedDayContainerColor = Purple,
                    todayContentColor = Purple,
                    todayDateBorderColor = Purple
                )
            )
        }
    }
}

fun priorityEmoji(priority: String) = when (priority) {
    "urgent" -> "🔶"
    "high" -> "🔴"
    "low" -> "🟢"
    else -> "🟡"
}

@Composable
fun dialogFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Purple,
    unfocusedBorderColor = BorderColor,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = Purple,
    focusedContainerColor = BgDark,
    unfocusedContainerColor = BgDark
)