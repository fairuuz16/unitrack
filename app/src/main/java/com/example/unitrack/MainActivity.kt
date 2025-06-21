package com.example.unitrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.unitrack.ui.theme.UniTrackTheme
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID

data class Course(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val lecturer: String,
    val schedule: String,
    val dayOfWeek: Int,
    val time: String
)

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val courseId: String,
    val title: String,
    val deadline: LocalDateTime,
    var isCompleted: Boolean = false
)

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    object Calendar : Screen("calendar", "Kalender", Icons.Default.CalendarToday)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniTrackTheme {
                UniTrackApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniTrackApp() {
    val courses = remember { mutableStateListOf<Course>() }
    val tasks = remember { mutableStateListOf<Task>() }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showEditCourseDialog by remember { mutableStateOf<Course?>(null) }
    var showAddTaskDialog by remember { mutableStateOf<Course?>(null) }
    var showEditTaskDialog by remember { mutableStateOf<Task?>(null) }

    LaunchedEffect(Unit) {
        if (courses.isEmpty()) {
            val pbm = Course(
                name = "Pemrograman Perangkat Bergerak",
                lecturer = "Fajar Baskoro, S.Kom., M.T.",
                schedule = "Senin, 13.30",
                dayOfWeek = 1, // Senin
                time = "13.30"
            )
            val pbo = Course(
                name = "Pemrograman Jaringan",
                lecturer = "Royyana Muslim Ijtihadie, S.Kom.,M.Kom., Ph.D.",
                schedule = "Selasa, 10.00",
                dayOfWeek = 2, // Selasa
                time = "13:00"
            )
            val math = Course(
                name = "Perancangan dan Analisis Algoritma",
                lecturer = "Misbakhul Munir Irfan Subakti, S.Kom., M.Sc.",
                schedule = "Rabu, 07.00",
                dayOfWeek = 3, // Rabu
                time = "10:00"
            )
            courses.addAll(listOf(pbm, pbo, math))
            tasks.add(Task(courseId = pbm.id, title = "Buat Aplikasi Catatan", deadline = LocalDate.now().plusDays(5).atTime(23, 59)))
            tasks.add(Task(courseId = pbm.id, title = "Implementasi API", deadline = LocalDate.now().plusDays(12).atTime(18, 0), isCompleted = true))
            tasks.add(Task(courseId = pbo.id, title = "Desain Final Project", deadline = LocalDate.now().plusDays(7).atTime(23, 59)))
        }
    }

    val gradientColors = listOf(
        Color(0xFF667eea),
        Color(0xFF764ba2),
        Color(0xFFf093fb)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            currentScreen.title,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White.copy(alpha = 0.95f),
                    modifier = Modifier.shadow(8.dp)
                ) {
                    val screens = listOf(Screen.Dashboard, Screen.Calendar)
                    screens.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    screen.icon,
                                    contentDescription = screen.title,
                                    tint = if (currentScreen == screen) Color(0xFF667eea) else Color.Gray
                                )
                            },
                            label = {
                                Text(
                                    screen.title,
                                    color = if (currentScreen == screen) Color(0xFF667eea) else Color.Gray,
                                    fontWeight = if (currentScreen == screen) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selected = currentScreen == screen,
                            onClick = { currentScreen = screen },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF667eea),
                                selectedTextColor = Color(0xFF667eea),
                                indicatorColor = Color(0xFF667eea).copy(alpha = 0.1f)
                            )
                        )
                    }
                }
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
        ) { paddingValues ->
            when (currentScreen) {
                Screen.Dashboard -> {
                    DashboardScreen(
                        courses = courses,
                        tasks = tasks,
                        paddingValues = paddingValues,
                        onAddTaskClicked = { course -> showAddTaskDialog = course },
                        onEditCourseClicked = { course -> showEditCourseDialog = course },
                        onToggleTask = { taskToToggle ->
                            val taskIndex = tasks.indexOfFirst { it.id == taskToToggle.id }
                            if (taskIndex != -1) {
                                val updatedTask = tasks[taskIndex].copy(isCompleted = !tasks[taskIndex].isCompleted)
                                tasks[taskIndex] = updatedTask
                            }
                        },
                        onTaskClicked = { taskToEdit ->
                            showEditTaskDialog = taskToEdit
                        }
                    )
                }
                Screen.Calendar -> {
                    CalendarScreen(
                        courses = courses,
                        tasks = tasks,
                        paddingValues = paddingValues
                    )
                }
            }
        }
    }

    if (showAddCourseDialog) {
        AddCourseDialog(
            onDismiss = { showAddCourseDialog = false },
            onAddCourse = { name, lecturer, schedule, dayOfWeek, time ->
                courses.add(Course(
                    name = name,
                    lecturer = lecturer,
                    schedule = schedule,
                    dayOfWeek = dayOfWeek,
                    time = time
                ))
                showAddCourseDialog = false
            }
        )
    }

    showEditCourseDialog?.let { course ->
        EditCourseDialog(
            course = course,
            onDismiss = { showEditCourseDialog = null },
            onEditCourse = { updatedCourse ->
                val index = courses.indexOfFirst { it.id == updatedCourse.id }
                if (index != -1) {
                    courses[index] = updatedCourse
                }
                showEditCourseDialog = null
            },
            onDeleteCourse = { courseToDelete ->
                courses.remove(courseToDelete)
                tasks.removeAll { it.courseId == courseToDelete.id }
                showEditCourseDialog = null
            }
        )
    }

    showAddTaskDialog?.let { course ->
        AddTaskDialog(
            courseName = course.name,
            onDismiss = { showAddTaskDialog = null },
            onAddTask = { title, deadline ->
                tasks.add(Task(courseId = course.id, title = title, deadline = deadline))
                showAddTaskDialog = null
            }
        )
    }

    showEditTaskDialog?.let { task ->
        val courseName = courses.find { it.id == task.courseId }?.name ?: "N/A"
        EditTaskDialog(
            task = task,
            courseName = courseName,
            onDismiss = { showEditTaskDialog = null },
            onEditTask = { updatedTask ->
                val index = tasks.indexOfFirst { it.id == updatedTask.id }
                if (index != -1) {
                    tasks[index] = updatedTask
                }
                showEditTaskDialog = null
            },
            onDeleteTask = { taskToDelete ->
                tasks.remove(taskToDelete)
                showEditTaskDialog = null
            }
        )
    }
}

