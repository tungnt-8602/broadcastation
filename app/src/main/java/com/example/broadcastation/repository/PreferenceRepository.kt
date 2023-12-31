package com.example.broadcastation.repository

import android.content.Context
import com.example.broadcastation.common.base.BaseRepository
import com.example.broadcastation.control.Preference
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.presentation.home.HomeViewModel

class PreferenceRepository private constructor() : BaseRepository() {

    /* **********************************************************************
     * Variable
     ********************************************************************** */
    companion object {
        @Volatile
        private var instance: PreferenceRepository? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: PreferenceRepository().also { instance = it }
        }
    }

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

    fun getAllRemote(): MutableList<Remote> {
        return preference.getAllRemotes()
    }

    fun actionRemote(action: String) {
        preference.actionRemote(action)
    }

    fun getActionRemote(): String {
        return preference.getActionRemote() ?: ""
    }

    fun getMessageAction(): String {
        return preference.getMessageAction() ?: ""
    }

    fun saveMessageAction(message: String) {
        preference.saveMessageAction(message)
    }

    fun getMessageBroadcast(): String {
        return preference.getMessageBroadcast() ?: ""
    }

    fun saveMessageBroadcast(message: String) {
        preference.saveMessageBroadcast(message)
    }

    fun getCategoryList(): MutableList<String> {
        return preference.getCategoryList()
    }

    fun saveCategoryList(categories: MutableList<String>) {
        preference.setCategoryList(categories)
    }

    fun getSortType(): HomeViewModel.SortType {
        return preference.getSortType()
    }

    fun saveSortType(type: HomeViewModel.SortType) {
        preference.saveSortType(type)
    }
}