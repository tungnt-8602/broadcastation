package com.example.broadcastation.common.base

import androidx.lifecycle.ViewModel
import com.example.broadcastation.common.logger.Logger
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

abstract class BaseViewModel : ViewModel() {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    internal val gson = Gson()
    internal val logger = Logger.instance

    private val job = SupervisorJob()
    protected val scope = CoroutineScope(Dispatchers.IO + job)
}