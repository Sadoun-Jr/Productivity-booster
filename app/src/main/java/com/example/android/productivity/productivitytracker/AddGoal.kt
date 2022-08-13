package com.example.android.productivity.productivitytracker

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.example.android.productivity.MainActivity

import com.example.android.productivity.R
import com.example.android.productivity.database.ProductiveDay
import com.example.android.productivity.database.ProductivityDatabase
import com.example.android.productivity.database.ProductivityDatabaseDao
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.add_goal.*

import kotlinx.coroutines.runBlocking
import java.util.*

class AddGoal : AppCompatActivity() {

    private val LOG_TAG = "AddGoal activity"
    private lateinit var dataSource : ProductivityDatabaseDao
    private lateinit var goalEditTxt : EditText
    private lateinit var descEditTxt : EditText
    private var name : String = ""
    private var sleepTime : Int = -2
    private var wakeTime : Int = -2

    /**
     * [rating] value of goal rating, 1: Happy, 2:Meh, 3:Sad, 0: null
     */
    private var rating : Int = 0
    private lateinit var progressBar : ProgressBar
    private lateinit var mainLinear : LinearLayout
    private lateinit var ratingLinear : LinearLayout
    private lateinit var inspireRelative : RelativeLayout
    private lateinit var happyImg : ImageView
    private lateinit var topImg : ImageView
    private lateinit var mehImg : ImageView
    private lateinit var sadImg : ImageView
    private lateinit var proceedButtonToInspire : Button
    private lateinit var mainTxt : TextView
    private lateinit var doneImg : ImageView
    private lateinit var proceedBtn : Button
    private lateinit var inspireTxt : TextView
    private lateinit var fab : FloatingActionButton
    private val HAPPY_RATING = 1
    private val MEH_RATING = 2
    private val SAD_RATING = 3
    private var bundle : Bundle? = Bundle()

    private  val MSG_ADDGOAL = "Go to AddGoalFragment"
    private  val MSG_EDITGOAL = "Edit Goal"
    private  val MSG_RATEGOAL = "Rate Goal"
    private  val MSG_UPDATE = "update whole day"

    val STREAK_COUNTER = "streak counter"
    val CURRENT_DAY_FOR_STREAK = " current day streak"
    val LAST_DAY_SAVED = "last day saved for strreak"

    private val NAVIGATE_TO_ADD = "go to add activity"

    var prefs: SharedPreferences? = null
    var sharedPreferencesEditor: SharedPreferences.Editor? = null

    val NAME_KEY = "Name"
    val SLEEP_KEY = "Sleep Time"
    val WAKE_KEY = "Wake up"
    val SPLASH_SCREEN_KEY = "splashscreen"

    /**
     * [addForFab] is a fab marker, true for add functionality, and is invoked when
     * this activity is accessed from add notification or add button in previous fragment
     */
    var addForFab = false
    /**
     * [rateForFab] is a fab marker, true for rate functionality, and is invoked when
     * this activity is accessed from rate notification
     */
    var rateForFab = false
    /**
     * [editForFab] is a fab marker, true for edit functionality, and is invoked when
     * this activity is accessed from recycler view
     */
    var editForFab = false

    override fun onCreate(savedInstanceState: Bundle?) {

        // Handle the splash screen transition.
        //TODO: implement finished your goal or not screen, quotes screen
        super.onCreate(savedInstanceState)

//        //hide status bar
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.add_goal)

        val application = requireNotNull(this).application

        prefs = this.getSharedPreferences(
            getString(R.string.app_name), MODE_PRIVATE
        )
        sharedPreferencesEditor = prefs?.edit()

        goalEditTxt = findViewById(R.id.goalEditText)
        descEditTxt = findViewById(R.id.descEditTxt)
        progressBar = findViewById(R.id.progressBar)
        mainLinear = findViewById(R.id.mainLinear)
        ratingLinear = findViewById(R.id.ratingLinearLayout)
        happyImg = findViewById(R.id.happyIcon)
        mehImg = findViewById(R.id.mehIcon)
        sadImg = findViewById(R.id.sadIcon)
        proceedButtonToInspire = findViewById(R.id.testButton2)
        topImg = findViewById(R.id.topImg)
        fab = findViewById(R.id.fab)
        mainTxt = findViewById(R.id.createOrUpdateGoalText)
        inspireTxt = findViewById(R.id.goalMadeTxt)
        doneImg = findViewById(R.id.firstItemImg)
        proceedBtn = findViewById(R.id.doneBtn)
        inspireRelative = findViewById(R.id.relativeLayoutForShowDoneLayout)

        dataSource = ProductivityDatabase.getInstance(application).productivityDatabaseDao

        supportActionBar?.hide()

        bundle = intent.extras
        //was this accessed from the 1- add activity to add a normal goal, or 2- wake up notification
        //FIXME: both the variables below are null when accessed from notification

