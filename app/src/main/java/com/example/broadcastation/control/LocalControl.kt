package com.example.broadcastation.control

import android.content.Context
import com.example.broadcastation.common.base.BaseControl
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.presentation.home.HomeViewModel
import com.example.broadcastation.repository.PreferenceRepository

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
    private val repository = PreferenceRepository.getInstance()


    /* **********************************************************************
     * Function
     ********************************************************************** */
    fun setContext(context: Context) {
        repository.setContext(context)
    }

    /* **********************************************************************
     * Notification before power outage
     ********************************************************************** */
    fun saveRemote(remote: Remote) {
        repository.saveRemote(remote)
    }

    fun saveAllRemote(remotes: MutableList<Remote>) {
        repository.saveAllRemote(remotes)
    }

    fun getAllRemote(): MutableList<Remote> {
        return repository.getAllRemote()
    }

    fun actionRemote(action: String) {
        repository.actionRemote(action)
    }

    fun getActionRemote(): String {
        return repository.getActionRemote()
    }

    fun getMessageAction(): String {
        return repository.getMessageAction()
    }

    fun saveMessageAction(message: String) {
        repository.saveMessageAction(message)
    }

    fun getMessageBroadcast(): String {
        return repository.getMessageBroadcast()
    }

    fun saveMessageBroadcast(message: String) {
        repository.saveMessageBroadcast(message)
    }

    fun getCategoryList(): MutableList<String> {
        return repository.getCategoryList()
    }

    fun saveCategoryList(categories: MutableList<String>) {
        repository.saveCategoryList(categories)
    }

    fun getSortType(): HomeViewModel.SortType {
        return repository.getSortType()
    }

    fun saveSortType(type: HomeViewModel.SortType) {
        repository.saveSortType(type)
    }
}