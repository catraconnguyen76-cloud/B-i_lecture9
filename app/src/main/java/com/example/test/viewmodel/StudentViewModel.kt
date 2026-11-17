package com.example.test.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.model.Student
import com.example.test.repository.StudentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudentViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage = _syncMessage.asStateFlow()
    val students = repository.students.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) { repository.addDefaultStudentIfEmpty() }
    }

    fun save(student: Student) = viewModelScope.launch(Dispatchers.IO) {
        try {
            if (student.id == 0) repository.insert(student) else repository.update(student)
            _syncMessage.value = "Đã lưu thông tin sinh viên trên máy"
        } catch (error: Exception) {
            _syncMessage.value = "Lưu không thành công: ${error.localizedMessage ?: "lỗi cơ sở dữ liệu"}"
        }
    }

    fun delete(student: Student) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(student)
    }

    fun syncFromServer() = viewModelScope.launch(Dispatchers.IO) {
        _syncMessage.value = "Đang đồng bộ..."
        try {
            repository.syncFromServer()
            _syncMessage.value = "Đồng bộ thành công"
        } catch (error: Exception) {
            _syncMessage.value = "Không kết nối được máy chủ. Đang hiển thị dữ liệu đã lưu trên máy."
        }
    }

    fun uploadToServer(student: Student) = viewModelScope.launch(Dispatchers.IO) {
        _syncMessage.value = "Đang gửi ${student.fullName} lên máy chủ..."
        try {
            repository.createOnServer(student)
            _syncMessage.value = "Đã gửi sinh viên lên máy chủ"
        } catch (error: Exception) {
            _syncMessage.value = "Gửi thất bại. Dữ liệu vẫn được giữ trong Room trên máy."
        }
    }
}