        val Add : String? = bundle?.getString("Add")!!.toString()
        val notificationMessage = bundle?.getString("NotificationMessage").toString()


        var dayIdFromRv = ""
        var dayIdFromSleepNotification = ""
        //check from where was this activity accessed
        if (notificationMessage == MSG_ADDGOAL || Add == NAVIGATE_TO_ADD){
            addForFab = true
            showAddUI()

        }
        if (notificationMessage == MSG_RATEGOAL){
            rateForFab = true
            dayIdFromSleepNotification = Add!!
            showRateUI()
        }
        if (Add == MSG_UPDATE){
            editForFab = true
            dayIdFromRv = notificationMessage
            runBlocking {showEditUI(dayIdFromRv)}

        }

        //hide the confirmation relative layout
        inspireRelative.visibility = View.GONE

        //desc edit text and goal edit text should look good
        descEditTxt.background = AppCompatResources.getDrawable(this,
            R.drawable.edit_text_border)
        goalEditTxt.background = AppCompatResources.getDrawable(this,
            R.drawable.edit_text_border)


        //set up the rating images logic
        setUpRatingImages()


        //method to insert random fake goals for testing
        proceedButtonToInspire.setOnClickListener{
            //put new goal into database
            when {
                addForFab -> {
                    runBlocking {
                        insertGoalIntoDatabase()
                    }
                }

                //edit the goal with today's rating
                rateForFab -> {
                    runBlocking {
                        rateDay(dayIdFromSleepNotification)
                    }
                }
                //completely edit the entry
                editForFab -> {
                    runBlocking {
                        completelyEditGoal(dayIdFromRv.toLong())
                    }
                }
            }
        }
        fab.visibility = View.GONE

        proceedBtn.setOnClickListener{
            proceedToProdTrackFrag()
        }

