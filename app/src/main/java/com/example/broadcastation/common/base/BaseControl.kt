package com.example.broadcastation.common.base

import com.example.broadcastation.common.logger.Logger
import com.google.gson.Gson

abstract class BaseControl {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    protected val logger = Logger.instance
    protected val gson = Gson()

    /* **********************************************************************
     * Function
     ********************************************************************** */
}