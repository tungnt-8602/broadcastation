package com.example.broadcastation.common.base

import androidx.lifecycle.ViewModel
import com.example.broadcastation.common.logger.Logger
import com.google.gson.Gson

open class BaseViewModel : ViewModel() {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    protected val gson = Gson()
    protected val logger = Logger.instance
}