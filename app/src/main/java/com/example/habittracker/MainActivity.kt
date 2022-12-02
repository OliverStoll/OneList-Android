package com.example.habittracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.example.habittracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Firebase.database.setPersistenceEnabled(true)
        // FirebaseDatabase.getInstance().setLogLevel(Logger.Level.INFO)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // open TodoDetailActivity when button is clicked
        binding.button.setOnClickListener {
            makeToast(this, "Button clicked")
            val intent = Intent(this, DiaryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.i("MAIN-ACTIVITY", "onKeyDown: Finish activity")
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }


}