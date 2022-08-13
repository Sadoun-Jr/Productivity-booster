package com.example.android.productivity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.res.TypedArrayUtils.getString
import com.example.android.productivity.productivitytracker.AddGoal
import java.util.*

class ReminderBroadcast : BroadcastReceiver() {

    val channelID = "10001"
    val WAKEUP_NOTIFICATION_ID = 200
    val SLEEP_NOTIFICATION_ID = 100
    val LOG_TAG = "Alarm Manager"

    companion object {
        private const val MSG_ADDGOAL = "Go to AddGoalFragment"
        private const val MSG_EDITGOAL = "Edit Goal"
        private const val MSG_RATEGOAL = "Rate Goal"
        private const val PARAM_NAME = "name"
        private const val REQUEST_TIMER_SLEEP = 1
       private const val REQUEST_TIMER_WAKE = 2
    }

    override fun onReceive(context: Context?, intentReceived: Intent?) {

        Log.e(LOG_TAG, "broadcaster received")
        val trigger : String = intentReceived?.getStringExtra("trigger").toString()
        val requestCode : Int? = intentReceived?.getIntExtra("request code", -1)
        val intent = Intent(context, AddGoal::class.java)

        if (trigger == "wake"){
            intent.putExtra("NotificationMessage", MSG_ADDGOAL)
            intent.putExtra("Add", "")
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.action = Intent.ACTION_MAIN
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            val pendingIntent =
                PendingIntent.getActivity(context, requestCode!!, intent, PendingIntent.FLAG_IMMUTABLE)

            val builder : NotificationCompat.Builder =
                NotificationCompat.Builder(context!!, channelID)
                    .setSmallIcon(R.drawable.efficiencylauncher)
                        //put some emojis here
                    .setContentTitle("Rise and Shine!")
                    .setContentText("Time to be productive and set a goal!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

            val notificationManager = NotificationManagerCompat.from(context)

            notificationManager.notify(WAKEUP_NOTIFICATION_ID, builder.build())

        } else if (trigger == "sleep"){
            //get the day that you want the user to rate in case
                // the user clicks the notification the next day
                    val calendar = Calendar.getInstance()

            val day = calendar[Calendar.DAY_OF_YEAR]
            val yr = calendar[Calendar.YEAR]
            val dayYear = "$day + $yr"

            intent.putExtra("NotificationMessage", MSG_RATEGOAL)
            intent.putExtra("Add", dayYear)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.action = Intent.ACTION_MAIN
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            val pendingIntent =
                PendingIntent.getActivity(context, requestCode!!, intent, PendingIntent.FLAG_IMMUTABLE)

            val builder : NotificationCompat.Builder =
                NotificationCompat.Builder(context!!, channelID)
                    .setSmallIcon(R.drawable.efficiencylauncher)
                        //put some emojis here
                    .setContentTitle("Sleep time soon")
                    .setContentText("Did you manage to achieve today's goal? Rate it now!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

            val notificationManager = NotificationManagerCompat.from(context)

            notificationManager.notify(SLEEP_NOTIFICATION_ID, builder.build())
        }

    }

    private fun getIntent(context : Context, requestCode: Int, name: String? = null) : PendingIntent? {
        val intent = Intent(context, ReminderBroadcast::class.java)
        intent.putExtra(PARAM_NAME, name)
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
    }
}



