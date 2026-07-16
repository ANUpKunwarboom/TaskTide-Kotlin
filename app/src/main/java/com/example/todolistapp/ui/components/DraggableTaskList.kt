package com.example.todolistapp.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.todolistapp.model.TaskModel

@Composable
fun DraggableTaskList(
    tasks: List<TaskModel>,
    onReorder: (List<TaskModel>) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    itemContent: @Composable (TaskModel, isDragging: Boolean) -> Unit
) {
    val listState = rememberLazyListState()
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var draggedOffsetY by remember { mutableStateOf(0f) }
    var currentList by remember(tasks) { mutableStateOf(tasks) }

    LaunchedEffect(tasks) { currentList = tasks }

    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = contentPadding
    ) {
        items(currentList.size, key = { currentList[it].id }) { index ->
            val task = currentList[index]
            val isDragging = draggedIndex == index
            val offsetY = if (isDragging) draggedOffsetY else 0f

            Box(
                modifier = Modifier
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer { translationY = offsetY }
                    .then(if (isDragging) Modifier.shadow(8.dp, RoundedCornerShape(14.dp)) else Modifier)
                    .pointerInput(task.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggedIndex = index
                                draggedOffsetY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                draggedOffsetY += dragAmount.y

                                val itemHeightPx = 88.dp.toPx() // approx item height + spacing
                                val from = draggedIndex ?: return@detectDragGesturesAfterLongPress
                                val moveBy = (draggedOffsetY / itemHeightPx).toInt()

                                if (moveBy != 0) {
                                    val to = (from + moveBy).coerceIn(0, currentList.size - 1)
                                    if (to != from) {
                                        val newList = currentList.toMutableList()
                                        val item = newList.removeAt(from)
                                        newList.add(to, item)
                                        currentList = newList
                                        draggedIndex = to
                                        draggedOffsetY -= moveBy * itemHeightPx
                                    }
                                }
                            },
                            onDragEnd = {
                                draggedIndex = null
                                draggedOffsetY = 0f
                                onReorder(currentList)
                            },
                            onDragCancel = {
                                draggedIndex = null
                                draggedOffsetY = 0f
                            }
                        )
                    }
            ) {
                itemContent(task, isDragging)
            }
        }
    }
}