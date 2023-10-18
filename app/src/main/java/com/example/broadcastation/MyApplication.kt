package com.example.broadcastation

import android.app.Application
import com.example.broadcastation.control.LocalControl

class MyApplication : Application(){
    /* **********************************************************************
     * Life Cycle
     ********************************************************************** */
    override fun onCreate() {
        super.onCreate()
        initControl()
    }

    /* **********************************************************************
     * Function
     ********************************************************************** */
    private fun initControl() {
        LocalControl.getInstance().apply {
            setContext(applicationContext)
        }
    }
}