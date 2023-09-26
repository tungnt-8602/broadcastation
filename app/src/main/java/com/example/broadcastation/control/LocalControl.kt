package com.example.broadcastation.control

import android.content.Context
import com.example.broadcastation.common.base.BaseControl
import com.example.broadcastation.entity.Remote

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
    fun saveRemote(remote: Remote) {
        preference.saveRemote(remote)
    }

    fun saveAllRemote(remotes: MutableList<Remote>) {
        preference.updateAllRemotes(remotes)
    }

    fun getAllRemote(): MutableList<Remote>{
        return preference.getAllRemotes()
    }

    fun editRemote(method: String) {
        preference.editRemote(method)
    }

    fun getEditRemote(): String {
        return preference.isEditRemote() ?: ""
    }

}