@Composable
fun DashboardScreen(
    courses: List<Course>,
    tasks: List<Task>,
    paddingValues: PaddingValues,
    onAddTaskClicked: (Course) -> Unit,
    onEditCourseClicked: (Course) -> Unit,
    onToggleTask: (Task) -> Unit,
    onTaskClicked: (Task) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { -40 },
                    animationSpec = tween(600)
                ) + fadeIn(animationSpec = tween(600))
            ) {
                StatisticsCard(tasks = tasks)
            }
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

        if (courses.isEmpty()) {
            item {
                EmptyState()
            }
        } else {
            items(courses, key = { it.id }) { course ->
                val index = courses.indexOf(course)
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { 60 },
                        animationSpec = tween(600, delayMillis = index * 100)
                    ) + fadeIn(animationSpec = tween(600, delayMillis = index * 100))
                ) {
                    CourseItem(
                        course = course,
                        tasks = tasks.filter { it.courseId == course.id },
                        onAddTaskClicked = { onAddTaskClicked(course) },
                        onEditCourseClicked = { onEditCourseClicked(course) },
                        onToggleTask = onToggleTask,
                        onTaskClicked = onTaskClicked
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarScreen(
    courses: List<Course>,
    tasks: List<Task>,
    paddingValues: PaddingValues
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        CalendarWidget(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            courses = courses,
            tasks = tasks,
            onDateSelected = { selectedDate = it },
            onMonthChanged = { currentMonth = it }
        )

        ScheduleForDate(
            date = selectedDate,
            courses = courses,
            tasks = tasks
        )
    }
}

@Composable
fun CalendarWidget(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    courses: List<Course>,
    tasks: List<Task>,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeekIndex = (firstDayOfMonth.dayOfWeek.value % 7)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChanged(currentMonth.minusMonths(1)) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month", tint = Color(0xFF667eea))
                }
                Text(
                    text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("in", "ID"))} ${currentMonth.year}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )
                IconButton(onClick = { onMonthChanged(currentMonth.plusMonths(1)) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = Color(0xFF667eea))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val dayNames = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                dayNames.forEach { dayName ->
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667eea),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                userScrollEnabled = false,
                modifier = Modifier.height(280.dp)
            ) {
                items(firstDayOfWeekIndex) {
                    Spacer(modifier = Modifier.aspectRatio(1f))
                }

                items(daysInMonth) { dayIndex ->
                    val day = dayIndex + 1
                    val date = currentMonth.atDay(day)
                    val hasCourse = courses.any { it.dayOfWeek == date.dayOfWeek.value }
                    val hasTask = tasks.any { it.deadline.toLocalDate() == date }
                    val isSelected = date == selectedDate
                    val isToday = date == LocalDate.now()

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> Color(0xFF667eea)
                                    isToday -> Color(0xFF667eea).copy(alpha = 0.2f)
                                    else -> Color.Transparent
                                }
                            )
                            .clickable { onDateSelected(date) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$day",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isSelected -> Color.White
                                    isToday -> Color(0xFF667eea)
                                    else -> Color(0xFF2D3748)
                                }
                            )
                            if (hasCourse || hasTask) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(
                                            when {
                                                isSelected -> Color.White
                                                hasTask -> Color(0xFFF56565)
                                                else -> Color(0xFF48BB78)
                                            },
                                            CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ScheduleForDate(
    date: LocalDate,
    courses: List<Course>,
    tasks: List<Task>
) {
    val dayOfWeek = date.dayOfWeek.value
    val scheduledCourses = courses.filter { it.dayOfWeek == dayOfWeek }
    val tasksForDate = tasks.filter { it.deadline.toLocalDate() == date }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Jadwal",
                        tint = Color(0xFF667eea),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Jadwal Kuliah",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (scheduledCourses.isEmpty()) {
                    EmptyScheduleItem("Tidak ada jadwal kuliah hari ini.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        scheduledCourses.sortedBy { it.time }.forEach { course ->
                            ScheduleItem(course = course)
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = "Tugas",
                        tint = Color(0xFFF56565),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Deadline Hari Ini",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (tasksForDate.isEmpty()) {
                    EmptyScheduleItem("Tidak ada tugas dengan deadline hari ini.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        tasksForDate.sortedBy { it.deadline }.forEach { task ->
                            val courseName = courses.find { it.id == task.courseId }?.name ?: "Mata Kuliah Tidak Ditemukan"
                            TaskDeadlineItem(task = task, courseName = courseName)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyScheduleItem(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🎉",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF4A5568),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun ScheduleItem(course: Course) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF667eea).copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, Color(0xFF667eea).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Color(0xFF667eea),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = course.time,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = course.lecturer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF667eea)
                )
            }
        }
    }
}

