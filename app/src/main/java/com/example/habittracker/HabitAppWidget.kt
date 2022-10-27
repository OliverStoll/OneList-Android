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
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


/**
 * Implementation of App Widget functionality.
 */

class HabitAppWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        createOnClickListener(context, appWidgetManager, appWidgetIds)
        createFirebaseListeners(context)
        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.app_widget)
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        this.onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    override fun onEnabled(context: Context) {
        //Firebase.database.setPersistenceEnabled(true)
        val widgetManager = AppWidgetManager.getInstance(context)
        val widgetIds = widgetManager.getAppWidgetIds(ComponentName(context, HabitAppWidget::class.java))
        Log.i("MAINACTIVITY", "Enabled - Widget Ids: ${widgetIds}")
        // create the habit clicking option
        createOnClickListener(context, widgetManager, widgetIds)
        createFirebaseListeners(context)
    }

    override fun onDisabled(context: Context) {
        Log.i("MAINACTIVITY", "Widgets disabled")
    }

    override fun onReceive(context: Context, intent: Intent) {

        super.onReceive(context, intent)
        when (intent.action) {
            "TAGEBUCH" -> toggleHabit(context, "Tagebuch")
            "MEDITIEREN" -> toggleHabit(context, "Meditieren")
            "ARBEITEN" -> toggleHabit(context, "Arbeiten")
            "TRAINING" -> toggleHabit(context, "Training")
        }
    }
}

fun toggleHabit(context: Context, habit: String) {
    val database = Firebase.database
    Log.i("ONCLICK", "Clicked: ${habit}")
    val ref = database.getReference("Habits/${habit}/${getDate(0)}")

    ref.get().addOnSuccessListener {
        Log.i("ONCLICK", "Old Value: ${it.value}")
        when (it.value.toString()) {
            "NONE" -> ref.setValue("DONE").addOnSuccessListener { }//createFirebaseListeners(context) }
            "null" -> ref.setValue("DONE").addOnSuccessListener { }//createFirebaseListeners(context) }
            "DONE" -> ref.setValue("FAIL").addOnSuccessListener { }//createFirebaseListeners(context) }
            "FAIL" -> ref.setValue("SKIP").addOnSuccessListener { }//createFirebaseListeners(context) }
            "SKIP" -> ref.setValue("NONE").addOnSuccessListener { }//createFirebaseListeners(context) }
        }
        createFirebaseListeners(context)
    }
}

fun createOnClickListener(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {

    fun getPendingSelfIntent(_context: Context, action: String): PendingIntent? {
        val intent = Intent(_context, HabitAppWidget::class.java)
        intent.action = action
        return PendingIntent.getBroadcast(_context, 0, intent, 0)
    }

    for (widgetId in appWidgetIds) {
        val views = RemoteViews(context.packageName, R.layout.app_widget)

        views.setOnClickPendingIntent(R.id.habitcard_tagebuch, getPendingSelfIntent(context, "TAGEBUCH"))
        views.setOnClickPendingIntent(R.id.habitcard_meditieren, getPendingSelfIntent(context, "MEDITIEREN"))
        views.setOnClickPendingIntent(R.id.habitcard_arbeiten, getPendingSelfIntent(context, "ARBEITEN"))
        views.setOnClickPendingIntent(R.id.habitcard_training, getPendingSelfIntent(context, "TRAINING"))
        appWidgetManager.updateAppWidget(widgetId, views)
        Log.i("ONCLICK ", "Set all onclick pending intents")
    }

}

fun createFirebaseListeners(context: Context) {

    fun createHabitListener(habit: String, database: FirebaseDatabase, buttons: List<Int>, widgetManager: AppWidgetManager, widgetIds: IntArray) {
        val habitRef = database.getReference("Habits/${habit}").limitToLast(7)
        habitRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i("FIREBASE", "Received ${habit}: ${snapshot.value}")
                if (snapshot.value == null) {
                    return
                }
                for (appWidgetId in widgetIds) {
                    updateHabitWidget(context, widgetManager, appWidgetId, buttons=buttons,
                        habitsHashmap = snapshot.value as HashMap<String, String>
                    )
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.i("FIREBASE", "Cancelled $habit")
            }
        })
    }

    // get manager and widget ids from context
    val widgetManager = AppWidgetManager.getInstance(context)
    val widgetIds = widgetManager.getAppWidgetIds(ComponentName(context, HabitAppWidget::class.java))

    // firebase
    Log.i("FIREBASE", "CREATE LISTENERS")
    val habits = listOf("Tagebuch", "Meditieren", "Arbeiten", "Training")
    val database = Firebase.database
    val buttons = mapOf("Tagebuch" to listOf<Int>(R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6, R.id.button7),
                        "Meditieren" to listOf<Int>(R.id.button11, R.id.button12, R.id.button13, R.id.button14, R.id.button15, R.id.button16, R.id.button17),
                        "Arbeiten" to listOf<Int>(R.id.button21, R.id.button22, R.id.button23, R.id.button24, R.id.button25, R.id.button26, R.id.button27),
                        "Training" to listOf<Int>(R.id.button41, R.id.button42, R.id.button43, R.id.button44, R.id.button45, R.id.button46, R.id.button47))
    for (habit in habits){
        createHabitListener(habit, database, buttons[habit]!!, widgetManager, widgetIds)
    }
}

fun updateHabitWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, buttons: List<Int>, habitsHashmap: HashMap<String, String>) {

    val views = RemoteViews(context.packageName, R.layout.app_widget)

    // iterate over all buttons
    for (i in 0 .. 6) {
        val button = buttons.elementAt(i)
        val date = getDate(-(6-i))  // get the days offset inverted
        val day = getDay(-(6-i))
        var icon = R.drawable.circle_none
        if (i == 6){
            icon = R.drawable.circle_open
        }

        // compare the date the button represents against existing dates
        if (date in habitsHashmap.keys){
            val dateValue = habitsHashmap[date]

            when (dateValue) {
                "DONE" -> icon = R.drawable.circle_done
                "SKIP" -> icon = R.drawable.circle_skip
                "FAIL" -> icon = R.drawable.circle_fail
            }
        }

        // set the button background according to the habit state
        views.setInt(button,"setBackgroundResource", icon)
        views.setTextViewText(button, day)
    }

    // update the widget with the new data
    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
}