        fab.setOnClickListener{
            runBlocking {
                insertRandomGoals()
            }
        }
    }

    private fun showRateUI() {

        //TODO: hide mainImg in inspiration text
        //TODO: better icon for well done

        name = prefs?.getString(NAME_KEY, "null").toString()
        mainLinear.visibility = View.VISIBLE
        ratingLinear.visibility = View.VISIBLE
        inspireRelative.visibility = View.GONE
        goalEditTxt.visibility = View.GONE
        descEditTxt.visibility = View.VISIBLE

        mainTxt.text = "Time to sleep, $name!\n" + getString(R.string.rateTodaysGoal)

        mainTxt.setTextColor(Color.parseColor("#0096FF"))

        proceedButtonToInspire.background = AppCompatResources.getDrawable(this,
            R.drawable.my_blue_btn)
        proceedButtonToInspire.setTextColor(resources.getColor(R.color.white_text_color))
        topImg.visibility = View.VISIBLE
        topImg.setImageResource(R.drawable.night)
        proceedButtonToInspire.setText(R.string.rateGoal)

    }

    private suspend fun showEditUI(id: String) {
        mainLinear.visibility = View.VISIBLE
        goalEditTxt.visibility = View.VISIBLE
        inspireRelative.visibility = View.GONE
        ratingLinear.visibility = View.VISIBLE
        descEditTxt.visibility = View.VISIBLE

        mainTxt.visibility = View.VISIBLE
        mainTxt.text = "Edit Entry"
        proceedButtonToInspire.setText(R.string.done)


        proceedButtonToInspire.background = AppCompatResources.getDrawable(this,
            R.drawable.my_blue_btn)
        mainTxt.setTextColor(Color.parseColor("#0096FF"))

        proceedButtonToInspire.setTextColor(resources.getColor(R.color.white_text_color))
        topImg.visibility = View.VISIBLE

        topImg.setImageResource(R.drawable.edit_entryy)

        proceedButtonToInspire.setTextColor(resources.getColor(R.color.white_text_color))

        val dayId = id.toLong()
        val dayToEdit = dataSource.getDayById(dayId)
        val rating = dayToEdit?.overallDayRating
        goalEditTxt.setText(dayToEdit?.firstGoal)
        descEditTxt.setText(dayToEdit?.desc)

        when(rating){
            1 -> happyImg.setImageResource(R.drawable.happy_selected)
            2 -> mehImg.setImageResource(R.drawable.meh_selected)
            3 -> sadImg.setImageResource(R.drawable.sad_selected)

        }
    }

    private fun setUpRatingImages() {
        if (rateForFab){
            //initial state, show all images as non selected
            happyImg.setImageResource(R.drawable.happy_not_selected)
            mehImg.setImageResource(R.drawable.meh_not_selected)
            sadImg.setImageResource(R.drawable.sad_not_selected)
        }

        //save rating into database after being chosen
        happyImg.setOnClickListener{
            rating = HAPPY_RATING

            //highlight it because it is selected and unhighlight the others
            happyImg.setImageResource(R.drawable.happy_selected)
            mehImg.setImageResource(R.drawable.meh_not_selected)
            sadImg.setImageResource(R.drawable.sad_not_selected)

        }
        mehImg.setOnClickListener{
            rating = MEH_RATING

            //highlight it because it is selected and unhighlight the others
            happyImg.setImageResource(R.drawable.happy_not_selected)
            mehImg.setImageResource(R.drawable.meh_selected)
            sadImg.setImageResource(R.drawable.sad_not_selected)
        }
        sadImg.setOnClickListener{
            rating = SAD_RATING

            //highlight it because it is selected and unhighlight the others
            happyImg.setImageResource(R.drawable.happy_not_selected)
            mehImg.setImageResource(R.drawable.meh_not_selected)
            sadImg.setImageResource(R.drawable.sad_selected)
        }

    }

    private suspend fun completelyEditGoal(id:Long) {
        val newGoal = goalEditTxt.text.toString().trimStart().trimEnd()
        val newDesc = descEditTxt.text.toString().trimStart().trimEnd()

        if (rating != 0){
            val newRating = rating
            dataSource.updateSpecificWholeDay(newGoal, newRating, newDesc, id)
            editForFab = false

            Toast.makeText(this, R.string.goalEditedSuccess, Toast.LENGTH_SHORT).show()
            proceedToProdTrackFrag()
        } else {
            Toast.makeText(this, R.string.plsRate, Toast.LENGTH_SHORT).show()

        }

    }

    private suspend fun insertRandomGoals() {
//        val randomNumber: Int = Random().nextInt(50)
//        val year = (2020..2024).random()

        for (i in 1..123){
            val year = 2022
            val dayNow = "$i + $year"
            val random = (1..500).random().toString()
            val randomGoal = "This is random goal #$random"
            val randomDesc = "Description of random goal #$random"
            val productiveDay = ProductiveDay(0, randomGoal, dayNow, randomDesc, (1..3).random())
            dataSource.insert(productiveDay)
        }

        val calender = Calendar.getInstance()
        val year = calender.get(Calendar.YEAR)
        val dayOfYear = calender.get(Calendar.DAY_OF_YEAR)

        //create streak logic, send day and year as int to compare to last entry and see if
        //more than 1 day passed since creation
        val lastDayFromDbArray = dataSource.getAllDays()
        val lastDay = cutDateFromDbToDays(lastDayFromDbArray[0].day)
        val lastYr = cutDateFromDbToYrs(lastDayFromDbArray[0].day)

        createStreak(dayOfYear, year, lastDay.toInt(), lastYr.toInt())

    }

    private fun showAddUI() {

        name = prefs?.getString(NAME_KEY, "-1")!!

        ratingLinear.visibility = View.GONE
        mainLinear.visibility = View.VISIBLE
        descEditTxt.visibility = View.GONE

        name = prefs?.getString(NAME_KEY, "-1")!!
        mainTxt.text = "Hey $name, " + getString(R.string.whatsTodaysGoal)

        mainTxt.setTextColor(Color.parseColor("#0096FF"))

        goalEditTxt.background = AppCompatResources.getDrawable(this,
            R.drawable.edit_text_border)

        proceedButtonToInspire.background = AppCompatResources.getDrawable(this,
            R.drawable.my_blue_btn)

        proceedButtonToInspire.setTextColor(Color.parseColor("#FFFFFF"))

        topImg.visibility = View.VISIBLE
        topImg.setImageResource(R.drawable.content300)
        proceedButtonToInspire.setText(R.string.creategoal)
    }

    private suspend fun rateDay(day : String){

        val descString = descEditTxt.text.toString().trimStart().trimEnd()
        var ratedSuccess = false

        if (rating != 0){
            dataSource.updateSpecificDayRatingAndDesc(rating, descString, day)
            ratedSuccess = true
        } else {
            Toast.makeText(this, "Please rate the entry", Toast.LENGTH_SHORT).show()
        }

        //show goal edited
        if (ratedSuccess){
            topImg.visibility = View.GONE
            mainLinear.visibility = View.GONE
            inspireRelative.visibility = View.VISIBLE
            doneImg.setImageResource(R.drawable.good_rated_inspire_img)

            //text in inspire interface changes according to rating
            if (rateForFab){
                when (rating) {
                    HAPPY_RATING -> {
                        inspireTxt.setText(R.string.goalRatedHappy)
                    }
                    MEH_RATING -> {
                        inspireTxt.setText(R.string.goalRatedMeh)
                    }
                    SAD_RATING -> {
                        inspireTxt.setText(R.string.goalRatedSad)
                    }
                }
            }
            proceedBtn.background = AppCompatResources.getDrawable(this, R.drawable.my_blue_btn)

            rateForFab = false
        }

    }

    private fun checkIfGoalExistsForDay(todayFromDb: String) : Boolean {
        val calender = Calendar.getInstance()
        val day = calender.get(Calendar.DAY_OF_YEAR)
        val year = calender.get(Calendar.YEAR)
        val dayInYear = "$day + $year"
        return todayFromDb == dayInYear
    }

    //to update recyclerview in previous activity
    override fun onBackPressed() {
        proceedToProdTrackFrag()
        super.onBackPressed()
    }

    private fun proceedToProdTrackFrag() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        sharedPreferencesEditor?.putBoolean(SPLASH_SCREEN_KEY, false)?.apply()
        startActivity(intent)
        finish()
    }

    private suspend fun insertGoalIntoDatabase() {

        //FIXME: fix year and day of year var, these are random
        val calender = Calendar.getInstance()
        val year = calender.get(Calendar.YEAR)
        val dayOfYear = calender.get(Calendar.DAY_OF_YEAR)
        val dayOfYearIntoDatabase = "$dayOfYear + $year"

        val newGoal = goalEditTxt.text.toString()

        //get arguments from database to check if today's goal exists
        val day = dataSource.getDayInYear("$dayOfYear + $year")?.day

        //check if today exists
        val todayExists : Boolean = checkIfGoalExistsForDay("$day")


        if (todayExists){
            Toast.makeText(this, "There's already a goal for today", Toast.LENGTH_SHORT).show()
            return
        }

        if(newGoal.isNotEmpty() && !todayExists){
            val productiveDay : ProductiveDay =
                ProductiveDay(0, newGoal.trimStart().trimEnd(), dayOfYearIntoDatabase, "", -1)
            dataSource.insert(productiveDay)
            addForFab = false

            //show goal saved screen
            topImg.visibility = View.GONE
            mainLinear.visibility = View.GONE
            inspireRelative.visibility = View.VISIBLE
            doneImg.setImageResource(R.drawable.efficiency300)
            inspireTxt.setText(R.string.goalSaved)
            proceedBtn.background = AppCompatResources.getDrawable(this, R.drawable.my_blue_btn)

            //create streak logic, send day and year as int to compare to last entry and see if
            //more than 1 day passed since creation
            val lastDayFromDbArray = dataSource.getAllDays()
            val lastDay = cutDateFromDbToDays(lastDayFromDbArray[0].day)
            val lastYr = cutDateFromDbToYrs(lastDayFromDbArray[0].day)

            Log.e(LOG_TAG, "Last day logged is $lastDay in year $year")
            Log.e(LOG_TAG, "Array of days: ${lastDayFromDbArray[0].day}")
            Log.e(LOG_TAG, "Attempting to create streak, streak value is ${prefs?.getInt(STREAK_COUNTER, 0)}")

            createStreak(dayOfYear, year, lastDay.toInt(), lastYr.toInt())

        } else {
            Toast.makeText(this, "You forgot to make a goal", Toast.LENGTH_SHORT).show()
        }

    }

    //create a streak
    private fun createStreak(dayOfYear : Int, year : Int, lastDay : Int, lastYr : Int){

        //start streak from 0
        prefs?.getInt(STREAK_COUNTER, 0)

        //get current entry date
        val currentDayCalender= Calendar.getInstance()
        currentDayCalender[Calendar.DAY_OF_YEAR] = dayOfYear
        currentDayCalender[Calendar.YEAR] = year
        val currentDateMillis = currentDayCalender.timeInMillis

        //last entry date
        val lastEntryCalender = Calendar.getInstance()
        currentDayCalender[Calendar.DAY_OF_YEAR] = lastDay
        currentDayCalender[Calendar.YEAR] = lastYr
        val lastEntryInMillis = lastEntryCalender.timeInMillis

        val millisInDay : Long = 24 * 60 * 60 * 1000
        //compare them in millis
        if (currentDateMillis > lastEntryInMillis + millisInDay){
            //skipped a day, lose streak
            sharedPreferencesEditor?.putInt(STREAK_COUNTER, 0)?.apply()
            Log.e(LOG_TAG, "LOSE STREAK")

        } else if (currentDateMillis < lastEntryInMillis + millisInDay){
            //streak continued, increment 1
            val streak = prefs?.getInt(STREAK_COUNTER, 0)
            sharedPreferencesEditor?.putInt(STREAK_COUNTER, streak!! + 1)?.apply()
            Log.e(LOG_TAG, "STREAK INCREASED BY 1")
        }

    }

    private fun cutDateFromDbToYrs(fullDateFromDb: String): String {
        return fullDateFromDb.substring(
            fullDateFromDb.lastIndexOf(" ") + 1
        ).trimEnd().trimStart()
    }

    private fun cutDateFromDbToDays(day: String): String {
        val i = day.indexOf(' ')
        return day.substring(0, i)
    }

}