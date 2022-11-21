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
import android.widget.Toast
import com.example.habittracker.util.TodoWidgetService
import com.example.habittracker.util.deleteTodoItem


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

        val widgetManager = AppWidgetManager.getInstance(context)
        val widgetIds = widgetManager.getAppWidgetIds(ComponentName(context, TodoWidget::class.java))

        val clickType = intent.getStringExtra("click-type")
        val todoId = intent.getStringExtra("todo-id")
        val todoName = intent.getStringExtra("todo-name")
        val todoStartDate = intent.getStringExtra("todo-start-date")

        if (intent.action == "TODO-CLICK") {
            // handle the click event if the user clicks on the widget
            Log.i("ONCLICK", "Clicked: $todoName $clickType")
            Toast.makeText(context, "Clicked: $todoName $clickType", Toast.LENGTH_SHORT).show()

            // show activity if click-type is "edit"
            if (clickType == "edit") {
                val editIntent = Intent(context, TodoDetailActivity::class.java)
                editIntent.putExtra("todo-id", todoId)
                editIntent.putExtra("todo-name", todoName)
                editIntent.putExtra("todo-start-date", todoStartDate)
                editIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(editIntent)
            }
            else if (clickType == "check") {
                deleteTodoItem(context=context, ref=todoId!!)
                widgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.todoListView)
            }
        }
        else if (intent.action == "TODO-ADD") {
            makeToast(context, "Clicked: ${intent.action}")
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
    val remoteView = RemoteViews(context.packageName, R.layout.todo_widget)

    // set the remoteViewFactory as adapter for the listview (using it to populate the listview)
    val serviceIntent = Intent(context, TodoWidgetService::class.java)
    serviceIntent.data = android.net.Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))
    remoteView.setRemoteAdapter(R.id.todoListView, serviceIntent)

    // set the click intent for the add button
    remoteView.setOnClickPendingIntent(R.id.button_add_todo, createPendingIntent(context=context, action="TODO-ADD", widget=TodoWidget::class.java))

    // create a pending intent template for the listview items
    val clickIntent = Intent(context, TodoWidget::class.java)
    clickIntent.action = "TODO-CLICK"
    val clickPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    remoteView.setPendingIntentTemplate(R.id.todoListView, clickPendingIntent)

    // Update the widget
    for (widgetId in widgetIds) {
        widgetManager.partiallyUpdateAppWidget(widgetId, remoteView)
    }

}