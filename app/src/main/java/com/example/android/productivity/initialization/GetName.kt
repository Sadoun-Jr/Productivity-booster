package com.example.android.productivity.initialization

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.productivity.R
import com.example.android.productivity.database.ProductivityDatabase
import com.example.android.productivity.databinding.GetInitialDetailsBinding
import com.example.android.productivity.viewmodelFactories.GetInitialDetailsViewModelFactory
import com.example.android.productivity.viewmodels.GetNameViewModel


class GetName : Fragment() {

    var usernameFromSP = "-1"
    var sleepTimeFromSP = -1
    var wakeTimeFromSP = -1
    val LOG_TAG = "GetName"
    val NAME_KEY = "Name"
    val SLEEP_KEY_HOUR = "Sleep Time Hour"
    val SLEEP_KEY_MINUTE = "Sleep Time Minute"
    val WAKE_KEY_HOUR = "Wake up Hour"
    val WAKE_KEY_MINUTE = "Wake up Minute"
    val MAIN_MENU_CLICK = "main menu"
    val RESET_DETAILS = "reset details"


    companion object {
        fun newInstance() = GetName()
    }

    var usersName : String = ""


    var prefs: SharedPreferences? = null
    var sharedPreferencesEditor: SharedPreferences.Editor? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: GetInitialDetailsBinding = DataBindingUtil.inflate(
            inflater, R.layout.get_initial_details, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = ProductivityDatabase.getInstance(application).productivityDatabaseDao

        val viewModelFactory = GetInitialDetailsViewModelFactory(dataSource, application)

        // Get a reference to the ViewModel associated with this fragment.
        val getNameViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(GetNameViewModel::class.java)

        binding.getInitialDetailsViewModel = getNameViewModel

        binding.lifecycleOwner = this

        prefs = requireContext().getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE
        )
        sharedPreferencesEditor = prefs?.edit()

        //check if initial data exists or not to skip to ProfTrackerFrag
        val int = prefs?.getInt(SLEEP_KEY_HOUR, -1)
        getNameViewModel.checkInitialDataExists(int != -1)

        //reset initials when accessed from main menu
        val resetInitials = prefs?.getBoolean(RESET_DETAILS, false)
        Log.e(LOG_TAG, "Reseting intitials, $resetInitials")

        if (!resetInitials!!){
            //proceed normallu
            getNameViewModel.userExists.observe(viewLifecycleOwner, Observer { exists ->
                if (exists){
                    //get the user details to give as arguments to ProdTrackerFrag and navigate
                    usernameFromSP = prefs?.getString(NAME_KEY, "").toString()
                    sleepTimeFromSP = prefs?.getInt(SLEEP_KEY_HOUR, -1)!!.toInt()
                    wakeTimeFromSP = prefs?.getInt(WAKE_KEY_HOUR, -1)!!.toInt()

                    Log.v("$LOG_TAG 2", "Checking if user exists , value is $usernameFromSP")

                    getNameViewModel.doneGettingInitFromDB()
                    getNameViewModel.doneNavigating()
                }
                else{
                    //show initial page views cuz user is staying in the frag
                    binding.nameEditText.visibility = View.VISIBLE
                    binding.getInitialInfoButton.visibility = View.VISIBLE

                }
            })
        } else if (resetInitials){
            //clicked main menu item to reset
            binding.nameEditText.visibility = View.VISIBLE
            binding.getInitialInfoButton.visibility = View.VISIBLE
            binding.letsGetStarted.setText(R.string.edit)
            sharedPreferencesEditor?.putBoolean(RESET_DETAILS, false)?.apply()

        }



        getNameViewModel.gotInitFromDB.observe(viewLifecycleOwner, Observer { gotData ->
            if(gotData){
                this.findNavController().navigate(
                    GetNameDirections
                        .actionGetInitialDetailsToProductivityTrackerFragment
                            ("-2", -2, -2)
                )
            }
        })


        //get initial name and save it to SF
        binding.getInitialInfoButton.setOnClickListener(){
            val nameEntered : String = binding.nameEditText.text.toString()
            if (nameEntered.isNotEmpty() && nameEntered.length < 15){

                //remove white space
                nameEntered.trimEnd().trimStart()

                //put the name in shared prefs
                sharedPreferencesEditor?.putString(NAME_KEY, nameEntered)
                sharedPreferencesEditor?.apply()
                usersName = nameEntered

                getNameViewModel.gotName()

            }  else {
                Toast.makeText(requireContext(),
                    "Please set a name no longer than 15 characters in length",
                    Toast.LENGTH_SHORT).show()
            }

        }

        //navigate to main screen after getting name
        getNameViewModel.nameEnteredEvent.observe(viewLifecycleOwner, Observer { entered ->
            entered?.let {
                this.findNavController().navigate(
                    GetNameDirections
                        .actionGetInitialDetailsToGetSleepTime(usersName))
            }
            getNameViewModel.doneNavigating
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //hide custom action bar
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

}