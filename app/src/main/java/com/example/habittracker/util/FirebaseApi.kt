package com.example.habittracker.util

import android.content.Context
import android.util.Log
import com.example.habittracker.getDate
import com.example.habittracker.habitsDict
import com.example.habittracker.updateWidgetOneHabit
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


fun createHabitListener(context: Context, habit: String) {

    // get global references
    val database = Firebase.database

    // add the listener for the habit, sorted by date
    val habitRef = database.getReference("Habits/${habit}").limitToLast(8)
    habitRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            Log.i("FIREBASE", "Received ${habit}: ${snapshot.value}")
            if (snapshot.value == null) { return }  // end if there are no values

            // typecast the snapshot to a hashmap of habit days
            val habitsData = snapshot.value as HashMap<String, *>

            // update the widget with the new data
            updateWidgetOneHabit(context, habit, habitsData)
        }
        override fun onCancelled(error: DatabaseError) {
            Log.i("FIREBASE", "Cancelled $habit")
        }
    })
}

fun getHabitData(context: Context, habit: String) {

    // get global references
    val database = Firebase.database

    // add the listener for the habit, sorted by date
    val habitRef = database.getReference("Habits/${habit}").limitToLast(8)
    habitRef.get().addOnSuccessListener { snapshot ->
        Log.i("FIREBASE", "Received ${habit}: ${snapshot.value}")
        if (snapshot.value != null) {
            // typecast the snapshot to a hashmap of habit days
            val habitsData = snapshot.value as HashMap<String, *>

            // update the widget with the new data
            updateWidgetOneHabit(context, habit, habitsData)
        }
    }
}

fun createAllHabitListeners(context: Context) {

    // create the listeners
    Log.i("FIREBASE", "CREATE ALL HABIT LISTENERS")
    for (habitData in habitsDict){
        getHabitData(context, habitData.key)
        createHabitListener(context, habitData.key)
    }
}

fun updateTodoItem(newTodoData: MutableMap<String, String?>) {
    Log.i("FIREBASE", "UPDATE TODO ITEM")
    val id = newTodoData["id"]!!
    val todoRef = Firebase.database.getReference("To-Do/${id}")
    todoRef.setValue(newTodoData)
}

fun snoozeTodoItem(todoData: MutableMap<String, String?>, snoozeDays: Int = 1) {
    Log.i("FIREBASE", "SNOOZE TODO ITEM")
    val id = todoData["id"]!!
    val snoozeDate = getDate(snoozeDays)
    todoData["start_datum"] = snoozeDate
    val todoRef = Firebase.database.getReference("To-Do/${id}")
    todoRef.setValue(todoData)
}

fun deleteTodoItem(ref: String) {
    Log.i("FIREBASE", "DELETE TODO ITEM")
    val todoRef = Firebase.database.getReference("To-Do/$ref")
    todoRef.removeValue()
}