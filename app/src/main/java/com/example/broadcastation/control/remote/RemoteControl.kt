package com.example.broadcastation.control.remote

import android.content.Context
import com.example.broadcastation.common.logger.Logger
import com.example.broadcastation.control.Mqtt
import com.example.broadcastation.control.StorageControl
import com.example.broadcastation.entity.config.MqttConfig
import com.google.gson.Gson

class RemoteControl {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private val logger = Logger.instance
    private val gson = Gson()

    companion object {
        val instance = RemoteControl()
    }

    private val storage = StorageControl.instance
    private val mqtt = Mqtt(callback = object : Mqtt.Callback {
        override fun getMqttData(): MqttConfig {
            return MqttConfig(
                storage.userName,
                storage.passWord,
                storage.domainMqtt,
                storage.port,
                storage.channel,
                storage.content
            )
        }

        override fun connectFail(error: String) {
            logger.w("connect fail : $error")
        }
    })

    /* **********************************************************************
     * Real time
     ********************************************************************** */
    fun createConnect(context: Context) {
        mqtt.connect(context = context)
    }

    fun stopConnect() {
        mqtt.disconnect()
    }

    fun sendMessage(message: String, context: Context) {
        mqtt.sendMessage(message, context)
    }
}