package com.example.broadcastation.control

import android.content.Context
import android.preference.Preference
import com.example.broadcastation.common.base.BaseControl

class LocalControl private constructor() : BaseControl() {
    /* **********************************************************************
     * Singleton
     ********************************************************************** */
    companion object {
        @Volatile
        private var instance: LocalControl? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: LocalControl().also { instance = it }
        }
    }

    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private val preference = Preference()

    /* **********************************************************************
     * Function
     ********************************************************************** */
    fun setContext(context: Context) {
        preference.initPreference(context)
    }

    /* **********************************************************************
     * Notification before power outage
     ********************************************************************** */
    fun addRemote(on: Boolean) {
        preference.addRemote(on)
    }

    fun isAddRemote(): Boolean {
        return preference.isAddRemote() ?: false
    }

    fun updateRemote(on: Boolean) {
        preference.updateRemote(on)
    }

    fun isUpdateRemote(): Boolean {
        return preference.isUpdateRemote() ?: false
    }

    fun editRemote(on: Boolean) {
        preference.editRemote(on)
    }

    fun isEditRemote(): Boolean {
        return preference.isEditRemote() ?: false
    }

}