@Composable
fun TaskDeadlineItem(task: Task, courseName: String) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF56565).copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, Color(0xFFF56565).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Color(0xFFF56565),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = task.deadline.format(timeFormatter),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    fontWeight = FontWeight.Bold,
                    color = if (task.isCompleted) Color.Gray else Color(0xFF2D3748)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dari: $courseName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (task.isCompleted) Color.Gray else Color(0xFFF56565)
                )
            }
            if (task.isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selesai",
                    tint = Color(0xFF48BB78),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Book,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFF667eea)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Belum Ada Mata Kuliah",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF667eea)
                )
                Text(
                    text = "Tekan tombol '+' untuk memulai.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun StatisticsCard(tasks: List<Task>) {
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.isCompleted }
    val progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks.toFloat() else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = EaseOutCubic), label = "progressAnimation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        )
    ) {
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF667eea).copy(alpha = 0.1f),
                                Color(0xFF764ba2).copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = Color(0xFF667eea),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Progress Seluruh Tugas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp)
                            .clip(CircleShape),
                        color = Color(0xFF667eea),
                        trackColor = Color(0xFF667eea).copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667eea)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(
                        icon = Icons.Default.CheckCircle,
                        title = "Selesai",
                        value = completedTasks.toString(),
                        color = Color(0xFF48BB78)
                    )
                    StatItem(
                        icon = Icons.Default.Assignment,
                        title = "Total",
                        value = totalTasks.toString(),
                        color = Color(0xFF667eea)
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(icon: ImageVector, title: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun CourseItem(
    course: Course,
    tasks: List<Task>,
    onAddTaskClicked: () -> Unit,
    onEditCourseClicked: (Course) -> Unit,
    onToggleTask: (Task) -> Unit,
    onTaskClicked: (Task) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF667eea).copy(alpha = 0.1f),
                                Color(0xFF764ba2).copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            course.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow(icon = Icons.Default.Person, text = course.lecturer)
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoRow(icon = Icons.Default.Schedule, text = course.schedule)
                    }
                    IconButton(onClick = { onEditCourseClicked(course) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Mata Kuliah", tint = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Daftar Tugas:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )
                Button(
                    onClick = onAddTaskClicked,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF667eea)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Tugas", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tugas")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (tasks.isEmpty()) {
                Text(
                    "Tidak ada tugas untuk mata kuliah ini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 4.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tasks.sortedBy { it.deadline }.forEach { task ->
                        TaskItem(
                            task = task,
                            onToggleTask = { onToggleTask(task) },
                            onTaskClicked = { onTaskClicked(task) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF667eea),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF4A5568)
        )
    }
}

@Composable
fun TaskItem(task: Task, onToggleTask: () -> Unit, onTaskClicked: () -> Unit) {
    val dateTimeFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yy, HH:mm", Locale("in", "ID")) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onTaskClicked)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggleTask) {
            Icon(
                imageVector = if (task.isCompleted) Icons.Outlined.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = "Toggle Task",
                tint = if (task.isCompleted) Color(0xFF48BB78) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                color = if (task.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Deadline: ${task.deadline.format(dateTimeFormatter)}",
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                color = if (task.isCompleted) Color.Gray else Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourseDialog(
    onDismiss: () -> Unit,
    onAddCourse: (name: String, lecturer: String, schedule: String, dayOfWeek: Int, time: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var lecturer by remember { mutableStateOf("") }
    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
    var selectedDay by remember { mutableStateOf(days.first()) }
    var time by remember { mutableStateOf("") }
    val isFormValid = name.isNotBlank() && lecturer.isNotBlank() && time.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Tambah Mata Kuliah", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Mata Kuliah") })
                OutlinedTextField(value = lecturer, onValueChange = { lecturer = it }, label = { Text("Nama Dosen") })
                OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Waktu (cth: 08:00)") })

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Hari:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 8.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(100.dp)
                    ) {
                        items(days) { day ->
                            val isSelected = selectedDay == day
                            OutlinedButton(
                                onClick = { selectedDay = day },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) Color(0xFF667eea) else Color.Transparent,
                                    contentColor = if (isSelected) Color.White else Color(0xFF667eea)
                                ),
                                border = BorderStroke(1.dp, Color(0xFF667eea))
                            ) {
                                Text(day, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val dayOfWeek = days.indexOf(selectedDay) + 1
                            val schedule = "$selectedDay, $time"
                            onAddCourse(name, lecturer, schedule, dayOfWeek, time)
                        },
                        enabled = isFormValid
                    ) {
                        Text("Tambah")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCourseDialog(
    course: Course,
    onDismiss: () -> Unit,
    onEditCourse: (Course) -> Unit,
    onDeleteCourse: (Course) -> Unit
) {
    var name by remember { mutableStateOf(course.name) }
    var lecturer by remember { mutableStateOf(course.lecturer) }
    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
    var selectedDay by remember { mutableStateOf(days[course.dayOfWeek - 1]) }
    var time by remember { mutableStateOf(course.time) }
    val isFormValid = name.isNotBlank() && lecturer.isNotBlank() && time.isNotBlank()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Edit Mata Kuliah", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Mata Kuliah") })
                OutlinedTextField(value = lecturer, onValueChange = { lecturer = it }, label = { Text("Nama Dosen") })
                OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Waktu (cth: 08:00)") })

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Hari:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 8.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(100.dp)
                    ) {
                        items(days) { day ->
                            val isSelected = selectedDay == day
                            OutlinedButton(
                                onClick = { selectedDay = day },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) Color(0xFF667eea) else Color.Transparent,
                                    contentColor = if (isSelected) Color.White else Color(0xFF667eea)
                                ),
                                border = BorderStroke(1.dp, Color(0xFF667eea))
                            ) {
                                Text(day, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Hapus")
                    }

                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val dayOfWeek = days.indexOf(selectedDay) + 1
                                val schedule = "$selectedDay, $time"
                                val updatedCourse = course.copy(
                                    name = name,
                                    lecturer = lecturer,
                                    schedule = schedule,
                                    dayOfWeek = dayOfWeek,
                                    time = time
                                )
                                onEditCourse(updatedCourse)
                            },
                            enabled = isFormValid
                        ) {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmationDialog(
            title = "Hapus Mata Kuliah?",
            text = "Aksi ini akan menghapus mata kuliah dan semua tugas yang terkait. Aksi ini tidak dapat dibatalkan.",
            onConfirm = {
                onDeleteCourse(course)
                showDeleteConfirm = false
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    courseName: String,
    onDismiss: () -> Unit,
    onAddTask: (title: String, deadline: LocalDateTime) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var deadlineDate by remember { mutableStateOf(LocalDate.now()) }
    var deadlineTime by remember { mutableStateOf(LocalTime.of(23, 59)) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("in", "ID")) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Tambah Tugas", style = MaterialTheme.typography.titleLarge)
                Text(courseName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFF667eea))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul Tugas") })

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1.5f)) {
                        Text(deadlineDate.format(dateFormatter))
                    }
                    OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.weight(1f)) {
                        Text(deadlineTime.format(timeFormatter))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onAddTask(title, deadlineDate.atTime(deadlineTime)) },
                        enabled = title.isNotBlank()
                    ) {
                        Text("Tambah")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = deadlineDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            deadlineDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = deadlineTime.hour, initialMinute = deadlineTime.minute, is24Hour = true)
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        deadlineTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: Task,
    courseName: String,
    onDismiss: () -> Unit,
    onEditTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var deadlineDate by remember { mutableStateOf(task.deadline.toLocalDate()) }
    var deadlineTime by remember { mutableStateOf(task.deadline.toLocalTime()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("in", "ID")) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Edit Tugas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(courseName, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul Tugas") })

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1.5f)) {
                        Text(deadlineDate.format(dateFormatter))
                    }
                    OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.weight(1f)) {
                        Text(deadlineTime.format(timeFormatter))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Hapus")
                    }

                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val updatedTask = task.copy(title = title, deadline = deadlineDate.atTime(deadlineTime))
                                onEditTask(updatedTask)
                            },
                            enabled = title.isNotBlank()
                        ) {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = deadlineDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            deadlineDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = deadlineTime.hour, initialMinute = deadlineTime.minute, is24Hour = true)
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        deadlineTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    if (showDeleteConfirm) {
        ConfirmationDialog(
            title = "Hapus Tugas?",
            text = "Apakah Anda yakin ingin menghapus tugas ini?",
            onConfirm = {
                onDeleteTask(task)
                showDeleteConfirm = false
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = { Text(text = text) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Hapus")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = containerColor,
            tonalElevation = 6.dp,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.padding(top = 24.dp, bottom = 12.dp, start = 24.dp, end = 24.dp)) {
                    content()
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    UniTrackTheme {
        UniTrackApp()
    }
}
