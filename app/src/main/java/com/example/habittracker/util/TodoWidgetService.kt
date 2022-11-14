package com.example.habittracker.util

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.habittracker.R
import com.example.habittracker.data.Todo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TodoWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TodoRemoteViewsFactory(this.applicationContext, intent)
    }
}


class TodoRemoteViewsFactory(val context: Context, val intent: Intent) :
    RemoteViewsService.RemoteViewsFactory {
    private lateinit var todoList: MutableList<String>

    fun initData() {
        val myRef = FirebaseDatabase.getInstance().getReference("To-Do")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (::todoList.isInitialized) {
                    todoList.clear()
                } else {
                    todoList = mutableListOf()
                }
                for (todo in dataSnapshot.children) {
                    val todoName = todo.key
                    todoList.add(todoName!!)
                }
                Log.i("TodoRemoteViewsFactory", "initData: $todoList")
                onDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })
    }


    override fun onCreate() {
        // get data from firebase
        initData()
    }

    override fun onDataSetChanged() {
        Log.i("TodoRemoteViewsFactory", "onDataSetChanged: $todoList")
    }

    override fun onDestroy() {
        // close data source
        Log.i("WIDGET", "onDestroy (TODO)")
    }

    override fun getCount(): Int {
        // check if data is initialized
        val count = if (::todoList.isInitialized) {
            todoList.size
        } else {
            // wait until data is initialized
            while (!::todoList.isInitialized) {
                SystemClock.sleep(100)
            }
            todoList.size
        }
        Log.i("TodoRemoteViewsFactory", "getCount: $count")
        return count
    }

    override fun getViewAt(position: Int): RemoteViews {
        val todoView = RemoteViews(context.packageName, R.layout.todo_list_item)
        todoView.setTextViewText(R.id.todoTextView, todoList[position])
        return todoView
    }

    override fun getLoadingView(): RemoteViews {
        // return a loading view
        return RemoteViews(context.packageName, R.layout.todo_list_item)
    }

    override fun getViewTypeCount(): Int {
        return this.count
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }
}