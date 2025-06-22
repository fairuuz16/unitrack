package com.example.unitrack.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.unitrack.data.model.Course
import com.example.unitrack.data.model.Task
import com.example.unitrack.ui.components.CalendarWidget
import com.example.unitrack.ui.components.ScheduleForDate
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarScreen(
    courses: List<Course>,
    tasks: List<Task>
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
