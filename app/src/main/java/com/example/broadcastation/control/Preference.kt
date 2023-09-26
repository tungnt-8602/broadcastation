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

    fun getAllRemotes() : ArrayList<Remote>{
        var listRemote: ArrayList<Remote> = arrayListOf()
        val serializedObject: String? = shared?.getString(addRemote, null)
        if (serializedObject != null) {
            val gson = Gson()
            val type: Type = object : TypeToken<MutableList<Remote?>?>() {}.type
            listRemote = gson.fromJson("$serializedObject", type) as ArrayList<Remote>
        }
        else{
            return arrayListOf()
        }
        return listRemote
    }

    fun editRemote(edit: String) {
        editor?.apply {
            putString(editRemote, edit)
            apply()
        }
    }

    fun isEditRemote(): String? {
        return shared?.getString(editRemote, "")
    }
}