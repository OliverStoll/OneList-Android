package com.example.habittracker.util

import android.content.Context
import android.util.Log
import com.example.habittracker.data.HabitDay
import com.example.habittracker.habitsDict
import com.example.habittracker.updateHabitWidget
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
            updateHabitWidget(context, habit, habitsData)
        }
        override fun onCancelled(error: DatabaseError) {
            Log.i("FIREBASE", "Cancelled $habit")
        }
    })
}

fun createAllHabitListeners(context: Context) {

    // create the listeners
    Log.i("FIREBASE", "CREATE ALL HABIT LISTENERS")
    for (habitData in habitsDict){
        createHabitListener(context, habitData.key)
    }
}