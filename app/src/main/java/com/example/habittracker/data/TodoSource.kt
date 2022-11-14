package com.example.habittracker.data

fun getTodos() : List<String> {
    // create a list of three todos
    val todo1 = Todo(
        title = "Learn Kotlin",
        description = "Kotlin is a great language to learn",
        start_date = "2021-01-01",
    )
    val todo2 = Todo(
        title = "Learn Android",
    )
    val todo3 = Todo(
        title = "Learn Firebase",
    )

    // add the todos to the list
    val todoList = mutableListOf<String>(todo1.toString(), todo2.toString(), todo3.toString())

    return todoList
}