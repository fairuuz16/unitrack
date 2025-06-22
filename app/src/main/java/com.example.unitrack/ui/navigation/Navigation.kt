package com.example.unitrack.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.unitrack.data.model.Course
import com.example.unitrack.data.model.Task
import com.example.unitrack.ui.calendar.CalendarScreen
import com.example.unitrack.ui.dashboard.DashboardScreen
import com.example.unitrack.ui.viewmodel.UniTrackUiState

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    object Calendar : Screen("calendar", "Kalender", Icons.Default.CalendarToday)
}

@Composable
fun UniTrackNavHost(
    navController: NavHostController,
    uiState: UniTrackUiState,
    modifier: Modifier = Modifier,
    onToggleTask: (Task) -> Unit,
    onAddTaskClicked: (Course) -> Unit,
    onEditCourseClicked: (Course) -> Unit,
    onTaskClicked: (Task) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                uiState = uiState,
                onToggleTask = onToggleTask,
                onAddTaskClicked = onAddTaskClicked,
                onEditCourseClicked = onEditCourseClicked,
                onTaskClicked = onTaskClicked
            )
        }
        composable(route = Screen.Calendar.route) {
            CalendarScreen(
                courses = uiState.courses,
                tasks = uiState.tasks,
            )
        }
    }
}
