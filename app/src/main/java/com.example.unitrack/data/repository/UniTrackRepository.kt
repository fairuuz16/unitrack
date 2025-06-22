package com.example.unitrack.data.repository

import com.example.unitrack.data.model.Course
import com.example.unitrack.data.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalDateTime

class UniTrackRepository {

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    init {
        if (_courses.value.isEmpty()) {
            val pbm = Course(name = "Pemrograman Perangkat Bergerak", lecturer = "Fajar Baskoro, S.Kom., M.T.", schedule = "Senin, 13.30", dayOfWeek = 1, time = "13.30")
            val pbo = Course(name = "Pemrograman Jaringan", lecturer = "Royyana M.I, S.Kom.,M.Kom., Ph.D.", schedule = "Selasa, 10.00", dayOfWeek = 2, time = "13:00")
            val paa = Course(name = "Perancangan dan Analisis Algoritma", lecturer = "Misbakhul M.I.S, S.Kom., M.Sc.", schedule = "Rabu, 07.00", dayOfWeek = 3, time = "10:00")
            _courses.value = listOf(pbm, pbo, paa)

            _tasks.value = listOf(
                Task(courseId = pbm.id, title = "Buat Aplikasi Catatan", deadline = LocalDate.now().plusDays(5).atTime(23, 59)),
                Task(courseId = pbm.id, title = "Implementasi API", deadline = LocalDate.now().plusDays(12).atTime(18, 0), isCompleted = true),
                Task(courseId = pbo.id, title = "Desain Final Project", deadline = LocalDate.now().plusDays(7).atTime(23, 59))
            )
        }
    }

    fun addCourse(course: Course) = _courses.update { it + course }

    fun updateCourse(updatedCourse: Course) {
        _courses.update { courses -> courses.map { if (it.id == updatedCourse.id) updatedCourse else it } }
    }

    fun deleteCourse(courseId: String) {
        _courses.update { courses -> courses.filterNot { it.id == courseId } }
        _tasks.update { tasks -> tasks.filterNot { it.courseId == courseId } }
    }

    fun addTask(task: Task) = _tasks.update { it + task }

    fun updateTask(updatedTask: Task) {
        _tasks.update { tasks -> tasks.map { if (it.id == updatedTask.id) updatedTask else it } }
    }

    fun deleteTask(taskId: String) {
        _tasks.update { tasks -> tasks.filterNot { it.id == taskId } }
    }

    fun toggleTaskCompletion(taskId: String) {
        _tasks.update { tasks ->
            tasks.map {
                if (it.id == taskId) it.copy(isCompleted = !it.isCompleted) else it
            }
        }
    }
}
