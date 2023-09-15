package com.example.broadcastation.presentation.add

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseViewModel

class AddViewModel : BaseViewModel() {
    /* **********************************************************************
         * Variable
         ********************************************************************** */
    private var tabs: List<Tab>? = null
    val uuid = MutableLiveData<String>()

    init {
        tabs = listOf(
            Tab(
                type = Type.LOCAL,
                title = R.string.local,
                icon = R.drawable.ic_local_selector,
                fragment = null
            ),
            Tab(
                type = Type.HTTP,
                title = R.string.http,
                icon = R.drawable.ic_http_selector,
                fragment = null
            ),
            Tab(
                type = Type.MQTT,
                title = R.string.mqtt,
                icon = R.drawable.ic_mqtt_selector,
                fragment = null
            )
        )
    }

    /* **********************************************************************
     * Function
     ********************************************************************** */
    fun getTabs(): List<Tab>? {
        return tabs
    }

    fun bind(){
        uuid.value = storage.deviceId
    }

    /* **********************************************************************
     * Class
     ********************************************************************** */
    enum class Type { HTTP, MQTT, LOCAL }
    data class Tab(
        val type: Type, val title: Int, val icon: Int, var fragment: Fragment?
    )
}