package com.example.habittracker

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.habittracker.util.updateTodoItem

class TodoDetailActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val widgetManager = AppWidgetManager.getInstance(this)
        val widgetIds = widgetManager.getAppWidgetIds(ComponentName(this, TodoWidget::class.java))
        setContentView(R.layout.activity_todo_detail_view)

        // get the extra data from the intent
        var todoId = intent.getStringExtra("todo-id")
        var todoName = intent.getStringExtra("todo-name")
        var todoStartDate = intent.getStringExtra("todo-start-date")

        // create new to-do id if the to-do id is null
        if (todoId == null) {
            todoId = "todo-id_${(10000000..99999999).random()}"
            Log.i("TODO-DETAIL", "Created new todo id: $todoId")
        }

        Log.i("TODO DETAIL VIEW", "todoName: $todoName todoStartDate: $todoStartDate")
        findViewById<EditText>(R.id.todo_detail_title).setText(todoName)
        findViewById<EditText>(R.id.todo_detail_start_date).setText(todoStartDate)

        // set onclick for save button
        findViewById<Button>(R.id.todo_detail_button_save).setOnClickListener(){

            // get the text from the input fields
            todoName = findViewById<EditText>(R.id.todo_detail_title).text.toString()
            todoStartDate = findViewById<EditText>(R.id.todo_detail_start_date).text.toString()
            makeToast(context = this, "Saved: $todoId - $todoName - $todoStartDate")

            // save the to-do item
            val todoData = mapOf("name" to todoName, "start_datum" to todoStartDate)
            updateTodoItem(context = this, ref = todoId, todoData = todoData)

            // finish the activity
            widgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.todoListView)
            finish()
        }
    }
}