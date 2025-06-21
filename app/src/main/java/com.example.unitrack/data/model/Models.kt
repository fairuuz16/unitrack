package com.example.unitrack.data.model

import java.time.LocalDateTime
import java.util.UUID

data class Course(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val lecturer: String,
    val schedule: String,
    val dayOfWeek: Int, // 1 for Monday, 7 for Sunday
    val time: String
)

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val courseId: String,
    val title: String,
    val deadline: LocalDateTime,
    var isCompleted: Boolean = false
)
