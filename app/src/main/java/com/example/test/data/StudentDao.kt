package com.example.test.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import androidx.room.Update
import com.example.test.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: Student)

    @Upsert
    suspend fun upsert(student: Student)

    @Query("SELECT * FROM students_table WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findByRemoteId(remoteId: String): Student?

    @Update
    suspend fun update(student: Student)

    @Delete
    suspend fun delete(student: Student)

    @Query("SELECT * FROM students_table ORDER BY id DESC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT COUNT(*) FROM students_table")
    suspend fun countStudents(): Int
}
