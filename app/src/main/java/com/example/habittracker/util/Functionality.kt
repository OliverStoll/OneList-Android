package com.example.habittracker

import android.widget.RemoteViews
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.Calendar



fun getDate(offset: Int): String {
    val c = Calendar.getInstance()
    c.add(Calendar.DATE, offset)

    val year = c.get(Calendar.YEAR).toString()
    val month = (c.get(Calendar.MONTH)+1).toString().padStart(2, '0')
    val day = c.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')

    return year + "_" + month + "_" + day
}

fun getDay(offset: Int): String {
    val c = Calendar.getInstance()
    c.add(Calendar.DATE, offset)

    return when (c.get(Calendar.DAY_OF_WEEK)) {
        1 -> "So"
        2 -> "Mo"
        3 -> "Di"
        4 -> "Mi"
        5 -> "Do"
        6 -> "Fr"
        7 -> "Sa"
        else -> "ERROR"
    }
}

fun main() {


}
