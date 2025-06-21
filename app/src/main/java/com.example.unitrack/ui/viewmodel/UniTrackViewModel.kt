package com.example.unitrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.unitrack.data.model.Course
import com.example.unitrack.data.model.Task
import com.example.unitrack.data.repository.UniTrackRepository
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime

data class UniTrackUiState(
    val courses: List<Course> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = true
)

class UniTrackViewModel(private val repository: UniTrackRepository) : ViewModel() {

    val uiState: StateFlow<UniTrackUiState> = combine(
        repository.courses,
        repository.tasks
    ) { courses, tasks ->
        UniTrackUiState(courses, tasks, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UniTrackUiState()
    )

    fun addCourse(name: String, lecturer: String, schedule: String, dayOfWeek: Int, time: String) {
        val newCourse = Course(name = name, lecturer = lecturer, schedule = schedule, dayOfWeek = dayOfWeek, time = time)
        repository.addCourse(newCourse)
    }

    fun updateCourse(course: Course) = repository.updateCourse(course)

    fun deleteCourse(course: Course) = repository.deleteCourse(course.id)

    fun addTask(courseId: String, title: String, deadline: LocalDateTime) {
        val newTask = Task(courseId = courseId, title = title, deadline = deadline)
        repository.addTask(newTask)
    }

    fun updateTask(task: Task) = repository.updateTask(task)

    fun deleteTask(task: Task) = repository.deleteTask(task.id)

    fun toggleTask(task: Task) = repository.toggleTaskCompletion(task.id)
}

class ViewModelFactory(private val repository: UniTrackRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UniTrackViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UniTrackViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
