package com.example.habittracker.data


// create a data class with a day string and otherwise optional values
data class HabitDay (
    val datum: String? = null,
    val status: String? = null,
    val minuten: Int? = null,
)
