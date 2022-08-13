/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.productivity

import android.app.*
import android.app.ActivityManager.RunningTaskInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.findNavController
import com.example.android.productivity.database.ProductivityDatabase
import com.example.android.productivity.database.ProductivityDatabaseDao
import com.example.android.productivity.initialization.OnBoardingDesignOne
import com.example.android.productivity.productivitytracker.AddGoal
import kotlinx.coroutines.runBlocking
import java.util.*


class MainActivity : AppCompatActivity() {

    //I STARTED CREATING SHAREDPREF INSTEAD OF DB AT 3:00 PM, 25/4
    //RESTORE THESE FILES:

    val NOTIFICATION_CHANNEL_ID = "10001"
    // Create an instance of database the ViewModel Factory.
    var dataSource : ProductivityDatabaseDao? = null
    val LOG_TAG = "MainActivity"

    var prefs: SharedPreferences? = null
    var sharedPreferencesEditor: SharedPreferences.Editor? = null

    val NAME_KEY = "Name"
    val SLEEP_KEY_HOUR = "Sleep Time Hour"
    val SLEEP_KEY_MINUTE = "Sleep Time Minute"
    val WAKE_KEY_HOUR = "Wake up Hour"
    val WAKE_KEY_MINUTE = "Wake up Minute"
    val FIRST_TIME_USER = "first time"
    val FIRST_TIME_USER_ONBOARD = "first time"
    val MAIN_MENU_CLICK = "main menu"
    val MSG_RATEGOAL = "Rate Goal"
    val STREAK_COUNTER = "streak counter"
    val SPLASH_SCREEN_KEY = "splashscreen"
    val RESET_DETAILS = "reset details"

    private  val REQUEST_TIMER_SLEEP = 1
    private  val REQUEST_TIMER_WAKE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        //todo:put proper images for the ratings and reflect this in the recycler view

        //hide status bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        prefs = getSharedPreferences(
            getString(R.string.app_name), MODE_PRIVATE
        )
        sharedPreferencesEditor = prefs?.edit()

        //to avoid getting splashscreen on every intent to main activity

            // only for gingerbread and newer versions
//            val splashScreen = installSplashScreen()

            // Handle the splash screen transition.
            Log.e(LOG_TAG, "Splash screen happens")

        var firstTimeUserOnBoard : Boolean? = true

        try{ firstTimeUserOnBoard = prefs?.getBoolean(FIRST_TIME_USER_ONBOARD, true)}
        catch (e:Exception){ firstTimeUserOnBoard = true }

        //navigate to OnBoard screen if the user is new
        if(firstTimeUserOnBoard == null || firstTimeUserOnBoard == true){
            startActivity(Intent(this, OnBoardingDesignOne::class.java))
            finish()
        }

        val application = requireNotNull(this).application

        dataSource = ProductivityDatabase.getInstance(application).productivityDatabaseDao

        super.onCreate(savedInstanceState)
        //TODO: hide action bar

        setContentView(R.layout.activity_main)

        createNotificationChannel()

        triggerBootReceiver()

        setupCustomActionbar(this)

        //only trigger notifications after first goal has been set
        //the pref FIRST_TIME_USER is set in prod tracker frag
        val firstTimeUser = prefs?.getBoolean(FIRST_TIME_USER, true)

        if (firstTimeUser == false){

            createSleepNotification()

            createWakeupNotification()

        }

        val streakTxt = findViewById<TextView>(R.id.name)
        setupStreak(streakTxt)

        // TODO: when entering app for first time, when the user goes back from prodtrackfrag to prev screen, don't allow to go back to sleep/wake
    }

    private fun setupStreak(streakTxt: TextView?) {
//        streakTxt?.text = "Streak: 512 days!"
        val currentStreak = prefs?.getInt(STREAK_COUNTER, 0)
        if (currentStreak!! > 1){
            streakTxt?.text = "Streak: $currentStreak days"
        } else if (currentStreak <= 1){
            streakTxt?.text = "Streak: $currentStreak day"
        } else if (currentStreak == 0){
            streakTxt?.text = "Streak: $currentStreak days"
        }
    }


    private fun setupCustomActionbar(context: Context) {
        this.supportActionBar!!.displayOptions = ActionBar.DISPLAY_HOME_AS_UP
        supportActionBar!!.setDisplayShowCustomEnabled(true)
        supportActionBar!!.setCustomView(R.layout.custom_action_bar)
        //getSupportActionBar().setElevation(0);
        supportActionBar!!.setDisplayHomeAsUpEnabled(false);
        supportActionBar!!.setHomeButtonEnabled(false);
        supportActionBar!!.setBackgroundDrawable(getDrawable(R.drawable.blue_actionbar))

        val view: View = supportActionBar!!.customView

    }

    private fun triggerBootReceiver() {
        val receiver = ComponentName(this, SampleBootReceiver::class.java)

        this.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun createSleepNotification() {
        createNotification("sleep", SLEEP_KEY_HOUR, SLEEP_KEY_MINUTE, REQUEST_TIMER_SLEEP)
        Log.v(LOG_TAG, "Created sleep notification at $SLEEP_KEY_HOUR : $SLEEP_KEY_MINUTE")
    }

    private fun createWakeupNotification() {

        createNotification("wake", WAKE_KEY_HOUR, WAKE_KEY_MINUTE, REQUEST_TIMER_WAKE)
        Log.v(LOG_TAG, "Created wake notification at $WAKE_KEY_HOUR : $WAKE_KEY_MINUTE")

    }

    private fun createNotification(key: String, hoursKey: String, minutesKey: String, requestCode : Int) {
        val intent = Intent(this, ReminderBroadcast::class.java)
        intent.putExtra("trigger", key)
        intent.putExtra("request code", requestCode)

        val pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
        val alarmManager : AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        val millisInDay : Long = 24 * 60 * 60 * 1000
        val calendar = Calendar.getInstance()

        calendar[Calendar.HOUR_OF_DAY] = prefs?.getInt(hoursKey, -1)!!
        calendar[Calendar.MINUTE] = prefs?.getInt(minutesKey, -1)!!
        calendar[Calendar.SECOND] = 0

        if (calendar.time < Date()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        //TODO: assign start time properly from SP
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis, millisInDay,
//            testing instant notifications
//            System.currentTimeMillis(),60000,
            pendingIntent
        )

    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //testing purposes
//            R.id.goToMain -> {
//                sharedPreferencesEditor?.putBoolean(MAIN_MENU_CLICK, true)?.apply()
//                val navController = findNavController(R.id.nav_host_fragment)
//                navController.navigateUp() // to clear previous navigation history
//                navController.navigate(R.id.getInitialDetails)
//
//                startActivity(Intent(this, OnBoardingDesignOne::class.java))
//                finish()
//            }
//            R.id.deleteDBtest -> {
//                runBlocking {
//                    dataSource?.clear()
//                }
//            }
//            R.id.fake_rate -> {
//                val intent = Intent(this, AddGoal::class.java)
//                intent.putExtra("Add", "121 + 2022")
//                intent.putExtra("NotificationMessage", MSG_RATEGOAL)
//                startActivity(intent)
//            }
            R.id.credits -> {
                val intent = Intent(this, CreditsActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.resetSleepAndWakeup -> {
                sharedPreferencesEditor?.putBoolean(RESET_DETAILS, true)?.apply()

                val navController = findNavController(R.id.nav_host_fragment)
                navController.navigateUp() // to clear previous navigation history
                navController.navigate(R.id.getInitialDetails)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}

