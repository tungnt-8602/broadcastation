package com.example.broadcastation.presentation.add

import androidx.lifecycle.MutableLiveData
import com.example.broadcastation.common.base.BaseViewModel

class AddViewModel : BaseViewModel() {
    /* **********************************************************************
     * Variable
     ********************************************************************** */

    val uuid = MutableLiveData<String>()

    /* **********************************************************************
     * Function
     ********************************************************************** */

    fun bind() {
        uuid.value = storage.deviceId
    }

    /* **********************************************************************
     * Class
     ********************************************************************** */
}