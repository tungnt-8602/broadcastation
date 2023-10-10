package com.example.broadcastation.entity

import com.example.broadcastation.presentation.home.ItemRemoteAdapter

data class MqttConfig(
    val user: String,
    val password: String,
    val domain: String,
    val port: String?,
    val channel: String,
    val content: String
) : Config(ItemRemoteAdapter.Type.MQTT)