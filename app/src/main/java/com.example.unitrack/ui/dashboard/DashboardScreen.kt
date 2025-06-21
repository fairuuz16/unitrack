package com.example.unitrack.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.unitrack.data.model.Course
import com.example.unitrack.data.model.Task
import com.example.unitrack.ui.components.CourseItem
import com.example.unitrack.ui.components.EmptyState
import com.example.unitrack.ui.components.StatisticsCard
import com.example.unitrack.ui.viewmodel.UniTrackUiState

@Composable
fun DashboardScreen(
    uiState: UniTrackUiState,
    onToggleTask: (Task) -> Unit,
    onAddTaskClicked: (Course) -> Unit,
    onEditCourseClicked: (Course) -> Unit,
    onTaskClicked: (Task) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
             StatisticsCard(tasks = uiState.tasks)
        }

        item {
            Text(
                "Mata Kuliah Anda",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
            )
        }

        if (uiState.courses.isEmpty()) {
            item {
                 EmptyState()
            }
        } else {
            items(uiState.courses, key = { it.id }) { course ->
                CourseItem(
                    course = course,
                    tasks = uiState.tasks.filter { it.courseId == course.id },
                    onAddTaskClicked = { onAddTaskClicked(course) },
                    onEditCourseClicked = { onEditCourseClicked(course) },
                    onToggleTask = onToggleTask,
                    onTaskClicked = onTaskClicked
                )
            }
        }
    }
}
