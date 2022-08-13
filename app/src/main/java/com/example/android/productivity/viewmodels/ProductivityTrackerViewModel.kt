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

package com.example.android.productivity.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.example.android.productivity.database.ProductivityDatabaseDao
import com.example.android.productivity.database.ProductiveDay
import kotlinx.coroutines.launch

/**
 * ViewModel for SleepTrackerFragment.
 */
class ProductivityTrackerViewModel(
    val database: ProductivityDatabaseDao,
    application: Application) : AndroidViewModel(application) {

    private var today = MutableLiveData<ProductiveDay?>()

    /**
     * Converted nights to Spanned for displaying.
     */
    val nightsString = "placeholder"


    private val _eventGotDataToFillRV = MutableLiveData<Boolean>()
    val eventGotDataToFillRV: LiveData<Boolean>
        get() = _eventGotDataToFillRV

    //var to navigate to AddGoalFragment
    private val _navToAddGoalFragment = MutableLiveData<Boolean>()
    val navToAddGoalFragment: LiveData<Boolean>
        get() = _navToAddGoalFragment


    /**
     * Request a toast by setting this value to true.
     *
     * This is private because we don't want to expose setting this value to the Fragment.
     */
    private var _showSnackbarEvent = MutableLiveData<Boolean>()

    /**
     * If this is true, immediately `show()` a toast and call `doneShowingSnackbar()`.
     */
    val showSnackBarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    /**
     * Variable that tells the Fragment to navigate to a specific [SleepQualityFragment]
     *
     * This is private because we don't want to expose setting this value to the Fragment.
     */
    private val _navigateToSleepQuality = MutableLiveData<ProductiveDay>()


    val navigateToSleepQuality: LiveData<ProductiveDay>
        get() = _navigateToSleepQuality

    /**
     * Call this immediately after calling `show()` on a toast.
     *
     * It will clear the toast request, so if the user rotates their phone it won't show a duplicate
     * toast.
     */

    init {
        initializeToday()
    }

    private fun initializeToday() {
        viewModelScope.launch {
            today.value = getTodayFromDatabase()
        }
    }

    /**
     *  Handling the case of the stopped app or forgotten recording,
     *  the start and end times will be the same.j
     *
     *  If the start time and end time are not the same, then we do not have an unfinished
     *  recording.
     */
    private suspend fun getTodayFromDatabase(): ProductiveDay? {
        var today = database.getToday()
//        if (today?.secondGoal != today?.firstGoal) {
//            today = null
//        }
        return today
    }

    private suspend fun insert(today: ProductiveDay) {
        database.insert(today)
    }

    private suspend fun update(today: ProductiveDay) {
        database.update(today)
    }

    private suspend fun clear() {
        database.clear()
    }

    /**
     * Executes when the START button is clicked.
     */
    fun onStartTracking() {
        viewModelScope.launch {
            // Create a new night, which captures the current time,
            // and insert it into the database.
            val newDay = ProductiveDay()

            insert(newDay)

            today.value = getTodayFromDatabase()
        }
    }

    /**
     * Executes when the user enters the main screen for the first time.
     */
    fun insertDataFirstTime(initialData : ProductiveDay) {
        viewModelScope.launch {

            insert(initialData)

        }
    }

    /**
     * Executes when the STOP button is clicked.
     */
    fun onStopTracking() {
        viewModelScope.launch {
            // In Kotlin, the return@label syntax is used for specifying which function among
            // several nested ones this statement returns from.
            // In this case, we are specifying to return from launch(),
            // not the lambda.
            val oldDay = today.value ?: return@launch

            // Update the night in the database to add the end time.
//            oldNight.secondGoal = System.currentTimeMillis()

            update(oldDay)

            // Set state to navigate to the SleepQualityFragment.
            _navigateToSleepQuality.value = oldDay
        }
    }

    /**
     * Executes when the CLEAR button is clicked.
     */
    fun onClear() {
        viewModelScope.launch {
            // Clear the database table.
            clear()

            // And clear tonight since it's no longer in the database
            today.value = null

            // Show a snackbar message, because it's friendly.
            _showSnackbarEvent.value = true
        }
    }

    fun doneGettingDataForRV() {
        _eventGotDataToFillRV.value = true
    }

    fun NavToAddGoalFragment() {
        _navToAddGoalFragment.value = true
    }


}