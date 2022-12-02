package com.example.habittracker.util

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.habittracker.R
import com.example.habittracker.getDate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class TodoWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        Log.i("TODO WIDGET", "Factory: onGetViewFactory")
        // get gmail data
        return TodoRemoteViewsFactory(this.applicationContext, intent)
    }
}


class TodoRemoteViewsFactory(private val context: Context, val intent: Intent) :
    RemoteViewsService.RemoteViewsFactory {

    // global references
    private val widgetManager = AppWidgetManager.getInstance(context)
    // get the widget ids from the intent
    private val widgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)

    // private val widgetIds = widgetManager.getAppWidgetIds(intent.component)
    private lateinit var todoList: MutableList<Map<String,String?>>
    private var updatingMutex = false

    private fun createDataListener() {
        val myRef = FirebaseDatabase.getInstance().getReference("To-Do")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                updatingMutex = true
                if (::todoList.isInitialized) {
                    todoList.clear()
                } else {
                    todoList = mutableListOf()
                }
                for (todo in dataSnapshot.children) {
                    val todoId = todo.key as String
                    val todoName = todo.child("name").value as String?
                    val todoStartDate = todo.child("start_datum").value as String?
                    val todoData = mapOf("id" to todoId, "name" to todoName, "start_datum" to todoStartDate)
                    todoList.add(todoData)
                }
                Log.i("TODO WIDGET", "Factory: initData: $todoList")
                updatingMutex = false
                widgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.todoListView)
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
        Log.i("TODO WIDGET", "Factory: onDataSetChanged")
    }

    override fun onDestroy() {
        // close data source
        Log.i("TODO WIDGET", "Factory: onDestroy")
    }

    override fun getCount(): Int {
        // if updating or not yet initialized, wait for data
        while (!::todoList.isInitialized || updatingMutex) {
            SystemClock.sleep(100)
        }

        Log.i("TodoRemoteViewsFactory", "getCount: ${todoList.size}")
        return todoList.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        // get a single item view and set the data
        val remoteView = RemoteViews(context.packageName, R.layout.listitem_todolist)
        // check if position is invalid
        if (position >= todoList.size) {
            Log.i("TodoRemoteViewsFactory", "getViewAt: position $position is invalid")
            return remoteView
        }
        val todoItem = todoList[position]
        if (todoItem["name"] == null) {
            Log.i("TodoRemoteViewsFactory", "getViewAt: position $position name is NULL")
            return remoteView
        }
        Log.i("LISTSERVICE", "getViewAt: $position - $todoItem")
        remoteView.setTextViewText(R.id.todoTextView, todoItem["name"])

        // we have to define all special intents (for icon clicks) here, before declaring it generic
        val fillInIntentCheck = Intent()
        val fillInIntentSnooze = Intent()
        val fillInIntentEdit = Intent()
        for (intent in listOf(fillInIntentCheck, fillInIntentSnooze, fillInIntentEdit)) {
            for (key in todoItem.keys) {
                intent.putExtra("todo-$key", todoItem[key])
            }
            val clickType = when (intent) {
                fillInIntentCheck -> "check"
                fillInIntentSnooze -> "snooze"
                fillInIntentEdit -> "edit"
                else -> "unknown"
            }
            intent.putExtra("click-type", clickType)
        }
        remoteView.setOnClickFillInIntent(R.id.todoCheckIcon, fillInIntentCheck)
        remoteView.setOnClickFillInIntent(R.id.todoSnoozeIcon, fillInIntentSnooze)
        remoteView.setOnClickFillInIntent(R.id.todoItem, fillInIntentEdit)

        // make the whole item invisible if there is no data
        if (todoItem["start_datum"] == null) {
            remoteView.setViewVisibility(R.id.todoItem, View.GONE)
        } else if (todoItem["start_datum"] != "" && todoItem["start_datum"]!! > getDate(0, delimiter = "_")) {
            Log.i("TodoRemoteViewsFactory", "Position $position invisible")
            remoteView.setViewVisibility(R.id.todoItem, View.GONE)
        } else {
            remoteView.setViewVisibility(R.id.todoItem, View.VISIBLE)
        }

        return remoteView
    }

    override fun getLoadingView(): RemoteViews {
        // return a loading view
        return RemoteViews(context.packageName, R.layout.listitem_todolist)
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