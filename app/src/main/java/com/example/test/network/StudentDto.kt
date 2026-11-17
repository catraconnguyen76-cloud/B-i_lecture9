package com.example.test.network

import com.google.gson.annotations.SerializedName

data class StudentDto(
    @SerializedName("id") val remoteId: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("student_code") val studentCode: String,
    @SerializedName("email") val email: String,
    @SerializedName("gpa") val gpa: Double
)
