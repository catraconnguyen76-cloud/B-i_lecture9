package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.test.data.AppDatabase
import com.example.test.model.Student
import com.example.test.network.RetrofitClient
import com.example.test.repository.StudentRepository
import com.example.test.ui.theme.TestTheme
import com.example.test.viewmodel.StudentViewModel

class MainActivity : ComponentActivity() {
    private val studentViewModel: StudentViewModel by viewModels {
        val repository = StudentRepository(
            AppDatabase.getDatabase(applicationContext).studentDao(),
            RetrofitClient.studentApi
        )
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                StudentViewModel(repository) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestTheme {
                StudentScreen(studentViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentScreen(viewModel: StudentViewModel) {
    val students by viewModel.students.collectAsState()
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Quản lý sinh viên") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editingStudent = null; showDialog = true }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Danh sách (${students.size})", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { viewModel.syncFromServer() }) { Text("Đồng bộ API") }
            }
            val syncMessage by viewModel.syncMessage.collectAsState()
            syncMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            Spacer(Modifier.height(12.dp))
            if (students.isEmpty()) {
                Text("Chưa có sinh viên. Nhấn + để thêm mới.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(students, key = { it.id }) { student ->
                        StudentCard(
                            student = student,
                            onEdit = { editingStudent = student; showDialog = true },
                            onDelete = { viewModel.delete(student) },
                            onUpload = { viewModel.uploadToServer(student) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        StudentDialog(
            student = editingStudent,
            onDismiss = { showDialog = false },
            onSave = { viewModel.save(it); showDialog = false }
        )
    }
}

@Composable
private fun StudentCard(student: Student, onEdit: () -> Unit, onDelete: () -> Unit, onUpload: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(student.fullName, style = MaterialTheme.typography.titleMedium)
            Text("Mã sinh viên: ${student.studentCode}")
            Text("Email: ${student.email.ifBlank { "Chưa có" }}")
            Text("GPA: ${student.gpa}")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onUpload) { Text("Gửi API") }
                TextButton(onClick = onEdit) { Text("Sửa") }
                TextButton(onClick = onDelete) { Text("Xóa") }
            }
        }
    }
}

@Composable
private fun StudentDialog(student: Student?, onDismiss: () -> Unit, onSave: (Student) -> Unit) {
    var name by remember(student) { mutableStateOf(student?.fullName.orEmpty()) }
    var code by remember(student) { mutableStateOf(student?.studentCode.orEmpty()) }
    var email by remember(student) { mutableStateOf(student?.email.orEmpty()) }
    var gpaText by remember(student) { mutableStateOf(student?.gpa?.toString() ?: "0.0") }
    var formError by remember(student) { mutableStateOf<String?>(null) }
    val gpa = gpaText.toDoubleOrNull()
    val valid = name.isNotBlank() && code.isNotBlank() && gpa != null && gpa in 0.0..4.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (student == null) "Thêm sinh viên" else "Sửa sinh viên") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(name, { name = it }, label = { Text("Họ tên") }, singleLine = true)
                OutlinedTextField(code, { code = it }, label = { Text("Mã sinh viên") }, singleLine = true)
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true)
                OutlinedTextField(
                    value = gpaText,
                    onValueChange = { gpaText = it },
                    label = { Text("GPA (0–4)") },
                    supportingText = { Text("Bắt buộc là số từ 0 đến 4") },
                    singleLine = true
                )
                formError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                formError = when {
                    name.isBlank() -> "Vui lòng nhập họ tên."
                    code.isBlank() -> "Vui lòng nhập mã sinh viên."
                    gpa == null || gpa !in 0.0..4.0 -> "GPA phải là số từ 0 đến 4, ví dụ 3.2."
                    else -> null
                }
                if (valid) {
                    onSave(Student(student?.id ?: 0, name.trim(), code.trim(), email.trim(), gpa!!))
                }
            }) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}
