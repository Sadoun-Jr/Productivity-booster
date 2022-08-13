package com.example.android.productivity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class SampleBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            val alarmIntent = Intent(context, ReminderBroadcast::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0)

            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val interval = 8000
            manager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                interval.toLong(),
                pendingIntent
            )

            Toast.makeText(context, "Alarm Set", Toast.LENGTH_SHORT).show()
        }
    }
    }
