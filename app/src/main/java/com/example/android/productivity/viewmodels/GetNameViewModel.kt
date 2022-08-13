package com.example.android.productivity.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android.productivity.database.ProductiveDay
import com.example.android.productivity.database.ProductivityDatabaseDao
import java.lang.Exception

class GetNameViewModel(
    val database: ProductivityDatabaseDao,
    application: Application) : AndroidViewModel(application){

    private var _userExists = MutableLiveData<Boolean>()
    val userExists: LiveData<Boolean>
        get() = _userExists

    private var _gotInitFromDB = MutableLiveData<Boolean>()
    val gotInitFromDB: LiveData<Boolean>
        get() = _gotInitFromDB

    private var _nameEnteredEvent = MutableLiveData<Boolean>()
    val nameEnteredEvent: LiveData<Boolean>
        get() = _nameEnteredEvent

    private var _doneNavigating = MutableLiveData<Boolean>()
    val doneNavigating: LiveData<Boolean>
        get() = _doneNavigating



    fun gotName() {
        _nameEnteredEvent.value = true
    }

    fun doneNavigating(){
        _doneNavigating.value = true
    }

    fun doneGettingInitFromDB(){
        _gotInitFromDB.value = true
    }

    fun checkInitialDataExists(boolean : Boolean) {
        _userExists.value = boolean
    }

    suspend fun getTodayFromDatabase(): ProductiveDay? {
        return database.getToday()
    }



}