package com.example.habittracker

import android.app.Activity
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


/**
 * Implementation of App Widget functionality.
 */

class NewAppWidget : AppWidgetProvider() {
    private var firebaseListener = false

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // create firebase functionality
        Log.i("FIREBASE", "LISTENER:$firebaseListener")

        if (!this.firebaseListener) {
            // Firebase.database.setPersistenceEnabled(true)
            createFirebaseListeners(context, appWidgetManager, appWidgetIds)
            this.firebaseListener = true
        }

        // create the habit clicking option
        createHabitOnclickListener(context, appWidgetManager, appWidgetIds)
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
        Firebase.database.setPersistenceEnabled(true)
    }

    override fun onDisabled(context: Context) {    }

    override fun onReceive(context: Context, intent: Intent) {
        fun toggleHabit(habit: String) {
            val database = Firebase.database
            Log.i("ONCLICK", "Toggle ${habit}")
            val ref = database.getReference("Habits/${habit}/${getDate(0)}")
            ref.get().addOnSuccessListener {
                Log.i("ONCLICK", it.value.toString())
                when (it.value.toString()) {
                    "NONE" -> ref.setValue("DONE")
                    "null" -> ref.setValue("DONE")
                    "DONE" -> ref.setValue("SKIP")
                    "SKIP" -> ref.setValue("FAIL")
                    "FAIL" -> ref.setValue("NONE")
                }
            }
        }

        super.onReceive(context, intent)
        when (intent.action) {
            "TAGEBUCH" -> toggleHabit("Tagebuch")
            "MEDITIEREN" -> toggleHabit("Meditieren")
            "ARBEITEN" -> toggleHabit("Arbeiten")
            "TRAINING" -> toggleHabit("Training")
        }
    }
}

fun createHabitOnclickListener(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {

    fun getPendingSelfIntent(_context: Context, action: String): PendingIntent? {
        val intent = Intent(_context, NewAppWidget::class.java)
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

fun createFirebaseListeners(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {

    fun createHabitListener(habitName: String, database: FirebaseDatabase, buttons: List<Int>) {
        val habitRef = database.getReference("Habits/${habitName}").limitToLast(7)
        habitRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i("FIREBASE", "${habitName}: ${snapshot.value}")
                if (snapshot.value == null) {
                    return
                }
                for (appWidgetId in appWidgetIds) {
                    updateHabitAppWidget(context, appWidgetManager, appWidgetId, buttons=buttons,
                        habitsHashmap = snapshot.value as HashMap<String, String>
                    )
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    // firebase
    val database = Firebase.database
    val buttons_tagebuch = listOf<Int>(R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6, R.id.button7)
    val buttons_meditieren = listOf<Int>(R.id.button11, R.id.button12, R.id.button13, R.id.button14, R.id.button15, R.id.button16, R.id.button17)
    val buttons_arbeiten = listOf<Int>(R.id.button21, R.id.button22, R.id.button23, R.id.button24, R.id.button25, R.id.button26, R.id.button27)
    val buttons_training = listOf<Int>(R.id.button41, R.id.button42, R.id.button43, R.id.button44, R.id.button45, R.id.button46, R.id.button47)
    createHabitListener("Tagebuch", database, buttons_tagebuch)
    createHabitListener("Meditieren", database, buttons_meditieren)
    createHabitListener("Arbeiten", database, buttons_arbeiten)
    createHabitListener("Training", database, buttons_training)
}

fun updateHabitAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, buttons: List<Int>, habitsHashmap: HashMap<String, String>) {

    Log.i("FIREBASE", "Updating Habit")
    val views = RemoteViews(context.packageName, R.layout.app_widget)


    // iterate over all buttons
    for (i in 0 .. 6) {
        val button = buttons.elementAt(i)
        val date = getDate(-(6-i))  // get the days offset inverted
        val day = getDay(-(6-i))
        var icon = R.drawable.circle_none

        // compare the date the button represents against existing dates
        if (date in habitsHashmap.keys){
            val dateValue = habitsHashmap[date]
            Log.i("FIREBASE", "$date: $dateValue")

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
