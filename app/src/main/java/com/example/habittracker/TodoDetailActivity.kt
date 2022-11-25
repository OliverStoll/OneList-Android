package com.example.habittracker

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import com.example.habittracker.util.updateTodoItem

class TodoDetailActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val widgetManager = AppWidgetManager.getInstance(this)
        val widgetIds = widgetManager.getAppWidgetIds(ComponentName(this, TodoWidget::class.java))
        setContentView(R.layout.activity_todoitem_detailview)

        // get the input fields
        val todoText = findViewById<EditText>(R.id.todo_detail_title)
        val startDateText = findViewById<EditText>(R.id.todo_detail_start_date)
        val startDatePicker = findViewById<DatePicker>(R.id.todo_detail_start_date_picker)

        // get the extra data from the intent
        var todoId = intent.getStringExtra("id")
        var todoName = intent.getStringExtra("name")
        var todoStartDate = intent.getStringExtra("start_datum")
        Log.i("TODO DETAIL VIEW", "todoName: $todoName todoStartDate: $todoStartDate")

        // set the text of the edit text fields
        todoText.setText(todoName)
        startDateText.setText(todoStartDate)

        // set the date picker to the start date
        if (todoStartDate != null && todoStartDate != "") {
            val startDate = todoStartDate.split("-")
            startDatePicker.updateDate(startDate[0].toInt(), startDate[1].toInt() - 1, startDate[2].toInt())
        }



        // create new to-do id if the to-do id is null
        if (todoId == null) {
            todoId = "t-${(10000000..99999999).random()}"
            Log.i("TODO-DETAIL", "Created new todo id: $todoId")
            // make todo_detail_title focused
            findViewById<EditText>(R.id.todo_detail_title).requestFocus()
        } else {
            // set focus to the end of the input text
            startDateText.requestFocus()
            startDateText.setSelection(startDateText.text.length)
        }

        // set onclick for save button
        findViewById<Button>(R.id.todo_detail_button_save).setOnClickListener(){
            // get the text from the input fields
            todoName = todoText.text.toString()
            // trim to-do name whitespace
            if (todoName != null) {
                todoName = todoName!!.trim()
            }
            // get the start date from the date picker
            todoStartDate = "${startDatePicker.year}-${startDatePicker.month + 1}-${startDatePicker.dayOfMonth}"
            makeToast(context = this, "Saved: $todoId - $todoName - $todoStartDate")

            // save the to-do item
            val todoData = mutableMapOf("id" to todoId, "name" to todoName, "start_datum" to todoStartDate)
            updateTodoItem(newTodoData = todoData)

            // finish the activity
            finish()
        }
    }
}