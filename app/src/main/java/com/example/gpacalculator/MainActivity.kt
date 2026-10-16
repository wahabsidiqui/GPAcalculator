package com.example.gpacalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GpaTheme {
                GpaApp()
            }
        }
    }
}

private fun fmt(value: Double?): String = value?.let { "%.2f".format(it) } ?: "--"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpaApp(vm: GpaViewModel = viewModel()) {
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = { TopAppBar(title = { Text("GPA Calculator") }) },
        bottomBar = { ResultBar(vm.semester, vm.cumulative) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { PreviousRecordCard(vm) }

            items(vm.courses, key = { it.id }) { course ->
                CourseCard(
                    course = course,
                    onChange = { vm.update(it) },
                    onDelete = { vm.remove(course.id) }
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { vm.addCourse() }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add course")
                    }
                    OutlinedButton(onClick = { vm.reset() }) { Text("Reset") }
                }
            }
        }
    }
}

@Composable
fun PreviousRecordCard(vm: GpaViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Previous record (optional)", style = MaterialTheme.typography.titleSmall)
            Text(
                "Fill these in to also see your cumulative GPA.",
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = vm.prevGpa,
                    onValueChange = { if (isDecimal(it)) vm.prevGpa = it },
                    label = { Text("Previous GPA") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = vm.prevCredits,
                    onValueChange = { if (isDecimal(it)) vm.prevCredits = it },
                    label = { Text("Credits so far") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CourseCard(
    course: Course,
    onChange: (Course) -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = course.name,
                    onValueChange = { onChange(course.copy(name = it)) },
                    label = { Text("Course name") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete course")
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = course.credits,
                    onValueChange = { if (isDecimal(it)) onChange(course.copy(credits = it)) },
                    label = { Text("Credits") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                GradeDropdown(
                    selected = course.grade,
                    onSelected = { onChange(course.copy(grade = it)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun GradeDropdown(
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Grade: $selected")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            gradePoints.forEach { (grade, points) ->
                DropdownMenuItem(
                    text = { Text("$grade  ($points)") },
                    onClick = {
                        onSelected(grade)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ResultBar(semester: GpaResult?, cumulative: GpaResult?) {
    Surface(tonalElevation = 3.dp, color = MaterialTheme.colorScheme.primaryContainer) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Semester GPA", style = MaterialTheme.typography.labelLarge)
                Text(fmt(semester?.gpa), style = MaterialTheme.typography.headlineLarge)
                Text(
                    "Credits: ${semester?.totalCredits?.let { "%.1f".format(it) } ?: "0"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (cumulative != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("Cumulative GPA", style = MaterialTheme.typography.labelLarge)
                    Text(fmt(cumulative.gpa), style = MaterialTheme.typography.headlineLarge)
                    Text(
                        "Credits: ${"%.1f".format(cumulative.totalCredits)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

/** Allows only digits and a single decimal point. */
private fun isDecimal(s: String): Boolean =
    s.all { it.isDigit() || it == '.' } && s.count { it == '.' } <= 1
