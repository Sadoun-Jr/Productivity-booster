package com.example.android.productivity.initialization

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.android.productivity.MainActivity
import com.example.android.productivity.R
import com.ramotion.paperonboarding.PaperOnboardingEngine
import com.ramotion.paperonboarding.PaperOnboardingFragment
import com.ramotion.paperonboarding.PaperOnboardingPage


class OnBoardingDesignOne : AppCompatActivity() {

    private var fragmentManager: FragmentManager? = null
    var prefs: SharedPreferences? = null
    var sharedPreferencesEditor: SharedPreferences.Editor? = null

    val FIRST_TIME_USER_ONBOARD = "first time"
    val LOG_TAG = "OnBoard screen"
    private lateinit var frameLayout : FrameLayout


    override fun onCreate(savedInstanceState: Bundle?) {

        (this).supportActionBar?.hide()

        prefs = getSharedPreferences(
            getString(R.string.app_name), MODE_PRIVATE
        )
        sharedPreferencesEditor = prefs?.edit()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)

        fragmentManager = supportFragmentManager

        val skipBtn = findViewById<Button>(R.id.skipBtn)
        frameLayout = findViewById<FrameLayout>(R.id.frame_layout)

        skipBtn.setOnClickListener{
            val firstTime = sharedPreferencesEditor?.putBoolean(FIRST_TIME_USER_ONBOARD, false)?.apply()
            Log.e(LOG_TAG, "First time? $firstTime")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // new instance is created and data is took from an
        // array list known as getDataonborading

        // new instance is created and data is took from an
        // array list known as getDataonborading
        val paperOnboardingFragment = PaperOnboardingFragment.newInstance(getDataforOnboarding())
        val fragmentTransaction: FragmentTransaction = fragmentManager!!.beginTransaction()

        // fragmentTransaction method is used
        // do all the transactions or changes
        // between different fragments
        // fragmentTransaction method is used
        // do all the transactions or changes
        // between different fragments

        fragmentTransaction.add(R.id.frame_layout, paperOnboardingFragment)

        // all the changes are committed
        fragmentTransaction.commit()

        paperOnboardingFragment.setOnRightOutListener {
            val firstTime = sharedPreferencesEditor?.putBoolean(FIRST_TIME_USER_ONBOARD, false)?.apply()
            Log.e(LOG_TAG, "First time? $firstTime")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        //change backgrounds
        paperOnboardingFragment.setOnChangeListener{ firstFrag: Int, secondFrag: Int ->
//            Log.e(LOG_TAG, "Value of first fragment is $firstFrag and value of second fragment is $secondFrag")
            Log.e(LOG_TAG, "Value of first fragment is $firstFrag and value of second fragment is $secondFrag")

            if (firstFrag == 3 && secondFrag == 4){
                skipBtn.text = "LET'S GO!"
            } else {
                skipBtn.text = "SKIP"
            }
        }

        }

    private fun changeBackGroundsOnFrameChange(c:Context, drawable:Int){
        val sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            frameLayout.setBackgroundDrawable(ContextCompat.getDrawable(c, drawable) );
        } else {
            frameLayout.background = ContextCompat.getDrawable(c, drawable);
        }
    }

    }

    class MyPaperOnboardingEngine(
        var rootLayout: View,
        var contentElements: ArrayList<PaperOnboardingPage>, var appContext: Context
        ) : PaperOnboardingEngine(rootLayout, contentElements, appContext) {

        override fun createContentTextView(paperOnboardingPage: PaperOnboardingPage?): ViewGroup {
            val group = super.createContentTextView(paperOnboardingPage)
            (group.getChildAt(0) as TextView).setTextColor(Color.WHITE)
            (group.getChildAt(1) as TextView).setTextColor(Color.WHITE)
            return group
        }
    }

private fun getDataforOnboarding() : ArrayList<PaperOnboardingPage> {
    val clr1 = "#7AD7F0"
    val clr2 = "#92DFF3"
    val clr3 = "#B7E9F7"
    val clr4 = "#DBF3FA"
    val clr5 = "#F5FCFF"

    val source = PaperOnboardingPage(
        "Welcome!",  //the title
        "Be more productive, while having fun!",  //the descreption
        Color.parseColor(clr1),    //the colour of the background
//        Color.TRANSPARENT,
        R.drawable.b1,  //the main icon, should be big
        R.drawable.happy_selected  //the icon in the bottom navigation bar
    );

    val source1 = PaperOnboardingPage(
        "Create a goal",
        "Every day, you'll get a notification right as you wake up to remind you to set the new day's goal",
        Color.parseColor(clr2),
//        Color.TRANSPARENT,
        R.drawable.b2,
        R.drawable.pencil
    );

    val source2 = PaperOnboardingPage(
        "Focus on your goal",
        "Be more productive and prioritise finishing your goal throughout the day",
        Color.parseColor(clr3),
//        Color.TRANSPARENT,
        R.drawable.b3,
        R.drawable.target
    );

    val source3 = PaperOnboardingPage(
        "Rate your goal",
        "Right before sleep time, you'll get a notification to remind you to rate your performance for the day",
        Color.parseColor(clr4),
//        Color.TRANSPARENT,
        R.drawable.b4,
        R.drawable.star
    );

    val source4 = PaperOnboardingPage(
        "Improve",
        "Keep a record of all of your previous goals to help you track your improvement",
        Color.parseColor(clr5),
//        Color.TRANSPARENT,
        R.drawable.b5,
        R.drawable.muscle
    );


    // array list is used to store
    // data of onbaording screen
    val elements = ArrayList<PaperOnboardingPage>();

    // all the sources(data to show on screens)
    // are added to array list
    elements.add(source);
    elements.add(source1);
    elements.add(source2);
    elements.add(source3);
    elements.add(source4);

    return elements;
}
