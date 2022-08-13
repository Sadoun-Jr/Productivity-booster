package com.example.android.productivity.initialization

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.android.productivity.R
import com.example.android.productivity.viewmodels.GetWakeUpTimeViewModel
import java.lang.Exception

class GetWakeUpTime : Fragment() {

    var wakeUpTimeSavedInt : Int = -1
    var hrsInInt : Int = -1
    var userNameFromBundle = "-1"
    var sleepTimeFromBundle = -1
    val args: GetWakeUpTimeArgs by navArgs()
    val LOG_TAG = "GetWakeUpTime"
    val WAKE_KEY_HOUR = "Wake up Hour"
    val WAKE_KEY_MINUTE = "Wake up Minute"
    val NAME_KEY = "Name"


    companion object {
        fun newInstance() = GetWakeUpTime()
    }

    private lateinit var viewModel: GetWakeUpTimeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.get_wake_up_time_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(GetWakeUpTimeViewModel::class.java)
    }

    override fun onStart() {

        super.onStart()

        val prefs: SharedPreferences? = requireContext().getSharedPreferences(
            getString(R.string.app_name), AppCompatActivity.MODE_PRIVATE
        )

        val username = prefs?.getString(NAME_KEY, "")

        val timePicker: TimePicker = requireView().findViewById(R.id.timePickerWake)
        val btn: Button = requireView().findViewById(R.id.proceedToMainBtn)
        val wakeTimeTxt = requireView().findViewById<TextView>(R.id.wakeTimeText)


        wakeTimeTxt.text = "${getString(R.string.whenDoYouWakeUp)}, $username?"

        timePicker.setOnTimeChangedListener { picker, hour, minute ->
            convertWakeTime(picker, hour, minute)
        }

        timePicker.setIs24HourView(true)

        btn.setOnClickListener{

            navigateToMainFragAndSendArgs()
        }


    }

    private fun navigateToMainFragAndSendArgs() {

        if(wakeUpTimeSavedInt != -1){
            try{
                userNameFromBundle = args.nameOfUser
                sleepTimeFromBundle = args.sleepTime
                this.findNavController().navigate(GetWakeUpTimeDirections
                    .actionGetWakeUpTimeToProductivityTrackerFragment(userNameFromBundle, sleepTimeFromBundle, wakeUpTimeSavedInt))

            } catch (e : Exception) {
                Toast.makeText(requireContext(), "Error. Unable to get username and time from bundle", Toast.LENGTH_SHORT).show()
            }
        }  else {
            Toast.makeText(requireContext(), "Please set a time", Toast.LENGTH_SHORT).show()
        }

    }

    private fun saveWakeTimeInSP(hour: Int, minute: Int) {
        val preferences: SharedPreferences = requireContext()
            .getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt(WAKE_KEY_HOUR, hour)
        editor.putInt(WAKE_KEY_MINUTE, minute)
        editor.apply()

        val wakeTimeHourFromSF = preferences.getInt(WAKE_KEY_HOUR, -1)
        val wakeTimeMinuteFromSF = preferences.getInt(WAKE_KEY_MINUTE, -1)
        Log.i("$LOG_TAG 1", "Saved wakeup time hour into SF, value is $wakeTimeHourFromSF hr and $wakeTimeMinuteFromSF min")
    }

    private fun convertWakeTime(picker: TimePicker, hour : Int, minute: Int) {

        // display format of time
        val timeFormatted = String.format("%02d%02d", hour, minute)
        wakeUpTimeSavedInt = timeFormatted.toInt()

        saveWakeTimeInSP(hour, minute)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //hide custom action bar
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

}