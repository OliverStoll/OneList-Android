package com.example.habittracker

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.habittracker.util.TodoWidgetService

/**
 * Implementation of App Widget functionality.
 */
class TodoWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.todo_widget)

    // create a remoteviewfactory
    val serviceIntent = Intent(context, TodoWidgetService::class.java)
    serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)  // extra to support multiple widgets
    serviceIntent.data = android.net.Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))
    views.setRemoteAdapter(R.id.todoListView, serviceIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
}