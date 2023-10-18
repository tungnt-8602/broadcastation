package com.example.broadcastation.control

import android.content.Context
import com.example.broadcastation.common.logger.Logger
import com.example.broadcastation.entity.config.MqttConfig
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.UUID

class Mqtt(val callback: Callback) {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private var client: MqttClient? = null
    private val options = MqttConnectOptions()
    private val persistence = MemoryPersistence()
    private val logger = Logger.instance
    private val gson = Gson()

    /* **********************************************************************
     * Function
     ********************************************************************** */
    fun connect(context: Context) {
        val clientId = UUID.randomUUID().toString()
        val data = callback.getMqttData()

        with(options) {
            userName = data.user
            password = data.password.toCharArray()

            isAutomaticReconnect = true
            isCleanSession = true
            connectionTimeout = 5
            keepAliveInterval = 60
        }

        val domain = data.domain
        client = MqttClient(domain, clientId, persistence)
        client?.let {
            it.setCallback(mqttEventHandle)
            try {
                it.connect(options)
            } catch (e: Exception) {
                callback.connectFail(e.message ?: "")
            }
        }

        logger.i("subscribe")
        val channel = data.channel
        subscribe(channel)
    }

    fun disconnect() {
        try {
            client?.disconnect()

            logger.i("subscribe")
            val channel = callback.getMqttData().channel
            unSubscribe(channel)
        } catch (e: Exception) {
            callback.connectFail(e.message ?: "")
        }
    }

    private fun verify(): Boolean {
        if (client == null) {
            logger.w("client null")
            return false
        } else if (client?.isConnected == false) {
            logger.w("client disconnect")
            return false
        }
        return true
    }

    fun sendMessage(message: String, context: Context) {
        if (!verify()) {
            return
        }

        client?.let {
            val topic = "event1/client/${message}"
            val jsonObject = JsonObject().apply {
                addProperty("message", message)
            }
            val json = gson.toJson(jsonObject)
            val message = MqttMessage(json.toByteArray())
            try {
                it.publish(topic, message)
            } catch (e: Exception) {
                callback.connectFail(e.message ?: "")
            }
        }
    }

    /* **********************************************************************
     * Subscribe
     ********************************************************************** */
    private fun subscribe(topic: String) {
        logger.i("verify")
        if (!verify()) {
            return
        }

        try {
            client?.subscribe(topic) { topic, message ->
                logger.d("type[all] topic[$topic] - message[${message}]")
            }
        } catch (e: Exception) {
            logger.w(e.message ?: "")
            return
        }
    }

    private fun unSubscribe(topic: String) {
        logger.i("verify")
        if (!verify()) {
            return
        }

        try {
            client?.unsubscribe(topic)
        } catch (e: Exception) {
            logger.w(e.message ?: "")
            return
        }
    }

    /* **********************************************************************
     * Event
     ********************************************************************** */
    private val mqttEventHandle = object : MqttCallback {
        override fun connectionLost(cause: Throwable?) {
            logger.w(cause?.message ?: "connectionLost")
        }

        override fun messageArrived(topic: String?, message: MqttMessage?) {
            logger.d("type[common] topic[$topic] - message[${message.toString()}]")
        }

        override fun deliveryComplete(token: IMqttDeliveryToken?) {
            logger.d(" type[common] token[${token.toString()}] - message[${token?.message}]")
        }
    }

    /* **********************************************************************
 * Class
 ********************************************************************** */
    interface Callback {
        fun getMqttData(): MqttConfig
        fun connectFail(error: String)
    }
}

