package com.example.habittracker.util

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.habittracker.R
import com.example.habittracker.makeToast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TodoWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        Log.i("TODO WIDGET", "Factory: onGetViewFactory")
        return TodoRemoteViewsFactory(this.applicationContext, intent)
    }
}


class TodoRemoteViewsFactory(private val context: Context, val intent: Intent) :
    RemoteViewsService.RemoteViewsFactory {
    private lateinit var todoList: MutableList<Map<String,String?>>

    private fun createDataListener() {
        Log.i("TODO WIDGET", "Factory: createDataListener")
        val myRef = FirebaseDatabase.getInstance().getReference("To-Do")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (::todoList.isInitialized) {
                    todoList.clear()
                } else {
                    todoList = mutableListOf()
                }
                for (todo in dataSnapshot.children) {
                    val todoId = todo.key as String
                    val todoName = todo.child("name").value as String
                    val todoStartDate = todo.child("start_datum").value as String?
                    val todoData = mapOf("id" to todoId, "name" to todoName, "start_datum" to todoStartDate)
                    todoList.add(todoData)
                }
                Log.i("TODO WIDGET", "Factory: initData: $todoList")
            }
            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })
    }

    override fun onCreate() {
        if (::todoList.isInitialized) {
            Log.i("TODO WIDGET", "Factory: onCreate: $todoList")
        } else {
            Log.i("TODO WIDGET", "Factory: onCreate: todoList not initialized")
        }
        // get data from firebase
        createDataListener()
    }

    override fun onDataSetChanged() {
        if (::todoList.isInitialized) {
            Log.i("TODO WIDGET", "Factory: onDataSetChanged: $todoList")
        } else {
            Log.i("TODO WIDGET", "Factory: onDataSetChanged: todoList not initialized")
        }
        // get data from firebase
        createDataListener()
    }

    override fun onDestroy() {
        // close data source
        Log.i("TODO WIDGET", "Factory: onDestroy")
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
        // get a single item view and set the data
        val remoteView = RemoteViews(context.packageName, R.layout.todo_list_item)
        Log.i("LISTSERVICE", "getViewAt: $position with name ${todoList[position]["name"]}")
        remoteView.setTextViewText(R.id.todoTextView, todoList[position]["name"])



        val fillInIntentCheckbox = Intent()
        fillInIntentCheckbox.putExtra("todo-id", todoList[position]["id"])
        fillInIntentCheckbox.putExtra("todo-name", todoList[position]["name"])
        fillInIntentCheckbox.putExtra("click-type", "check")
        remoteView.setOnClickFillInIntent(R.id.todoCheckBox, fillInIntentCheckbox)
        val fillInIntent = Intent()
        fillInIntent.putExtra("todo-id", todoList[position]["id"])
        fillInIntent.putExtra("todo-name", todoList[position]["name"])
        fillInIntent.putExtra("click-type", "edit")
        remoteView.setOnClickFillInIntent(R.id.todo_item, fillInIntent)

        return remoteView
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
        return true
    }
}