package com.example.broadcastation.control

import android.content.Context
import android.content.SharedPreferences
import com.example.broadcastation.entity.Remote
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
    private val filterList = "$preferenceName:filterList"

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
        if(!pastList.isNullOrEmpty()){
            pastList += ","
        }
        editor?.apply {
            putString(addRemote, pastList + json)
            apply()
        }
    }

    fun updateAllRemotes(remotes: MutableList<Remote>) {
        val gson = Gson()
        val type: Type = object : TypeToken<MutableList<Remote?>?>() {}.type
        val json = gson.toJson(remotes, type)
        editor?.apply {
            putString(addRemote, json.replace("[","").replace("]",""))
            apply()
        }
    }

    fun getAllRemotes() : MutableList<Remote>{
        val listRemote: MutableList<Remote>
        val serializedObject: String? = shared?.getString(addRemote, null)
        if (serializedObject != null) {
            val gson = Gson()
            val type: Type = object : TypeToken<MutableList<Remote?>?>() {}.type
            listRemote = gson.fromJson("[$serializedObject]", type) as MutableList<Remote>
        }
        else{
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

    fun saveMessageAction(message: String){
        editor?.apply{
            putString(messageAction, message)
            apply()
        }
    }

    fun getMessageBroadcast(): String? {
        return shared?.getString(messageBroadcast, "")
    }

    fun saveMessageBroadcast(message: String){
        editor?.apply{
            putString(messageBroadcast, message)
            apply()
        }
    }

    fun setFilterList(type: String) {
        editor?.apply {
            putString(filterList, type)
            apply()
        }
    }

    fun getFilterListType(): String? {
        return shared?.getString(filterList, "")
    }
}