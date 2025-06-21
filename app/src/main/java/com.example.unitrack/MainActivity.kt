package com.example.unitrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.unitrack.data.model.Course
import com.example.unitrack.data.model.Task
import com.example.unitrack.data.repository.UniTrackRepository
// Import semua dialog yang diperlukan
import com.example.unitrack.ui.components.AddCourseDialog
import com.example.unitrack.ui.components.AddTaskDialog
import com.example.unitrack.ui.components.EditCourseDialog
import com.example.unitrack.ui.components.EditTaskDialog
import com.example.unitrack.ui.navigation.Screen
import com.example.unitrack.ui.navigation.UniTrackNavHost
import com.example.unitrack.ui.theme.UniTrackTheme
import com.example.unitrack.ui.viewmodel.UniTrackUiState
import com.example.unitrack.ui.viewmodel.UniTrackViewModel
import com.example.unitrack.ui.viewmodel.ViewModelFactory
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {

    private val viewModel: UniTrackViewModel by viewModels {
        ViewModelFactory(UniTrackRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniTrackTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                UniTrackApp(
                    uiState = uiState,
                    // Teruskan semua event handler dari ViewModel
                    onAddCourse = viewModel::addCourse,
                    onUpdateCourse = viewModel::updateCourse,
                    onDeleteCourse = viewModel::deleteCourse,
                    onAddTask = viewModel::addTask,
                    onUpdateTask = viewModel::updateTask,
                    onDeleteTask = viewModel::deleteTask,
                    onToggleTask = viewModel::toggleTask
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniTrackApp(
    uiState: UniTrackUiState,
    navController: NavHostController = rememberNavController(),
    onAddCourse: (String, String, String, Int, String) -> Unit,
    onUpdateCourse: (Course) -> Unit,
    onDeleteCourse: (Course) -> Unit,
    onAddTask: (String, String, LocalDateTime) -> Unit,
    onUpdateTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onToggleTask: (Task) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route
    val currentScreen = remember(currentRoute) {
        when (currentRoute) {
            Screen.Calendar.route -> Screen.Calendar
            else -> Screen.Dashboard
        }
    }

    var showAddCourseDialog by remember { mutableStateOf(false) }
    var courseToEdit by remember { mutableStateOf<Course?>(null) }
    var courseForNewTask by remember { mutableStateOf<Course?>(null) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }


    val gradientColors = listOf(Color(0xFF667eea), Color(0xFF764ba2))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = gradientColors))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(currentScreen.title, fontWeight = FontWeight.Bold, color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                AppBottomNavigation(navController = navController, currentRoute = currentRoute)
            },
            floatingActionButton = {
                if (currentScreen == Screen.Dashboard) {
                    FloatingActionButton(
                        onClick = { showAddCourseDialog = true },
                        containerColor = Color.White,
                        contentColor = Color(0xFF667eea),
                        modifier = Modifier.shadow(8.dp, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Mata Kuliah")
                    }
                }
            }
        ) { innerPadding ->
            UniTrackNavHost(
                navController = navController,
                uiState = uiState,
                modifier = Modifier.padding(innerPadding),
                onToggleTask = onToggleTask,
                onAddTaskClicked = { course -> courseForNewTask = course },
                onEditCourseClicked = { course -> courseToEdit = course },
                onTaskClicked = { task -> taskToEdit = task },
            )
        }
    }

    if (showAddCourseDialog) {
        AddCourseDialog(
            onDismiss = { showAddCourseDialog = false },
            onAddCourse = { name, lecturer, schedule, dayOfWeek, time ->
                onAddCourse(name, lecturer, schedule, dayOfWeek, time)
                showAddCourseDialog = false // Tutup dialog setelah selesai
            }
        )
    }

    courseToEdit?.let { course ->
        EditCourseDialog(
            course = course,
            onDismiss = { courseToEdit = null },
            onEditCourse = { updatedCourse ->
                onUpdateCourse(updatedCourse)
                courseToEdit = null
            },
            onDeleteCourse = { courseToDelete ->
                onDeleteCourse(courseToDelete)
                courseToEdit = null
            }
        )
    }

    courseForNewTask?.let { course ->
        AddTaskDialog(
            courseName = course.name,
            onDismiss = { courseForNewTask = null },
            onAddTask = { title, deadline ->
                onAddTask(course.id, title, deadline)
                courseForNewTask = null
            }
        )
    }

    taskToEdit?.let { task ->
        val courseName = uiState.courses.find { it.id == task.courseId }?.name ?: "N/A"
        EditTaskDialog(
            task = task,
            courseName = courseName,
            onDismiss = { taskToEdit = null },
            onEditTask = { updatedTask ->
                onUpdateTask(updatedTask)
                taskToEdit = null
            },
            onDeleteTask = { taskToDelete ->
                onDeleteTask(taskToDelete)
                taskToEdit = null
            }
        )
    }

}

@Composable
fun AppBottomNavigation(navController: NavHostController, currentRoute: String?) {
    NavigationBar(
        containerColor = Color.White.copy(alpha = 0.95f),
        modifier = Modifier.shadow(8.dp)
    ) {
        val screens = listOf(Screen.Dashboard, Screen.Calendar)
        screens.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                icon = {
                    Icon(
                        screen.icon,
                        contentDescription = screen.title,
                        tint = if (isSelected) Color(0xFF667eea) else Color.Gray
                    )
                },
                label = {
                    Text(
                        screen.title,
                        color = if (isSelected) Color(0xFF667eea) else Color.Gray,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFF667eea).copy(alpha = 0.1f)
                )
            )
        }
    }
}
