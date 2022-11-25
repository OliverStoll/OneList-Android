package com.example.habittracker

import android.os.Bundle
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
    }


}