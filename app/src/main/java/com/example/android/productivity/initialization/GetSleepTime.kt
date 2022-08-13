package com.example.android.productivity.initialization

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.android.productivity.R
import com.example.android.productivity.viewmodels.GetSleepTimeViewModel


class GetSleepTime : Fragment() {

    var sleepTimeSavedInt : Int = -1
    var hrsInInt : Int = -1
    var userNameFromBundle = "-1"
    val args: GetSleepTimeArgs by navArgs()
    val SLEEP_KEY_HOUR = "Sleep Time Hour"
    val SLEEP_KEY_MINUTE = "Sleep Time Minute"
    val LOG_TAG = "GetSleepTime"
    val NAME_KEY = "Name"

    companion object {
        fun newInstance() = GetSleepTime()
    }

    private lateinit var viewModel: GetSleepTimeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.get_sleep_time_fragment, container, false)

    }

    override fun onStart() {
        super.onStart()
        val timePicker: TimePicker = requireView().findViewById<TimePicker>(R.id.timePickerSleep)
        val btn : Button = requireView().findViewById(R.id.getSleepBtn)
        val sleepTimeTxt = requireView().findViewById<TextView>(R.id.sleepTimeText)

        val prefs: SharedPreferences? = requireContext().getSharedPreferences(
            getString(R.string.app_name), AppCompatActivity.MODE_PRIVATE
        )

        val username = prefs?.getString(NAME_KEY, "")


        sleepTimeTxt.text = "$username, ${getString(R.string.whenDoYouSleep)}"

        timePicker.setIs24HourView(true)

        timePicker.setOnTimeChangedListener { picker, hour, minute ->
            convertSleepTime(picker, hour, minute)
        }

        btn.setOnClickListener{

            navigateToWakeUpTimeFragAndSendArgs()
        }

    }

    private fun saveSleepTimeInSP(hour: Int, minute: Int) {
        val preferences: SharedPreferences = requireContext()
            .getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt(SLEEP_KEY_HOUR, hour)
        editor.putInt(SLEEP_KEY_MINUTE, minute)
        editor.apply()

        val sleepTimeHourFromSF = preferences.getInt(SLEEP_KEY_HOUR, -1)
        val sleepTimeMinuteFromSF = preferences.getInt(SLEEP_KEY_MINUTE, -1)
        Log.i("$LOG_TAG 1", "Saved sleep time hour into SF, value is $sleepTimeHourFromSF hr and $sleepTimeMinuteFromSF min")
    }

    private fun navigateToWakeUpTimeFragAndSendArgs() {

        if (sleepTimeSavedInt != -1){
            try{
                userNameFromBundle = args.nameOfUser
                this.findNavController().navigate(GetSleepTimeDirections
                    .actionGetSleepTimeToGetWakeUpTime(userNameFromBundle, sleepTimeSavedInt))

            } catch (e : Exception) {
                Toast.makeText(requireContext(), "Unable to get username from bundle", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Please set a time", Toast.LENGTH_SHORT).show()
        }

    }

    private fun convertSleepTime(picker: TimePicker, hour : Int, minute: Int) {
        // display format of time
        val timeFormatted = String.format("%02d%02d", hour, minute)
        sleepTimeSavedInt = timeFormatted.toInt()

        saveSleepTimeInSP(hour, minute)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[GetSleepTimeViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //hide custom action bar
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

}