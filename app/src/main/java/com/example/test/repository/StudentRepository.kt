package com.example.test.repository

import com.example.test.data.StudentDao
import com.example.test.model.Student
import com.example.test.network.StudentApiService
import com.example.test.network.StudentDto

class StudentRepository(private val dao: StudentDao, private val api: StudentApiService) {
    val students = dao.getAllStudents()
    suspend fun insert(student: Student) = dao.insert(student)
    suspend fun update(student: Student) = dao.update(student)
    suspend fun delete(student: Student) = dao.delete(student)
    suspend fun addDefaultStudentIfEmpty() {
        if (dao.countStudents() == 0) {
            dao.insert(
                Student(
                    fullName = "Đặng Thái Nguyên",
                    studentCode = "2415053122224",
                    email = "",
                    gpa = 0.0
                )
            )
        }
    }

    suspend fun syncFromServer() {
        api.getStudents().forEach { remote ->
            val existing = dao.findByRemoteId(remote.remoteId)
            dao.upsert(remote.toStudent(id = existing?.id ?: 0))
        }
    }

    suspend fun createOnServer(student: Student) {
        val created = api.createStudent(student.toDto())
        dao.upsert(created.toStudent(id = student.id))
    }

    private fun StudentDto.toStudent(id: Int = 0) = Student(
        id = id,
        fullName = fullName,
        studentCode = studentCode,
        email = email,
        gpa = gpa,
        remoteId = remoteId
    )

    private fun Student.toDto() = StudentDto(
        remoteId = remoteId.orEmpty(),
        fullName = fullName,
        studentCode = studentCode,
        email = email,
        gpa = gpa
    )
}
