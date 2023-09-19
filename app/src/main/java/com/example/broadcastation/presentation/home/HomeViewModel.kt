package com.example.broadcastation.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.broadcastation.R
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
    var remoteLiveList = MutableLiveData(mutableListOf<Remote>())
    private var remoteList = mutableListOf<Remote>()

    init {

        remoteList.add(Remote("Home", "", 1, R.drawable.ic_local_fill))
        remoteList.add(Remote("TV", "", 1, R.drawable.ic_http_fill))
        remoteList.add(Remote("Mobile", "", 1, R.drawable.ic_local_fill))
        remoteList.add(Remote("Ipad", "", 1, R.drawable.ic_http_fill))
        remoteList.add(Remote("Web", "", 1, R.drawable.ic_mqtt))
        remoteLiveList.postValue(remoteList)
    }

    /* **********************************************************************
     * Function
     ********************************************************************** */
    fun addRemote(remote: Remote) {
        logger.i("added")
        remoteList.add(remote)
        remoteLiveList.postValue(remoteList)
    }

    fun getRemoteList(): MutableList<Remote> {
        return remoteLiveList.value ?: mutableListOf<Remote>()
    }

    /* **********************************************************************
    * Class
    ********************************************************************** */
}