package com.example.broadcastation.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.broadcastation.common.base.BaseViewModel
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.presentation.add.AddViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

class HomeViewModel : BaseViewModel() {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    var remoteList = MutableLiveData(mutableListOf<Remote>())

    /* **********************************************************************
     * Function
     ********************************************************************** */
    fun addRemote(remote: Remote) {
        viewModelScope.launch(Dispatchers.IO){
            remoteList.value?.add(remote)
        }
    }

//    fun getRemoteList() : MutableList<Remote>{
//        return remoteList
//    }

    /* **********************************************************************
    * Class
    ********************************************************************** */
}