package com.example.broadcastation.presentation

import android.content.Context
import com.example.broadcastation.common.base.BaseViewModel
import com.example.broadcastation.common.utility.getUUID

class MainViewModel : BaseViewModel(){
    /* **********************************************************************
     * Variable
     ********************************************************************** */

    /* **********************************************************************
     * Function
     ********************************************************************** */

    fun getData(context: Context){
        storage.deviceId = context.getUUID()
    }

    /* **********************************************************************
    * Class
    ********************************************************************** */
}