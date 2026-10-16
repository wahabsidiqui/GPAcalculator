package com.example.gpacalculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class Course(
    val id: Int,
    val name: String = "",
    val credits: String = "3",
    val grade: String = "A"
)

data class GpaResult(val gpa: Double, val totalCredits: Double)

// Edit these values if your university uses a different scale
val gradePoints = linkedMapOf(
    "A" to 4.0, "A-" to 3.67,
    "B+" to 3.33, "B" to 3.0, "B-" to 2.67,
    "C+" to 2.33, "C" to 2.0, "C-" to 1.67,
    "D+" to 1.33, "D" to 1.0,
    "F" to 0.0
)

fun calculateGpa(courses: List<Course>): GpaResult? {
    var totalPoints = 0.0
    var totalCredits = 0.0
    for (c in courses) {
        val credits = c.credits.toDoubleOrNull() ?: continue
        if (credits <= 0) continue
        totalPoints += credits * (gradePoints[c.grade] ?: 0.0)
        totalCredits += credits
    }
    return if (totalCredits == 0.0) null else GpaResult(totalPoints / totalCredits, totalCredits)
}

class GpaViewModel : ViewModel() {
    val courses = mutableStateListOf(Course(0), Course(1), Course(2))
    private var nextId = 3

    // Optional: previous record, used for cumulative GPA
    var prevGpa by mutableStateOf("")
    var prevCredits by mutableStateOf("")

    val semester: GpaResult?
        get() = calculateGpa(courses)

    val cumulative: GpaResult?
        get() {
            val sem = semester ?: return null
            val pg = prevGpa.toDoubleOrNull() ?: return null
            val pc = prevCredits.toDoubleOrNull() ?: return null
            if (pc <= 0) return null
            val total = pc + sem.totalCredits
            return GpaResult((pg * pc + sem.gpa * sem.totalCredits) / total, total)
        }

    fun addCourse() {
        courses.add(Course(nextId++))
    }

    fun update(course: Course) {
        val i = courses.indexOfFirst { it.id == course.id }
        if (i >= 0) courses[i] = course
    }

    fun remove(id: Int) {
        courses.removeAll { it.id == id }
    }

    fun reset() {
        courses.clear()
        repeat(3) { addCourse() }
        prevGpa = ""
        prevCredits = ""
    }
}
