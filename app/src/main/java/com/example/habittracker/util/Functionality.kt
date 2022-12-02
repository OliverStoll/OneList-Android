package com.example.habittracker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*


fun getDate(offset: Int = 0, delimiter: String = "_", includeTime: Boolean = false): String {
    val c = Calendar.getInstance()
    c.add(Calendar.DATE, offset)

    val year = c.get(Calendar.YEAR).toString()
    val month = (c.get(Calendar.MONTH)+1).toString().padStart(2, '0')
    val day = c.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')

    if (includeTime) {
        val hour = c.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val minute = c.get(Calendar.MINUTE).toString().padStart(2, '0')
        return "${year}${delimiter}${month}${delimiter}${day} ${hour}:${minute}"
    }

    return year + delimiter + month + delimiter + day
}

fun getWeekday(offset: Int): String {
    val c = Calendar.getInstance()
    c.add(Calendar.DATE, offset)

    return when (c.get(Calendar.DAY_OF_WEEK)) {
        1 -> "S"
        2 -> "M"
        3 -> "D"
        4 -> "M"
        5 -> "D"
        6 -> "F"
        7 -> "S"
        else -> "ERROR"
    }
}


fun createPendingIntent(context: Context, action: String, widget: Class<*> = HabitAppWidget::class.java): PendingIntent? {
    val intent = Intent(context, widget)
    intent.action = action
    return PendingIntent.getBroadcast(context, 0, intent, 0)
}

fun makeToast(context: Context, text: String) {
    val duration = android.widget.Toast.LENGTH_SHORT
    val toast = android.widget.Toast.makeText(context, text, duration)
    toast.show()
}