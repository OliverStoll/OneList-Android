package com.example.habittracker

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class TodoDetailView : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo_detail_view)
    }
}