package com.example.habittracker

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.habittracker.data.DiaryEntry
import com.example.habittracker.data.HabitDay
import com.example.habittracker.databinding.ActivityDiaryBinding
import com.google.firebase.database.FirebaseDatabase

class DiaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Hide the status bar
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        // Hide the action bar
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        binding = ActivityDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.saveButton.setOnClickListener {
            Log.i("Diary Activity", "CLICKED\n\n\n")
            sendDiaryFirebase()
        }
        Log.i("Diary Activity", "Set Listener\n\n\n")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.i("DIARY-DETAIL", "onKeyDown: Finish activity")
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun sendDiaryFirebase() {

        // create the diary from the Input Data
        val diaryText = binding.diaryText.text.toString()
        val diaryMood = when (binding.diaryMoodGroup.checkedRadioButtonId) {
            R.id.mood_great -> "Great"
            R.id.mood_good -> "Good"
            R.id.mood_okay -> "Okay"
            R.id.mood_bad -> "Bad"
            R.id.mood_terrible -> "Terrible"
            else -> "None"
        }
        val diaryEntry = DiaryEntry(diaryText, diaryMood)
        Log.i("MainActivity", "Text: ${diaryText} | Mood: ${diaryMood}")

        if (diaryText == "" || diaryMood == "None") {
            Log.i("MainActivity", "Text or Mood missing!")
            return
        }

        // get the current date as a string, to reference the entry in firebase
        val dateString = getDate()
        val dateTimeString = getDate(includeTime = true)

        // initialize firebase
        val database = FirebaseDatabase.getInstance()
        val diaryRef = database.getReference("Tagebuch/${dateTimeString}")
        val habitRef = database.getReference("Habits/Tagebuch/${dateString}")

        // create a new object
        val habitDay = HabitDay(status = "DONE", datum = dateString)

        // save the diary entry, and then mark the habit as done
        diaryRef.setValue(diaryEntry).addOnSuccessListener {
            habitRef.setValue(habitDay).addOnSuccessListener {
                closeApp()
            }
        }
    }

    private fun closeApp() {
        // close the app
        Thread.sleep(150)
        finishAndRemoveTask()
        System.exit(0)
    }
}