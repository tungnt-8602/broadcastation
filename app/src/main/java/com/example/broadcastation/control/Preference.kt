package com.example.broadcastation.control

import android.content.Context
import android.content.SharedPreferences
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.presentation.home.HomeViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class Preference {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private val preferenceName = "Preference"
    private val addRemote = "$preferenceName:addRemote"
    private val actionRemote = "$preferenceName:actionRemote"
    private val messageAction = "$preferenceName:messageAction"
    private val messageBroadcast = "$preferenceName:messageBroadcast"
    private val categoryList = "$preferenceName:categoryList"
    private val sortRemote = "$preferenceName:sortRemote"

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
    fun saveRemote(remote: Remote) {
        val gson = Gson()
        val json = gson.toJson(remote)
        var pastList = shared?.getString(addRemote, "")
        if (!pastList.isNullOrEmpty()) {
            pastList += ","
        }
        editor?.apply {
            putString(addRemote, pastList + json)
            apply()
        }
    }

    fun updateAllRemotes(remotes: MutableList<Remote>) {
        val type: Type = object : TypeToken<MutableList<Remote?>?>() {}.type
        val json = gson.toJson(remotes, type)
        editor?.apply {
            putString(addRemote, json.replace("[", "").replace("]", ""))
            apply()
        }
    }

    fun getAllRemotes(): MutableList<Remote> {
        val listRemote: MutableList<Remote>
        val serializedObject: String? = shared?.getString(addRemote, null)
        if (serializedObject != null) {
            val type: Type = object : TypeToken<MutableList<Remote?>?>() {}.type
            listRemote = gson.fromJson("[$serializedObject]", type) as MutableList<Remote>
        } else {
            return mutableListOf()
        }
        return listRemote
    }

    fun actionRemote(action: String) {
        editor?.apply {
            putString(actionRemote, action)
            apply()
        }
    }

    fun getActionRemote(): String? {
        return shared?.getString(actionRemote, "")
    }

    fun getMessageAction(): String? {
        return shared?.getString(messageAction, "")
    }

    fun saveMessageAction(message: String) {
        editor?.apply {
            putString(messageAction, message)
            apply()
        }
    }

    fun getMessageBroadcast(): String? {
        return shared?.getString(messageBroadcast, "")
    }

    fun saveMessageBroadcast(message: String) {
        editor?.apply {
            putString(messageBroadcast, message)
            apply()
        }
    }

    fun setCategoryList(categories: MutableList<String>) {
        val type: Type = object : TypeToken<MutableList<String>>() {}.type
        val json = gson.toJson(categories, type)
        editor?.apply {
            putString(categoryList, json.replace("[", "").replace("]", ""))
            apply()
        }
    }

    fun getCategoryList(): MutableList<String> {
        val listCategory: MutableList<String>
        val serializedObject: String? = shared?.getString(categoryList, null)
        if (serializedObject != null) {
            val type: Type = object : TypeToken<MutableList<String>>() {}.type
            listCategory = gson.fromJson("[$serializedObject]", type) as MutableList<String>
        } else {
            return mutableListOf()
        }
        return listCategory
    }

    fun getSortType(): HomeViewModel.SortType {
        return when(shared?.getString(sortRemote, "Normal")){
            "Grid" -> HomeViewModel.SortType.Grid
            "Category" -> HomeViewModel.SortType.Category
            "Broadcast" -> HomeViewModel.SortType.Broadcast
            else -> HomeViewModel.SortType.Normal
        }
    }

    fun saveSortType(type: HomeViewModel.SortType) {
        val stringType = when(type){
            HomeViewModel.SortType.Normal -> "Normal"
            HomeViewModel.SortType.Grid -> "Grid"
            HomeViewModel.SortType.Category -> "Category"
            HomeViewModel.SortType.Broadcast -> "Broadcast"
        }
        editor?.apply {
            putString(sortRemote, stringType)
            apply()
        }
    }
}