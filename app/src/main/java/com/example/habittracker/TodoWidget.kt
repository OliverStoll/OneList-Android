package com.example.habittracker

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.example.habittracker.util.TodoWidgetService
import com.example.habittracker.util.deleteTodoItem
import com.example.habittracker.util.snoozeTodoItem





class TodoWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        updateTodoWidgets(context=context)
    }

    override fun onAppWidgetOptionsChanged(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetId: Int, newOptions: Bundle?) {
        // update the widget when the widget size changes
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        this.onUpdate(context!!, appWidgetManager!!, intArrayOf(appWidgetId))
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
        updateTodoWidgets(context=context)
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == "TODO-CLICK" || intent.action == "TODO-ADD") {
            Log.i("ONCLICK", "Clicked: ${intent.action}")
        } else {
            Log.i("onReceive", "Received other: ${intent.action}")
            return
        }

        val widgetManager = AppWidgetManager.getInstance(context)
        val widgetIds = widgetManager.getAppWidgetIds(ComponentName(context, TodoWidget::class.java))

        val clickType = intent.getStringExtra("click-type")
        val todoData = mutableMapOf(
            "id" to intent.getStringExtra("todo-id"),
            "name" to intent.getStringExtra("todo-name"),
            "start_datum" to intent.getStringExtra("todo-start_datum")
        )

        // handle all clicks
        if (intent.action == "TODO-CLICK" && clickType == "edit") {
            val editIntent = Intent(context, TodoDetailActivity::class.java)
            editIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            for ((key, value) in todoData) {
                editIntent.putExtra(key, value)
            }
            context.startActivity(editIntent)
        }
        else if (intent.action == "TODO-CLICK" && clickType == "check") {
            deleteTodoItem(ref=todoData["id"]!!)
        }
        else if (intent.action == "TODO-CLICK" && clickType == "snooze") {
            snoozeTodoItem(todoData=todoData)
        }
        else if (intent.action == "TODO-ADD") {
            // start the TodoDetailActivity
            val addIntent = Intent(context, TodoDetailActivity::class.java)
            addIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(addIntent)
        }
    }
}

internal fun updateTodoWidgets(
    context: Context,
) {
    Log.i("TODO WIDGET", "Updating widgets")

    // get global references
    val widgetManager = AppWidgetManager.getInstance(context)
    val widgetIds = widgetManager.getAppWidgetIds(ComponentName(context, TodoWidget::class.java))
    val remoteView = RemoteViews(context.packageName, R.layout.widget_todolist)

    // set the click intent for the add button
    remoteView.setOnClickPendingIntent(R.id.button_add_todo, createPendingIntent(context=context, action="TODO-ADD", widget=TodoWidget::class.java))

    // set the remoteViewFactory as adapter for the listview (using it to populate the listview)
    val serviceIntent = Intent(context, TodoWidgetService::class.java)
    // add the appWidgetIds to the intent
    serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
    serviceIntent.data = android.net.Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))
    remoteView.setRemoteAdapter(R.id.todoListView, serviceIntent)

    // create a pending intent template for the listview items
    val clickIntent = Intent(context, TodoWidget::class.java)
    clickIntent.action = "TODO-CLICK"

    val clickPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    remoteView.setPendingIntentTemplate(R.id.todoListView, clickPendingIntent)

    // update the listview
    widgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.todoListView)

    // Update the widget
    for (widgetId in widgetIds) {
        widgetManager.partiallyUpdateAppWidget(widgetId, remoteView)
    }

}