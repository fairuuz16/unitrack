package com.example.unitrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Assignment
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.unitrack.ui.theme.UniTrackTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

// --- DATA MODELS ---
data class Course(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val lecturer: String,
    val schedule: String
)

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val courseId: String,
    val title: String,
    val deadline: LocalDate,
    var isCompleted: Boolean = false
)

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
    // --- STATE MANAGEMENT ---
    val courses = remember { mutableStateListOf<Course>() }
    val tasks = remember { mutableStateListOf<Task>() }
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf<Course?>(null) }

    // --- INITIAL DATA ---
    LaunchedEffect(Unit) {
        if (courses.isEmpty()) {
            val pbm = Course(name = "Pemrograman Berbasis Mobile", lecturer = "Dr. Udin", schedule = "Rabu, 08:00")
            val pbo = Course(name = "Pemrograman Berorientasi Objek", lecturer = "Prof. Siti", schedule = "Jumat, 13:00")
            courses.addAll(listOf(pbm, pbo))
            tasks.add(Task(courseId = pbm.id, title = "Buat Aplikasi Catatan", deadline = LocalDate.now().plusDays(5)))
            tasks.add(Task(courseId = pbm.id, title = "Implementasi API", deadline = LocalDate.now().plusDays(12), isCompleted = true))
            tasks.add(Task(courseId = pbo.id, title = "Desain Class Diagram", deadline = LocalDate.now().plusDays(7)))
        }
    }

    // Gradient background
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
                            "UniTrack Dashboard",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddCourseDialog = true },
                    containerColor = Color.White,
                    contentColor = Color(0xFF667eea),
                    modifier = Modifier.shadow(8.dp, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Mata Kuliah")
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // --- BAGIAN STATISTIK ---
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

                // --- BAGIAN DAFTAR MATA KULIAH ---
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
                                onAddTaskClicked = { showAddTaskDialog = course },
                                onToggleTask = { taskToToggle ->
                                    val taskIndex = tasks.indexOfFirst { it.id == taskToToggle.id }
                                    if (taskIndex != -1) {
                                        val updatedTask = tasks[taskIndex].copy(isCompleted = !tasks[taskIndex].isCompleted)
                                        tasks[taskIndex] = updatedTask
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddCourseDialog) {
        AddCourseDialog(
            onDismiss = { showAddCourseDialog = false },
            onAddCourse = { name, lecturer, schedule ->
                courses.add(Course(name = name, lecturer = lecturer, schedule = schedule))
                showAddCourseDialog = false
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

    // Animated progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = EaseOutCubic)
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
            // Subtle gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                        progress = animatedProgress,
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
                    horizontalArrangement = Arrangement.SpaceBetween
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
fun StatItem(
    icon: ImageVector,
    title: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
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
    onToggleTask: (Task) -> Unit
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
            // Header with gradient accent
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
                    .padding(16.dp)
            ) {
                Column {
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
                    modifier = Modifier.shadow(4.dp, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Tambah Tugas",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Tugas", fontWeight = FontWeight.Medium)
                }
            }

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🎉 Hore, tidak ada tugas!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF48BB78),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    tasks.sortedBy { it.isCompleted }.forEach { task ->
                        TaskItem(task = task, onToggle = { onToggleTask(task) })
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
            modifier = Modifier.size(18.dp),
            tint = Color(0xFF667eea)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF4A5568)
        )
    }
}

@Composable
fun TaskItem(task: Task, onToggle: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("in", "ID"))
    val isOverdue = task.deadline.isBefore(LocalDate.now()) && !task.isCompleted

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted)
                Color(0xFF48BB78).copy(alpha = 0.1f)
            else
                Color(0xFFF7FAFC)
        ),
        border = BorderStroke(
            1.dp,
            if (task.isCompleted) Color(0xFF48BB78).copy(alpha = 0.3f)
            else Color(0xFFE2E8F0)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF48BB78),
                    uncheckedColor = Color(0xFF667eea)
                )
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.isCompleted)
                        androidx.compose.ui.text.style.TextDecoration.LineThrough
                    else null,
                    color = if (task.isCompleted)
                        Color.Gray
                    else
                        Color(0xFF2D3748)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Deadline: ${task.deadline.format(formatter)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        task.isCompleted -> Color.Gray
                        isOverdue -> Color(0xFFE53E3E)
                        else -> Color(0xFF667eea)
                    },
                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun AddCourseDialog(
    onDismiss: () -> Unit,
    onAddCourse: (name: String, lecturer: String, schedule: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var lecturer by remember { mutableStateOf("") }
    var schedule by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            modifier = Modifier.shadow(20.dp, RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Mata Kuliah Baru",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Mata Kuliah") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF667eea),
                        focusedLabelColor = Color(0xFF667eea)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = lecturer,
                    onValueChange = { lecturer = it },
                    label = { Text("Nama Dosen") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF667eea),
                        focusedLabelColor = Color(0xFF667eea)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = schedule,
                    onValueChange = { schedule = it },
                    label = { Text("Jadwal (e.g. Senin, 10:00)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF667eea),
                        focusedLabelColor = Color(0xFF667eea)
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && lecturer.isNotBlank() && schedule.isNotBlank()) {
                                onAddCourse(name, lecturer, schedule)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF667eea)
                        )
                    ) {
                        Text("Tambah", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    courseName: String,
    onDismiss: () -> Unit,
    onAddTask: (title: String, deadline: LocalDate) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var deadlineStr by remember { mutableStateOf(LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE)) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            modifier = Modifier.shadow(20.dp, RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Tugas Baru",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF667eea)
                )
                Text(
                    courseName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Tugas") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF667eea),
                        focusedLabelColor = Color(0xFF667eea)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = deadlineStr,
                    onValueChange = { deadlineStr = it },
                    label = { Text("Deadline (YYYY-MM-DD)") },
                    supportingText = {
                        Text("Contoh: ${LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE)}")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF667eea),
                        focusedLabelColor = Color(0xFF667eea)
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val deadline = try {
                                LocalDate.parse(deadlineStr)
                            } catch (e: Exception) {
                                LocalDate.now().plusDays(7)
                            }
                            if (title.isNotBlank()) {
                                onAddTask(title, deadline)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF667eea)
                        )
                    ) {
                        Text("Tambah", fontWeight = FontWeight.Bold)
                    }
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