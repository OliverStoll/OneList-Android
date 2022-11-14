package com.example.habittracker

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.example.habittracker.util.createAllHabitListeners
import com.example.habittracker.util.createHabitListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.Float.max
import java.lang.Float.min
import kotlin.math.max
import kotlin.math.min


/** Global variables **/

val numberHabits = 7

val minutesTotal = 225.0
val circlesDrawables = listOf(R.drawable.circle_none, R.drawable.circle_05, R.drawable.circle_10, R.drawable.circle_15, R.drawable.circle_20, R.drawable.circle_25, R.drawable.circle_30, R.drawable.circle_35, R.drawable.circle_40, R.drawable.circle_45, R.drawable.circle_50, R.drawable.circle_55, R.drawable.circle_60, R.drawable.circle_65, R.drawable.circle_70, R.drawable.circle_75, R.drawable.circle_80, R.drawable.circle_85, R.drawable.circle_90, R.drawable.circle_95, R.drawable.circle_done)

// Habit names and button ids
val habitsDict = mapOf(
    "Tagebuch" to mapOf("buttons" to listOf(R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6, R.id.button7),),
    "Meditieren" to mapOf("buttons" to listOf(R.id.button11, R.id.button12, R.id.button13, R.id.button14, R.id.button15, R.id.button16, R.id.button17),),
    "Arbeiten" to mapOf("buttons" to listOf(R.id.button21, R.id.button22, R.id.button23, R.id.button24, R.id.button25, R.id.button26, R.id.button27),),
    "Training" to mapOf("default" to R.drawable.circle_none,"buttons" to listOf(R.id.button41, R.id.button42, R.id.button43, R.id.button44, R.id.button45, R.id.button46, R.id.button47))
)



/** Implementation of App Widget functionality. **/


class HabitAppWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        createOnClickListeners(context)
        createAllHabitListeners(context)
        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.habit_widget)
            appWidgetManager.partiallyUpdateAppWidget(widgetId, views)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        // update the widget when the widget size changes
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        this.onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    override fun onEnabled(context: Context) {
        //Firebase.database.setPersistenceEnabled(true)
        Log.i("MAINACTIVITY", "WIDGETS ENABLED")
        createOnClickListeners(context)
        createAllHabitListeners(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // handle the click event if the user clicks on the widget
        if (intent.action in listOf("Tagebuch", "Meditieren", "Arbeiten", "Training")) {
            handleHabitClick(context, intent.action!!)
        }
    }

    override fun onDisabled(context: Context) {
        Log.i("MAINACTIVITY", "WIDGETS DISABLED")
    }
}


/** EXTERNAL FUNCTIONS **/


fun handleHabitClick(context: Context, habit: String) {
    Log.i("ONCLICK", "Clicked: ${habit}")

    val ref = Firebase.database.getReference("Habits/${habit}/${getDate(0)}/status")
    ref.get().addOnSuccessListener {
        Log.i("ONCLICK", "Old Value: ${it.value}")
        val newStatus = when (it.value.toString()) {
            "NONE" -> "DONE"
            "null" -> "DONE"
            "DONE" -> "FAIL"
            "FAIL" -> "SKIP"
            "SKIP" -> "NONE"
            else -> "NONE"
        }
        ref.setValue(newStatus).addOnSuccessListener{ createHabitListener(context, habit) }
        createAllHabitListeners(context)
    }
}


fun createOnClickListeners(context: Context) {

    // get global references
    val widgetManager = AppWidgetManager.getInstance(context)
    val widgetIds = widgetManager.getAppWidgetIds(ComponentName(context, HabitAppWidget::class.java))

    for (widgetId in widgetIds) {
        // get the remote view of the widget
        val views = RemoteViews(context.packageName, R.layout.habit_widget)

        // set the click listeners for the habit cards on the widget
        views.setOnClickPendingIntent(R.id.habitcard_tagebuch, createPendingIntent(context, "Tagebuch"))
        views.setOnClickPendingIntent(R.id.habitcard_meditieren, createPendingIntent(context, "Meditieren"))
        views.setOnClickPendingIntent(R.id.habitcard_arbeiten, createPendingIntent(context, "Arbeiten"))
        views.setOnClickPendingIntent(R.id.habitcard_training, createPendingIntent(context, "Training"))
        widgetManager.partiallyUpdateAppWidget(widgetId, views)
        Log.i("ONCLICK ", "Set all onclick pending intents")
    }
}


fun getHabitButtonColor(defaultColor: Int,habitDay: HashMap<String, *>?) : Int {

    val status = habitDay?.get("status") as String?

    // get the color of the habit button for the given day
    var icon = when (status) {
        "DONE" -> R.drawable.circle_done
        "SKIP" -> R.drawable.circle_skip
        "FAIL" -> R.drawable.circle_fail
        "NONE" -> R.drawable.circle_none
        else -> defaultColor
    }

    // check if the day has an attribute Minuten and set the icon accordingly
    if (habitDay?.get("minuten") != null && icon != R.drawable.circle_done) {

        // calculate how much of the habit is done
        val minutes = habitDay["minuten"] as Long
        val circleIndex = min(20.0,((minutes / minutesTotal) * 20))
        icon = circlesDrawables[circleIndex.toInt()]

        // set the icon according to the minutes
        Log.i("ONCLICK", "Minuten: ${minutes}, CircleIndex: ${circleIndex} Icon: ${icon}")
    }

    return icon
}


fun updateHabitWidget(context: Context, habit: String, habitsData: HashMap<String, *>) {

    // get the button ids for the habit
    val buttons = habitsDict[habit]!!["buttons"] as List<Int>
    // get the default color, or circle_fail if not set
    val defaultColor = habitsDict[habit]?.get("default") as Int? ?: R.drawable.circle_fail

    // get the global references
    val widgetManager = AppWidgetManager.getInstance(context)
    val widgetIds = widgetManager.getAppWidgetIds(ComponentName(context, HabitAppWidget::class.java))
    val views = RemoteViews(context.packageName, R.layout.habit_widget)

    // iterate over all buttons
    val lastHabitIdx = (numberHabits - 1)
    for (i in 0 .. lastHabitIdx) {
        val button = buttons.elementAt(i)
        val date = getDate(-(lastHabitIdx-i))  // get the days offset inverted
        val day = getDay(-(lastHabitIdx-i))

        // compare the date the button represents against existing dates
        val habitDay = habitsData[date]
        var icon = defaultColor
        if (i == lastHabitIdx) {
            icon = R.drawable.circle_open
        }
        // check if the date exists in the habitsData and if it is a HashMap
        if (habitDay != null && habitDay is HashMap<*, *>) {
            icon = getHabitButtonColor(defaultColor, habitDay as HashMap<String, *>)
        }

        // set the button background according to the habit state
        views.setInt(button,"setBackgroundResource", icon)
        views.setTextViewText(button, day)
    }

    // update the widget with the new data
    for (widgetId in widgetIds) {
        // partially update the widget
        widgetManager.partiallyUpdateAppWidget(widgetId, views)
    }
}
