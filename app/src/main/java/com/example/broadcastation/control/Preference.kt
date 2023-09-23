package com.example.broadcastation.control

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class Preference {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private val preferenceName = "Preference"
    private val addRemote = "$preferenceName:addRemote"
    private val updateRemote = "$preferenceName:updateRemote"
    private val editRemote = "$preferenceName:editRemote"

    private var shared: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var gson = Gson()

    /* **********************************************************************
     * Function - Init
     ********************************************************************** */
    fun initPreference(context: Context) {
        shared = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
        editor = shared?.edit()
    }

    /* **********************************************************************
     * Admob
     ********************************************************************** */
    fun addRemote(night: Boolean) {
        editor?.apply {
            putBoolean(addRemote, night)
            apply()
        }
    }

    fun isAddRemote(): Boolean? {
        return shared?.getBoolean(addRemote, false)
    }

    fun updateRemote(night: Boolean) {
        editor?.apply {
            putBoolean(updateRemote, night)
            apply()
        }
    }

    fun isUpdateRemote(): Boolean? {
        return shared?.getBoolean(updateRemote, false)
    }

    fun editRemote(edit: Boolean) {
        editor?.apply {
            putBoolean(editRemote, edit)
            apply()
        }
    }

    fun isEditRemote(): Boolean? {
        return shared?.getBoolean(editRemote, false)
    }
}