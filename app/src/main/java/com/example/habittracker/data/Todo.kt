package com.example.habittracker.data

data class Todo (
    val title: String,
    val description: String? = null,
    val start_date: String? = null,
)